# Engineering (robustesse en production)

Ce fichier regroupe les règles qui rendent le système **fiable sous charge** :
observabilité, concurrence, idempotence, résilience et traitements asynchrones.
Elles complètent `architecture.md`, `spring-boot.md` et `persistance.md`.

> Sur une plateforme de paiement, un double débit, un remboursement appliqué deux
> fois ou un appel PSP bloqué ne sont pas des bugs mineurs : ce sont des incidents.
> Toute fonctionnalité qui touche de l'argent doit passer la checklist en fin de
> fichier.

---

## Observabilité

### Métriques suivies

Stack : **Spring Boot Actuator + Micrometer**, exposition au format Prometheus
(`/actuator/prometheus`).

| Indicateur                          | Source / métrique Micrometer                                       |
|-------------------------------------|--------------------------------------------------------------------|
| Latence moyenne                     | `http_server_requests_seconds` (`sum` / `count`)                   |
| P95 et P99 des temps de réponse     | `http_server_requests_seconds` avec histogramme de percentiles     |
| Requêtes par seconde                | `rate(http_server_requests_seconds_count[1m])`                     |
| Taux d'erreurs HTTP 5xx             | `http_server_requests_seconds_count{outcome="SERVER_ERROR"}`       |
| CPU et mémoire                      | `process_cpu_usage`, `system_cpu_usage`, `jvm_memory_used_bytes`   |
| Nombre de connexions PostgreSQL     | `hikaricp_connections_active`, `_idle`, `_pending`, `_timeout_total` |
| Temps d'exécution des requêtes SQL  | `@Timed` sur les adapters de persistence + `pg_stat_statements` côté base |
| Taux de cache hit                   | `cache_gets_total{result="hit"}` / `cache_gets_total` (Caffeine `recordStats()`) |

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
      slo:
        http.server.requests: 100ms, 300ms, 1s
    tags:
      application: ${spring.application.name}
```

### Règles

- Toute métrique métier critique est instrumentée (paiements initiés / confirmés /
  échoués, remboursements, appels PSP) via un `MeterRegistry` injecté dans un
  **adapter d'infrastructure**, jamais dans le `domain`.
- Ne jamais mettre en tag une valeur à forte cardinalité (`paymentId`, `merchantId`,
  email...) : cela fait exploser le stockage des métriques.
- `hikaricp_connections_pending > 0` de façon durable ou des
  `hikaricp_connections_timeout_total` qui augmentent = pool saturé : à alerter.
- Les états de Resilience4j (circuit breaker ouvert, retries) sont exposés
  automatiquement via `resilience4j-micrometer` et doivent être alertés.

### Logs

- Logs structurés (JSON) avec un **identifiant de corrélation** (`traceId`) propagé
  sur toute la requête (Micrometer Tracing), y compris dans les traitements
  asynchrones.
- Niveaux : `ERROR` = action humaine requise, `WARN` = situation anormale récupérée
  (retry, fallback, conflit de concurrence), `INFO` = fait métier important,
  `DEBUG` = diagnostic, désactivé en prod.
- **Jamais** de données sensibles dans les logs : numéro de carte, CVV, IBAN complet,
  token, mot de passe.

---

## Concurrence

Tout service critique (modification d'un solde, d'un paiement, d'un remboursement,
d'une réconciliation) doit être pensé pour **plusieurs requêtes simultanées sur la
même ressource** et **plusieurs instances** de l'application.

### Choisir le bon mécanisme

| Situation                                                            | Mécanisme                                               |
|----------------------------------------------------------------------|---------------------------------------------------------|
| Cas par défaut, conflits rares                                       | **Verrou optimiste** (`@Version`)                       |
| Forte contention sur une même ligne, ou invariant financier (solde, montant remboursable restant) | **Verrou pessimiste** (`PESSIMISTIC_WRITE`) |
| Garantir l'unicité (une seule transaction par référence PSP, une clé d'idempotence) | **Contrainte d'unicité** en base                |
| Tâche planifiée qui ne doit tourner que sur une instance              | **Verrou distribué** (ShedLock sur PostgreSQL)          |

- Un `synchronized` ou un `ReentrantLock` Java **ne protège rien** dès qu'il y a plus
  d'une instance : il est interdit pour protéger une donnée métier.
- Le verrou se fait **en base de données**, au plus près de la donnée, dans la
  transaction du use case.

### Verrou optimiste (par défaut)

```java
@Entity
@Table(name = "payment")
class PaymentJpaEntity {
    @Id
    private UUID id;

    @Version
    private long version;
}
```

- Une `OptimisticLockingFailureException` est traduite en `409 Conflict` par le
  `GlobalExceptionHandler`, ou rejouée par un retry **uniquement si l'opération est
  idempotente**.

### Verrou pessimiste (invariant financier)

Exemple : deux remboursements partiels simultanés ne doivent pas dépasser le montant
capturé.

```java
interface PaymentSpringDataRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select p from PaymentJpaEntity p where p.id = :id")
    Optional<PaymentJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
```

- Le port out expose une intention explicite : `PaymentRepository.findByIdForUpdate(...)`.
- Toujours définir un **timeout de verrou** : un verrou sans timeout peut bloquer le
  pool de connexions.
- Verrouiller les ressources **toujours dans le même ordre** (ex : par identifiant
  croissant) pour éviter les deadlocks.
- Garder la transaction verrouillée **la plus courte possible** : jamais d'appel HTTP
  externe (PSP, email) pendant qu'un verrou est tenu.

### Tâches planifiées

```java
@Scheduled(cron = "0 0 2 * * *")
@SchedulerLock(name = "dailyReconciliation", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
public void runDailyReconciliation() {
    runReconciliationUseCase.execute();
}
```

> ShedLock est une nouvelle dépendance : justifiée dès que l'application tourne sur
> plus d'une instance et possède des `@Scheduled` qui ne doivent pas s'exécuter en
> double.

---

## Idempotence

Une opération est idempotente si **l'exécuter N fois produit le même effet qu'une
seule fois**. C'est obligatoire dès qu'une requête peut être rejouée : retry client,
timeout réseau, retry Resilience4j, redelivery d'un événement.

### API : clé d'idempotence

Toute opération `POST` qui crée un effet financier (initier un paiement, demander un
remboursement) exige un header **`Idempotency-Key`** fourni par le client.

1. Le client envoie `Idempotency-Key: <uuid>`.
2. Le serveur cherche la clé dans la table `idempotency_key` (scope : marchand +
   endpoint).
   - Clé inconnue → on exécute, puis on stocke la clé, le hash de la requête et la
     réponse, **dans la même transaction** que l'opération métier.
   - Clé connue, même hash → on renvoie **la réponse stockée**, sans réexécuter.
   - Clé connue, hash différent → `422 Unprocessable Entity`.
   - Clé en cours de traitement → `409 Conflict`.
3. Une **contrainte d'unicité** sur `(merchant_id, idempotency_key)` protège contre
   deux requêtes simultanées avec la même clé.
4. Les clés expirent (ex : 24 h) et sont purgées par une tâche planifiée.

- Le mécanisme est implémenté **une seule fois**, dans `shared/infrastructure/web`
  (filtre ou intercepteur) avec son port dans `shared/application`, puis activé par
  endpoint. Pas de copier-coller dans chaque controller.

### Idempotence métier

- Le domaine rend les transitions d'état sûres : confirmer un paiement déjà
  `CONFIRMED` avec la même référence PSP est un **no-op**, pas une erreur ni un
  second effet.
- Les références externes (référence PSP, identifiant de webhook) sont stockées avec
  une **contrainte d'unicité**.

### Consommateurs d'événements et webhooks

- Tout consommateur doit supporter qu'un même message arrive **plusieurs fois**
  (livraison *at-least-once*, en particulier avec l'Outbox).
- On enregistre l'identifiant du message traité dans une table `processed_event`
  (clé unique `event_id` + `consumer`) dans la même transaction que le traitement ;
  un doublon est ignoré.
- Les webhooks PSP sont dédupliqués par leur identifiant d'événement fourni par le PSP.

---

## Résilience (Resilience4j)

Tout appel vers un **système externe** (PSP, API tierce, service d'email/SMS) doit
être protégé. Stack : `resilience4j-spring-boot3`.

### Où l'appliquer

- **Uniquement dans les adapters sortants** (`infrastructure/clients`). Le `domain`
  et l'`application` ne connaissent pas Resilience4j : ils voient un port
  (`PaymentProviderGateway`) qui réussit ou lève une exception métier
  (`PaymentProviderUnavailableException`).
- Pas de Resilience4j sur les appels à notre propre base de données ni entre modules
  du monolithe (appel en mémoire).

### Patterns

| Pattern             | Quand                                                                  | Obligatoire |
|---------------------|------------------------------------------------------------------------|:-----------:|
| **TimeLimiter / timeout** | Tout appel réseau sortant                                         | ✅ |
| **CircuitBreaker**  | Tout système externe dont la panne ne doit pas faire tomber le nôtre  | ✅ |
| **Retry**           | Erreurs transitoires (timeout, 503), **seulement si l'appel est idempotent** | Selon cas |
| **Bulkhead**        | Isoler un système externe lent pour qu'il ne consomme pas tous les threads | Selon cas |
| **RateLimiter**     | Respecter un quota imposé par un fournisseur                          | Selon cas |

```java
@Component
@RequiredArgsConstructor
class PaymentProviderStripeAdapter implements PaymentProviderGateway {

    private final StripeClient stripeClient;

    @Override
    @CircuitBreaker(name = "psp", fallbackMethod = "captureFallback")
    @Retry(name = "psp")
    public CaptureResult capture(CaptureRequest request) {
        return stripeClient.capture(request.toStripe(), request.idempotencyKey());
    }

    private CaptureResult captureFallback(CaptureRequest request, Throwable cause) {
        throw new PaymentProviderUnavailableException(request.paymentId(), cause);
    }
}
```

```yaml
resilience4j:
  circuitbreaker:
    instances:
      psp:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      psp:
        max-attempts: 3
        wait-duration: 200ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - java.net.SocketTimeoutException
          - org.springframework.web.client.HttpServerErrorException
        ignore-exceptions:
          - com.acme.payment.domain.exceptions.PaymentDeclinedException
  timelimiter:
    instances:
      psp:
        timeout-duration: 3s
```

### Règles

- **Un timeout est toujours défini** : sur le client HTTP (connect + read) et/ou via
  `TimeLimiter` (qui s'applique aux appels asynchrones `CompletableFuture`). Jamais
  d'appel réseau sans limite de temps.
- **Ne jamais retenter une erreur métier** (carte refusée, fonds insuffisants,
  400/422) : uniquement les erreurs transitoires.
- **Un retry n'est autorisé que si l'appel est idempotent** : on transmet toujours
  une clé d'idempotence au PSP.
- Ordre d'application : `Retry ( CircuitBreaker ( TimeLimiter ( Bulkhead ( appel ) ) ) )`.
  Le retry s'exécute à l'extérieur, le circuit breaker compte chaque tentative.
- Un fallback ne doit **jamais** masquer un échec de paiement par un faux succès :
  il traduit l'échec en exception métier ou en statut `PENDING` à réconcilier.
- La configuration est dans `application.yml`, jamais en dur dans le code.

---

## Traitements asynchrones

### Principe

On **découple** du flux synchrone tout ce qui n'est pas nécessaire pour répondre à
l'appelant : notifications (email, SMS), webhooks sortants vers le marchand, mise à
jour de projections, statistiques, réaction d'un autre module.

La réponse HTTP ne doit pas attendre (ni échouer à cause de) un traitement
secondaire.

### Choisir le mécanisme

| Mécanisme                                         | Garantie                         | Quand l'utiliser                                         |
|---------------------------------------------------|----------------------------------|----------------------------------------------------------|
| **`ApplicationEventPublisher`** (par défaut)      | En mémoire, perdu si crash après le commit | Effet secondaire dont la perte est acceptable ou rattrapable (email de confirmation, cache, statistiques) |
| **Transactional Outbox**                          | *At-least-once*, survit aux crashs | Effet **qui ne doit pas être perdu** : événement financier consommé par un autre module, webhook marchand, publication vers un broker externe, réconciliation |

> **Par défaut : `ApplicationEventPublisher`**. On passe à l'Outbox **uniquement
> quand c'est justifié** par l'impossibilité de perdre l'événement. Cette
> justification doit apparaître dans le plan d'implémentation.

### `ApplicationEventPublisher` (par défaut)

- Le use case publie via le port `DomainEventPublisher` (défini dans
  `shared/application`), dont l'implémentation dans `shared/infrastructure/events`
  délègue à `ApplicationEventPublisher`.
- Le consommateur écoute **après commit** et s'exécute en **asynchrone** :

```java
@Component
@RequiredArgsConstructor
class PaymentConfirmedNotificationListener {

    private final SendPaymentConfirmationUseCase sendPaymentConfirmationUseCase;

    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(PaymentConfirmedEvent event) {
        sendPaymentConfirmationUseCase.execute(event.paymentId());
    }
}
```

- `AFTER_COMMIT` : on ne déclenche jamais un effet externe pour une transaction qui
  a été annulée.
- `@Async` utilise un **executor dédié et borné** (`ThreadPoolTaskExecutor` avec
  taille de pool et de file définies), jamais l'executor par défaut. Les threads
  virtuels de Java 21 (`spring.threads.virtual.enabled=true`) sont acceptables pour
  des traitements I/O.
- Si le listener doit écrire en base, il ouvre **sa propre transaction**
  (`@Transactional(propagation = REQUIRES_NEW)` sur le use case appelé).
- Une exception dans un listener asynchrone est **loggée en `ERROR`** (via un
  `AsyncUncaughtExceptionHandler`), jamais avalée silencieusement.

### Transactional Outbox (quand c'est justifié)

Problème résolu : garantir que **l'état métier et l'événement sont enregistrés
ensemble**. Sans Outbox, un crash entre le commit et la publication perd
l'événement.

1. Dans la **même transaction** que l'opération métier, l'événement est inséré dans
   la table `outbox_event` du module (`id`, `aggregate_type`, `aggregate_id`,
   `event_type`, `payload` JSON, `occurred_at`, `published_at`, `attempts`).
2. Un **relay** (`infrastructure/messaging`) lit les événements non publiés et les
   publie (vers les listeners internes ou vers un broker), puis renseigne
   `published_at`.
3. Le relay lit avec `SELECT ... FOR UPDATE SKIP LOCKED` (ou est protégé par
   ShedLock) pour ne pas publier deux fois en multi-instance.
4. Les consommateurs sont **idempotents** (voir [Idempotence](#idempotence)) car la
   livraison est *at-least-once*.
5. Les événements publiés sont purgés ou archivés régulièrement.

- Les événements dont la publication échoue de façon répétée (`attempts` au-delà du
  seuil) sont marqués en échec et **alertés** ; ils ne bloquent pas les suivants.
- Avant d'implémenter un relay à la main, évaluer **Spring Modulith Event
  Publication Registry** (`spring-modulith-starter-jpa`), qui fournit une Outbox
  persistante pour les `@ApplicationModuleListener`, cohérente avec l'architecture
  modulaire.

---

## Checklist pour un service critique

Avant de considérer une fonctionnalité critique (paiement, remboursement,
réconciliation) comme terminée :

1. **Concurrence** : que se passe-t-il si deux requêtes modifient la même ressource
   en même temps ? Quel verrou ? Quel timeout ?
2. **Idempotence** : que se passe-t-il si la requête ou l'événement est rejoué ?
   Clé d'idempotence ? Contrainte d'unicité ?
3. **Résilience** : chaque appel externe a-t-il un timeout et un circuit breaker ?
   Le retry est-il limité aux erreurs transitoires et aux appels idempotents ?
4. **Asynchrone** : qu'est-ce qui peut sortir du flux synchrone ? L'événement
   peut-il être perdu (→ `ApplicationEventPublisher`) ou non (→ Outbox) ?
5. **Observabilité** : les métriques, logs et alertes permettent-ils de détecter le
   problème en production ?
6. **Tests** : tests de concurrence (plusieurs threads sur la même ressource) et de
   rejeu (même requête envoyée deux fois) écrits avec Testcontainers.
