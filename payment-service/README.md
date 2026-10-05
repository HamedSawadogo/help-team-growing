# Payment Service

Service de paiement basique : gestion de clients et de leurs paiements.

- **Backend** : Spring Boot 3.5, Java 25, Maven, Spring Data JPA, PostgreSQL
- **Frontend** : React + Vite

## Démarrer PostgreSQL

```bash
docker compose up -d
```

Base créée : `payment_service` (user `payment_user` / password `payment_password`, port 5432).

## Lancer le backend

Nécessite un JDK 25 (le pom cible `java.version=25`).

```bash
cd backend
mvn spring-boot:run
```

API disponible sur `http://localhost:8080/api`.

### Endpoints

| Méthode | URL | Description |
|---|---|---|
| POST | `/api/clients` | Créer un client |
| GET | `/api/clients` | Lister les clients |
| GET | `/api/clients/{id}` | Détail d'un client |
| DELETE | `/api/clients/{id}` | Supprimer un client |
| POST | `/api/payments` | Créer un paiement (statut `PENDING`) |
| GET | `/api/payments` | Lister tous les paiements |
| GET | `/api/payments/client/{clientId}` | Paiements d'un client |
| POST | `/api/payments/{id}/process` | Traiter un paiement (`PENDING` → `COMPLETED`/`FAILED`) |
| POST | `/api/payments/{id}/refund` | Rembourser un paiement (`COMPLETED` → `REFUNDED`) |

## Lancer le frontend

```bash
cd frontend
npm install
npm run dev
```

Interface disponible sur `http://localhost:5173`.
