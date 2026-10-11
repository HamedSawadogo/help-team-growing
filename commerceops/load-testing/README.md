# CommerceOps load testing

k6 scripts against the running backend. Requires either a local `k6` binary
or Docker (the runner falls back to `grafana/k6` automatically).

## Prerequisites

Backend + Postgres running (see `../docker-compose.yml`), seeded with at
least the SMALL profile so there's data to read.

## Scripts

- `mixed_read_load.js` - mixed GET traffic across dashboard, customers,
  products, orders (N+1), invoices (subtler N+1), unpaid invoices report.
  Compare VUS=10/50/100/200 before and after you fix something.
- `order_creation_load.js` - sustained order creation (POST /api/orders).
- `payment_concurrency.js` - fires N concurrent payments at a single freshly
  created invoice to probe the concurrency handling in `PaymentService.pay`.
- `stock_concurrency.js` - fires N concurrent orders for the same product to
  probe `StockService.reserve`.

## Running

```bash
./run.sh mixed_read_load.js -e VUS=50 -e DURATION=2m
./run.sh order_creation_load.js -e VUS=20 -e DURATION=1m
./run.sh payment_concurrency.js -e VUS=20
./run.sh stock_concurrency.js -e VUS=30 -e PRODUCT_ID=5

BASE_URL=http://localhost:18080 ./run.sh mixed_read_load.js   # different port
```

Log what you observe in `RESULTS.md` - before/after numbers only mean
something if you actually ran them both.
