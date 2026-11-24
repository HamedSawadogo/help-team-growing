# Architecture

## Vue d'ensemble

L'application est un **monolithe modulaire** : un seul artefact déployable (un seul
`jar` Spring Boot), découpé en **modules métier autonomes** alignés sur les
*Bounded Contexts* du domaine (ex : `payment`, `refund`, `merchant`, `reconciliation`).

À l'intérieur de chaque module, on applique une **Clean Architecture** organisée en
**Ports & Adapters (architecture hexagonale)** avec trois couches :

- `domain` : le cœur métier, en Java pur.
- `application` : les cas d'usage et les ports.
- `infrastructure` : les adapters techniques (web, persistence, messaging, clients externes).

```
            ┌──────────────────────── module "payment" ────────────────────────┐
            │                                                                   │
 HTTP ───▶  │  infrastructure/web ──▶ application/ports/in (UseCase)            │
            │                              │                                    │
            │                              ▼                                    │
            │                       application/services ──▶ domain            │
            │                              │                                    │
            │                              ▼                                    │
            │                   application/ports/out (interfaces)              │
            │                              ▲                                    │
            │  infrastructure/repositories ┘  infrastructure/clients ...        │
            └───────────────────────────────────────────────────────────────────┘
                                 │  api (façade publique) / événements
                                 ▼
                        autres modules (refund, merchant, ...)
```

**Règle de dépendance** : les dépendances pointent toujours **vers l'intérieur**.

```
infrastructure ──▶ application ──▶ domain
```

- `domain` ne dépend de rien (ni d'`application`, ni d'`infrastructure`, ni d'un framework).
- `application` dépend uniquement de `domain`.
- `infrastructure` dépend d'`application` et de `domain`, jamais l'inverse.

---

## Structure des packages

```
com.<company>.<app>
├── Application.java                      # point d'entrée Spring Boot
│
├── shared                                # noyau partagé (Shared Kernel), le plus petit possible
│   ├── domain                            # DomainException, AggregateRoot, DomainEvent, Money...
│   ├── application                       # DomainEventPublisher (port), pagination...
│   └── infrastructure
│       ├── web                           # GlobalExceptionHandler, ErrorResponse
│       ├── security                      # configuration Spring Security
│       ├── events                        # implémentation de DomainEventPublisher
│       └── config                        # configuration transverse (Jackson, OpenAPI, ...)
│
├── payment                               # un module = un Bounded Context
│   ├── api                               # SEULE surface publique du module
│   │   ├── PaymentFacade.java            # interface appelable par les autres modules
│   │   ├── dto                           # PaymentView, ... (records immuables)
│   │   └── events                        # PaymentConfirmedEvent (événements d'intégration)
│   │
│   ├── domain
│   │   ├── model                         # Aggregates, Entités, ValueObjects (Payment, PaymentId, ...)
│   │   ├── services                      # Domain Services (règles multi-aggregates)
│   │   ├── events                        # Domain Events internes au module
│   │   └── exceptions                    # PaymentAlreadyConfirmedException extends DomainException
│   │
│   ├── application
│   │   ├── ports
│   │   │   ├── in                        # InitiatePaymentUseCase, ConfirmPaymentUseCase (interfaces)
│   │   │   └── out                       # PaymentRepository, PaymentProviderGateway (interfaces)
│   │   ├── services                      # InitiatePaymentService implements InitiatePaymentUseCase
│   │   │                                 # PaymentFacadeService implements api.PaymentFacade
│   │   └── commands                      # InitiatePaymentCommand, ... (entrées des use cases)
│   │
│   └── infrastructure
│       ├── web                           # PaymentController, Request/Response DTO, web mappers
│       ├── repositories                  # PaymentJpaEntity, SpringData repo, PaymentJpaRepositoryAdapter
│       ├── clients                       # adapters vers les PSP externes (PaymentProviderStripeAdapter)
│       ├── messaging                     # listeners d'événements d'autres modules, producers
│       ├── mappers                       # mapping domain <-> JPA / DTO (MapStruct)
│       └── config                        # @Configuration et @ConfigurationProperties du module
│
├── refund
│   ├── api
│   ├── domain
│   ├── application
│   └── infrastructure
│
└── merchant
    └── ...
```

> Les noms de packages restent en minuscules et au singulier (`payment`, pas `payments`).

---

## Les modules (Monolithe modulaire)

### Découpage

- Un module correspond à **un Bounded Context métier**, jamais à une couche technique
  (pas de module `controllers`, `utils`, `dao`).
- Un module doit pouvoir être **extrait en microservice** sans refonte : c'est le test
  de bon découpage. S'il faut casser des dizaines de dépendances pour l'extraire, le
  découpage est mauvais.
- Créer un nouveau module est une **décision d'architecture** : elle doit être
  proposée et validée avant implémentation.

### Encapsulation

- Le package `api` est la **seule surface publique** d'un module. Tout le reste
  (`domain`, `application`, `infrastructure`) est **interne** au module.
- Un module ne doit **jamais** importer une classe d'un autre module en dehors de son
  package `api`.
- Les classes internes sont `package-private` dès que c'est possible
  (adapters, entités JPA, mappers, services d'implémentation).
- Les DTO du package `api` sont des `record` immuables. Ils n'exposent **jamais** une
  Entité, un Aggregate ou un ValueObject du domaine.

❌ **À éviter** :

```java
package com.acme.refund.application.services;

import com.acme.payment.domain.model.Payment;                       // domaine d'un autre module
import com.acme.payment.infrastructure.repositories.PaymentJpaRepository; // repo d'un autre module
```

✅ **À faire** :

```java
package com.acme.refund.application.services;

import com.acme.payment.api.PaymentFacade;
import com.acme.payment.api.dto.PaymentView;

@Service
@RequiredArgsConstructor
class RequestRefundService implements RequestRefundUseCase {

    private final PaymentFacade paymentFacade;
    private final RefundRepository refundRepository;

    public RefundId execute(RequestRefundCommand command) {
        PaymentView payment = paymentFacade.findPayment(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));
        Refund refund = Refund.request(payment.id(), payment.capturedAmount(), command.amount());
        refundRepository.save(refund);
        return refund.id();
    }
}
```

> Pour découpler encore plus, le module consommateur peut définir son propre port out
> (`PaymentInformationProvider` dans `refund/application/ports/out`) dont l'adapter
> (`refund/infrastructure/clients`) appelle `PaymentFacade`. Le domaine de `refund`
> ne connaît alors même pas l'existence du module `payment`.

### Communication entre modules

| Besoin                                                   | Mécanisme                                   |
|----------------------------------------------------------|---------------------------------------------|
| Lecture synchrone d'une information d'un autre module    | Appel de la façade `api` du module          |
| Réaction à un fait métier d'un autre module              | **Événement** publié dans `api/events`      |
| Commande qui modifie l'état d'un autre module            | Façade `api`, ou événement si asynchrone acceptable |

- **Privilégier les événements** pour tout ce qui ne nécessite pas une réponse
  immédiate : cela supprime le couplage temporel entre modules.
- Les événements d'intégration (`api/events`) sont des `record` immuables qui ne
  transportent que des types simples / identifiants, jamais d'objets du domaine.
- Les listeners d'événements vivent dans `infrastructure/messaging` du module
  consommateur et délèguent immédiatement à un UseCase.
- **Aucune dépendance cyclique** entre modules (`payment → refund → payment` est
  interdit). Si un cycle apparaît, remplacer l'un des appels par un événement.

### Données

- Chaque module est **propriétaire de ses tables** (schéma dédié ou préfixe de
  table, ex : `payment_*`, `refund_*`).
- Un module ne lit ni n'écrit **jamais** dans les tables d'un autre module.
- **Pas de clé étrangère ni de jointure SQL entre tables de modules différents** :
  on référence un autre module par son identifiant (`PaymentId`), pas par une
  relation JPA.
- Une transaction ne doit pas couvrir plusieurs modules ; la cohérence entre modules
  est **éventuelle**, assurée par les événements.

### Shared Kernel

- `shared` ne contient que des briques **réellement transverses et stables**
  (`DomainException`, `AggregateRoot`, `Money`, `DomainEventPublisher`,
  `GlobalExceptionHandler`...).
- **Aucune logique métier** d'un module dans `shared`. Si une règle n'est utilisée que
  par un module, elle appartient à ce module.
- `shared` ne dépend d'aucun module métier.

---

## Les couches (Clean Architecture / Ports & Adapters)

### `domain`

Le cœur métier : il porte les règles et invariants (voir `code-quality-ddd.md`).

**Contient** : Aggregates, Entités, ValueObjects, Domain Services, Domain Events,
exceptions métier.

**Règles** :

- **Java pur uniquement** : aucune annotation ni import de Spring, JPA, Jackson,
  Lombok, Bean Validation...
- Pas de dépendance vers `application` ni `infrastructure`.
- Un Aggregate est la frontière de cohérence : on le charge, on le modifie et on le
  sauvegarde **en entier**, via sa racine.
- Les ValueObjects sont immuables (`record` de préférence) et se valident dans leur
  constructeur.
- Les exceptions héritent de `DomainException` et sont dans `domain/exceptions`.

```java
public record PaymentId(UUID value) {
    public PaymentId {
        Objects.requireNonNull(value, "PaymentId cannot be null");
    }
}
```

### `application`

Orchestre les cas d'usage : il ne contient **pas** de règle métier, il coordonne le
domaine et les ports.

**Contient** :

- `ports/in` : interfaces des cas d'usage (`<Verbe><Sujet>UseCase`), appelées par les
  adapters entrants (controllers, listeners, schedulers).
- `ports/out` : interfaces dont le module a besoin vers l'extérieur
  (`PaymentRepository`, `PaymentProviderGateway`, `DomainEventPublisher`...).
- `services` : implémentations des use cases (`<Verbe><Sujet>Service`) et de la
  façade `api`.
- `commands` / `queries` : objets d'entrée immuables (`record`) des use cases.

**Règles** :

- Un use case = une intention métier = une classe. Pas de `PaymentService` fourre-tout
  de 30 méthodes.
- Déroulé type d'un use case : charger l'Aggregate via un port out → appeler une
  méthode métier de l'Aggregate → sauvegarder via le port out → publier les événements.
- Les ports out sont définis **du point de vue du besoin métier**, pas de la
  techno : `PaymentProviderGateway.capture(...)`, pas `StripeHttpClient.post(...)`.
- La couche application est la **frontière transactionnelle** : `@Transactional` se
  pose sur les services d'application, jamais sur le domaine ni sur les controllers.
- Seules annotations tolérées : `@Service`, `@Transactional` et `@RequiredArgsConstructor` (Lombok). Aucune
  dépendance vers JPA, le web (`HttpServletRequest`, `ResponseEntity`...) ou un SDK
  externe.
- Ne retourne jamais une entité JPA ; retourne un objet du domaine, un identifiant ou
  une vue en lecture.

```java
public interface ConfirmPaymentUseCase {
    void execute(ConfirmPaymentCommand command);
}

@Service
@RequiredArgsConstructor
class ConfirmPaymentService implements ConfirmPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public void execute(ConfirmPaymentCommand command) {
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));
        payment.confirm(command.providerReference());
        paymentRepository.save(payment);
        eventPublisher.publish(payment.pullDomainEvents());
    }
}
```

### `infrastructure`

Les **adapters** : tout ce qui touche un framework, une base de données, le réseau ou
un système externe.

| Package          | Type d'adapter | Rôle                                                                 |
|------------------|----------------|----------------------------------------------------------------------|
| `web`            | Entrant        | Controllers REST, DTO `Request`/`Response`, validation Bean Validation |
| `messaging`      | Entrant/Sortant| Listeners d'événements (autres modules, broker), producers           |
| `repositories`   | Sortant        | Entités JPA, repositories Spring Data, adapters des ports de persistence |
| `clients`        | Sortant        | Appels à des systèmes externes (PSP, API tierces)                    |
| `mappers`        | —              | Conversion domaine ↔ JPA / DTO (MapStruct)                           |
| `config`         | —              | `@Configuration`, `@ConfigurationProperties` du module               |

**Règles** :

- Un adapter entrant n'appelle **que des interfaces `ports/in`**, jamais un service
  d'implémentation directement, jamais un repository.
- Un adapter sortant **implémente un port out** (`<Sujet><Techno>Adapter`) et
  encapsule complètement la techno : aucune classe JPA / SDK ne sort de l'adapter.
- Les entités JPA sont distinctes du domaine (voir `persistance.md`).
- Les controllers sont fins : validation du DTO → mapping en `Command` → appel du use
  case → mapping de la réponse. Aucune logique métier, aucun `if` métier.
- Les erreurs techniques d'un système externe sont traduites dans l'adapter en
  exception compréhensible par l'application (ex :
  `PaymentProviderUnavailableException`), jamais propagées brutes.

```java
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
class PaymentController {

    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @PostMapping("/{paymentId}/confirmation")
    ResponseEntity<Void> confirm(@PathVariable UUID paymentId,
                                 @Valid @RequestBody ConfirmPaymentRequest request) {
        confirmPaymentUseCase.execute(
                new ConfirmPaymentCommand(new PaymentId(paymentId), request.providerReference()));
        return ResponseEntity.noContent().build();
    }
}
```

---

## Matrice des dépendances autorisées

| De \ Vers                     | `domain` (même module) | `application` (même module) | `infrastructure` (même module) | `api` d'un autre module | interne d'un autre module | `shared` |
|-------------------------------|:----:|:----:|:----:|:----:|:----:|:----:|
| `domain`                      | ✅   | ❌   | ❌   | ❌   | ❌   | ✅ (`shared/domain` seulement) |
| `application`                 | ✅   | ✅   | ❌   | ✅   | ❌   | ✅ (`domain` + `application`) |
| `infrastructure`              | ✅   | ✅   | ✅   | ✅   | ❌   | ✅   |
| `api`                         | ❌   | ❌   | ❌   | ❌   | ❌   | ✅ (types simples) |

---

## Vérification automatique

Ces règles ne doivent pas reposer uniquement sur la revue de code : elles sont
**vérifiées par des tests d'architecture** exécutés dans le build Maven.

- **ArchUnit** pour les règles de couches à l'intérieur d'un module (le `domain`
  n'importe ni Spring ni JPA, `application` n'importe pas `infrastructure`, les
  controllers n'utilisent que des `ports/in`...).
- **Spring Modulith** (`ApplicationModules.of(Application.class).verify()`) pour les
  frontières entre modules (seul `api` est accessible, pas de cycle). Il permet aussi
  de générer la documentation des modules et de tester un module isolément
  (`@ApplicationModuleTest`).

```java
@AnalyzeClasses(packages = "com.acme")
class LayerArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_frameworks =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta.persistence..",
                                        "com.fasterxml.jackson..", "lombok..");

    @ArchTest
    static final ArchRule application_must_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
}

class ModularityTest {

    @Test
    void should_respectModuleBoundaries_when_verifyingApplicationModules() {
        ApplicationModules.of(Application.class).verify();
    }
}
```

> ⚠️ Par défaut, Spring Modulith considère les sous-packages d'un module comme
> internes. Le package `api` (et chacun de ses sous-packages `dto`, `events`) doit
> donc être déclaré explicitement comme interface nommée dans un `package-info.java` :
>
> ```java
> @org.springframework.modulith.NamedInterface("api")
> package com.acme.payment.api;
> ```

> Un test d'architecture en échec bloque le merge, au même titre qu'un test unitaire.

---

## Checklist avant d'ajouter du code

1. **Quel module ?** La fonctionnalité appartient-elle à un module existant ? Sinon,
   proposer un nouveau module avant de coder.
2. **Quelle couche ?** Règle métier → `domain`. Orchestration → `application`.
   Techno / I/O → `infrastructure`.
3. **Besoin d'un autre module ?** Passer par son `api` ou écouter ses événements,
   jamais par ses classes internes ni ses tables.
4. **Besoin d'un système externe ?** Définir un port out dans `application/ports/out`,
   puis un adapter dans `infrastructure`.
5. **Les tests d'architecture passent-ils ?**
