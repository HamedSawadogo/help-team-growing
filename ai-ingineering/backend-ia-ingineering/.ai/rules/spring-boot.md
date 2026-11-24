
## Gestion des exceptions

- Les exceptions métier (invariants du domaine) héritent d'une classe commune
  `DomainException`, sont créées dans `domain/exceptions` et **ne dépendent d'aucun
  framework**.
- Une seule couche traduit ces exceptions en réponse HTTP : un
  `@RestControllerAdvice` global dans `infrastructure/web`, jamais de `try/catch`
  dispersés dans les controllers.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderAlreadyShippedException.class)
    public ResponseEntity<ErrorResponse> handle(OrderAlreadyShippedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ORDER_ALREADY_SHIPPED", ex.getMessage()));
    }
}
```

- Ne jamais exposer une stack trace ou un message technique brut au client final.
- Une exception technique inattendue (NPE, timeout DB...) doit être loggée en `ERROR`
  et retourner un 500 générique, sans détail d'implémentation.
---

## DTO & Mappers
- Une entité ou un ValueObject du domaine **ne doit jamais** être sérialisé
  directement dans une réponse API : on passe toujours par un DTO
  (`infrastructure/web` en entrée/sortie).
- Le mapping se fait via MapStruct de préférence, ou un
  mapper manuel si la conversion est triviale — jamais de mapping dans le controller
- Un DTO ne doit exposer que ce dont le consommateur a besoin (pas de champs internes,
  pas d'ID technique de persistence si non pertinent).

---
## Spring Boot

- Pour des raisons de performance, mettez en `readOnly = true` les transactions qui ne
  font que de la lecture :

  ```java
  @Transactional(readOnly = true)
  public List<OrderSummary> listOrders(CustomerId customerId) { ... }
  ```

- Publiez des **événements** et traitez-les de façon non bloquante pour tout ce qui
  n'a pas besoin d'être synchrone (envoi de SMS, d'email, notifications...) :

  ```java
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOrderCreated(OrderCreatedEvent event) {
      notificationService.sendConfirmationEmail(event.orderId());
  }
  ```

  Préférez `AFTER_COMMIT` pour être sûr que la transaction principale a réussi avant
  de déclencher un effet de bord externe.

- N'oubliez pas la concurrence pour les méthodes concurrentes : verrou pessimiste côté
  base de données quand plusieurs traitements peuvent modifier la même ressource en
  même temps (ex: stock, solde de compte) :

  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.productId = :productId")
  Optional<StockEntity> findByProductIdForUpdate(@Param("productId") String productId);
  ```

  Pour les cas où la contention est faible, préférez un **verrou optimiste**
  (`@Version` sur l'entité JPA) plutôt qu'un verrou pessimiste, plus coûteux.

- Écrire des logs en cas d'erreur et de warning, avec un niveau adapté (voir
  [Observabilité](engineering.md#observabilité)) — jamais de `System.out.println`.


- **Injection par constructeur uniquement**, jamais de `@Autowired` sur un champ.
  Cela rend les dépendances explicites et facilite les tests unitaires (pas besoin de
  Spring pour instancier la classe).
- Le constructeur n'est **pas écrit à la main** : on utilise `@RequiredArgsConstructor`
  (Lombok), qui génère un constructeur avec tous les champs `private final`. Les
  classes restent courtes et lisibles, et l'ajout d'une dépendance se limite à
  déclarer un nouveau champ `final`.

  ```java
  @Service
  @RequiredArgsConstructor
  public class CreateOrderService implements CreateOrderUseCase {
      private final OrderRepository orderRepository;
      private final DomainEventPublisher eventPublisher;
  }
  ```

  ❌ **À éviter** :

  ```java
  @Service
  public class CreateOrderService implements CreateOrderUseCase {
      @Autowired
      private OrderRepository orderRepository;
  }
  ```

  Règles d'usage :

  - Toutes les dépendances injectées doivent être `private final`, sinon elles ne
    sont pas incluses dans le constructeur généré (et restent `null`).
  - `@RequiredArgsConstructor` est réservé aux couches `application` et
    `infrastructure` (services, controllers, adapters, listeners). Le `domain` reste
    en **Java pur, sans Lombok** (voir `architecture.md`).
  - Si le constructeur doit contenir de la logique (validation, valeur par défaut,
    `@Qualifier` sur un paramètre), on écrit le constructeur explicitement.

- La configuration (URLs, timeouts, feature flags) passe par `@ConfigurationProperties`
  typées, jamais par des `@Value("${...}")` éparpillés dans le code métier.
- Un profil Spring par environnement (`dev`, `test`, `staging`, `prod`), aucune valeur
  de prod en dur dans le code ou dans un fichier versionné.

---
