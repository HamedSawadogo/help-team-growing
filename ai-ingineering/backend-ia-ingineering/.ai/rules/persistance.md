## Persistence / JPA

- Les entités JPA (`infrastructure/repositories`) sont **distinctes** des entités du
  domaine. Le mapping se fait via les mappers d'infrastructure. Cela évite de polluer
  le domaine avec des annotations JPA et permet de faire évoluer le schéma BD sans
  toucher aux règles métier.
- Toute évolution de schéma passe par un outil de migration versionné (Flyway ou
  Liquibase), jamais de modification manuelle en base ni de `ddl-auto: update` en
  production.
- Attention aux problèmes N+1 : utilisez `@EntityGraph`, un fetch join JPQL, ou une
  projection dédiée plutôt que de laisser Hibernate faire une requête par ligne.
- Toute requête sur une liste potentiellement grande doit être paginée
  (`Pageable` / `Page<T>`), jamais un `findAll()` sans limite.

---