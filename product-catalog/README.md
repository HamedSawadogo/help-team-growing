# Product Catalog

Spring Boot 4 (Java 25) + PostgreSQL + Angular 20 — liste de produits (catégorie + tags) chargée au scroll.

## Démarrage

```bash
# 1. Base de données (crée le schéma + 100 produits au premier démarrage)
docker compose up -d

# 2. Backend -> http://localhost:8080/api/products?page=0&size=20
cd backend && mvn spring-boot:run

# 3. Frontend -> http://localhost:4200 (proxy /api vers le backend)
cd frontend && npm start
```

## Insérer des produits

`sql/insert-products.sql` est rejouable : chaque exécution ajoute de nouveaux produits
(catégorie et 1 à 3 tags aléatoires). Catégories et tags ne sont créés qu'une fois.

```bash
docker exec -i catalog-db psql -U catalog -d catalog < sql/insert-products.sql            # +100
docker exec -i catalog-db psql -U catalog -d catalog -v nb=5000 < sql/insert-products.sql # +5000
```

## API

`GET /api/products?page=0&size=20` (size max 100)

```json
{ "content": [ { "id": 1, "name": "...", "price": 12.5, "category": {...}, "tags": [...] } ],
  "page": 0, "size": 20, "hasNext": true }
```
