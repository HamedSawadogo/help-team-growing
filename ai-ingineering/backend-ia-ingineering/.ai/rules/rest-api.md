## API REST

- URLs en `kebab-case`, ressources au pluriel : `/orders`, `/orders/{id}/lines`.
- Utiliser les bons verbes/status codes HTTP : `201 Created` + `Location` à la
  création, `204 No Content` pour une suppression réussie, `409 Conflict` pour un
  conflit métier, `422`/`400` pour une validation invalide.
- Versionner l'API dès qu'un changement casse la compatibilité (`/v1/orders`,
  header `Accept-Version`, ou stratégie définie en équipe — mais choisir une méthode
  et s'y tenir sur tout le projet).
- Toute API doit être documentée via OpenAPI/Swagger (`springdoc-openapi`), générée
  automatiquement à partir du code, pas maintenue à la main dans un fichier séparé.
- Valider les DTO d'entrée avec Bean Validation (`@NotNull`, `@Valid`, ...) au niveau
  du controller — ne laissez pas une donnée invalide atteindre le usecase.

---