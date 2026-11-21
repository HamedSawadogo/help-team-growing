# Qualité de code (DDD)

### Domain Rich (pas de modèle anémique)

Toutes les méthodes propres à une entité doivent être encapsulées à l'intérieur de
celle-ci. L'entité protège elle-même ses invariants, elle ne se contente pas de porter
des getters/setters.

❌ **À éviter** (modèle anémique — la logique métier fuit dans un service) :

```java
// Domain
public class Order {
    private OrderStatus status;
    private List<OrderLine> lines;
    // getters / setters uniquement
}

// Application
public class OrderService {
    public void cancel(Order order) {
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel a shipped order");
        }
        order.setStatus(OrderStatus.CANCELLED);
    }
}
```

✅ **À faire** (l'entité protège son propre invariant) :

```java
// Domain
public class Order {
    private OrderStatus status;
    private final List<OrderLine> lines;

    public void cancel() {
        if (this.status == OrderStatus.SHIPPED) {
            throw new OrderAlreadyShippedException(this.id);
        }
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelledEvent(this.id));
    }
}

// Application
public class CancelOrderUseCase {
    private final OrderRepository orderRepository;

    public void execute(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        orderRepository.save(order);
    }
}
```

### Autres règles

- Ne jamais utiliser une **Entité** ou un **ValueObject** dans une autre couche que la
  sienne : passez par un **Aggregate** ou un **Domain Service**, sauf cas très justifié
  à valider avec un autre développeur avant merge.
- Utiliser des **Domain Services** pour orchestrer un traitement complexe qui touche
  plusieurs Aggregates (une règle métier qui n'appartient naturellement à aucun des
  deux Aggregates seuls).
- Mettre en `final` toutes les variables qui ne doivent pas changer après construction
  (champs de ValueObject, dépendances injectées, listes exposées en lecture seule...).
- Nommer très clairement les méthodes et variables. N'ayez pas peur de la longueur du
  nom. **Les commentaires ne sont pas acceptés**, le code doit se décrire lui-même
  (`cancelBecauseOfLatePayment()` plutôt que `cancel() // annule car paiement en retard`).
- Évitez d'utiliser le repository d'un autre module/service directement : passez par son
  service ou son port public. Cela permet de contrôler la complexité et de garder les
  modules découplés.
- Les tests unitaires de la couche `domain` doivent être couverts à **100%**, sans mock
  de framework — que du Java pur.

---

## Principes SOLID

- **S — Single Responsibility** : une classe = une seule raison de changer.
  `Order` gère les règles métier de commande, `OrderMapper` gère uniquement la
  conversion vers un DTO, `OrderRepositoryAdapter` gère uniquement la persistence.

- **O — Open/Closed** : on étend le comportement sans modifier le code existant.
  Exemple avec une stratégie de calcul de remise :

  ```java
  public interface DiscountPolicy {
      Money apply(Order order);
  }

  public class LoyaltyDiscountPolicy implements DiscountPolicy { /* ... */ }
  public class BlackFridayDiscountPolicy implements DiscountPolicy { /* ... */ }
  ```

  Ajouter une nouvelle promotion = ajouter une classe, jamais modifier
  `LoyaltyDiscountPolicy` pour y glisser un `if`.

- **L — Liskov Substitution** : une sous-classe doit pouvoir remplacer sa classe mère
  sans casser le comportement attendu par l'appelant. Si `PhysicalOrder extends Order`
  doit lever une exception sur une méthode que `Order` supporte normalement, c'est que
  l'héritage est mal choisi — préférez la composition.

- **I — Interface Segregation** : préférez plusieurs petites interfaces (ports) ciblées
  plutôt qu'une seule grosse interface. `OrderReader` / `OrderWriter` plutôt qu'un
  `OrderRepository` unique si un consommateur n'a besoin que de lire.

- **D — Dependency Inversion** : `application` dépend d'un **port** (`OrderRepository`
  interface définie dans `application/ports/out`), jamais de l'implémentation JPA.
  C'est `infrastructure` qui fournit l'implémentation concrète, injectée par Spring.

---
## Conventions de nommage

| Élément                     | Convention                          | Exemple                              |
|------------------------------|--------------------------------------|---------------------------------------|
| UseCase (port in)            | `<Verbe><Sujet>UseCase`              | `CreateOrderUseCase`                  |
| Port out                     | `<Sujet><Rôle>`                      | `OrderRepository`, `PaymentGateway`   |
| Implémentation d'un UseCase  | `<Verbe><Sujet>Service`              | `CreateOrderService`                  |
| Adapter d'un port out        | `<Sujet><Techno>Adapter`             | `OrderJpaRepositoryAdapter`           |
| Entité JPA (infra)           | `<Sujet>Entity` / `<Sujet>JpaEntity` | `OrderJpaEntity`                      |
| DTO en entrée d'API          | `<Sujet><Action>Request`             | `CreateOrderRequest`                  |
| DTO en sortie d'API          | `<Sujet>Response`                    | `OrderResponse`                       |
| Domain Event                 | `<Sujet><VerbeAuPassé>Event`         | `OrderCancelledEvent`                 |
| Exception métier             | `<Raison>Exception`                  | `OrderAlreadyShippedException`        |

- Les packages restent en minuscules, singulier (`order`, pas `orders`).
- Les tests suivent la classe testée : `Order` → `OrderTest`,
  `CreateOrderUseCase` → `CreateOrderUseCaseTest`.

---