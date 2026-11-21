## Tests

| Couche          | Type de test                          | Outils                                   |
|------------------|-----------------------------------------|--------------------------------------------|
| `domain`         | Unitaire, 100% de couverture, pas de mock de framework | JUnit 5, AssertJ                |
| `application`    | Unitaire, ports mockés                 | JUnit 5, Mockito                          |
| `infrastructure` | Intégration (vraie base, vrai contexte Spring) | Testcontainers, `@SpringBootTest` ciblé |

- Convention de nommage des tests : `should_<résultat>_when_<condition>()`, ex.
  `should_throwOrderAlreadyShippedException_when_cancellingShippedOrder()`.
- Structurer les tests en **Given / When / Then** (ou Arrange/Act/Assert), un seul
  comportement testé par méthode de test.
- Les tests d'intégration infrastructure utilisent Testcontainers (vraie base
  PostgreSQL/MySQL en conteneur), jamais une base H2 qui masque les différences de
  comportement SQL avec la prod.
