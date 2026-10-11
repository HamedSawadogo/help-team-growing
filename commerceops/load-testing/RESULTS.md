# Load test results log

Record runs here as you make changes. Don't guess numbers you haven't measured —
leave a row blank and come back to it after running the script.

## mixed_read_load.js

| Date | VUS | Duration | Seed profile | p95 (ms) | Error rate | Notes / what changed |
|------|-----|----------|---------------|----------|------------|------------------------|
|      |     |          |               |          |            | baseline, no changes yet |

## order_creation_load.js

| Date | VUS | Duration | Seed profile | p95 (ms) | Error rate | Notes |
|------|-----|----------|---------------|----------|------------|-------|
|      |     |          |               |          |            |       |

## payment_concurrency.js

| Date | VUS | Invoice total | paidAmount after | Expected if no race | Notes (lost update? 500s? which strategy tested) |
|------|-----|----------------|-------------------|----------------------|----------------------------------------------------|
|      |     |                |                   |                      | current impl: optimistic lock via @Version on Invoice - conflicting writers get a raw 500, not a retry |

## stock_concurrency.js

| Date | VUS | Product | Before (onHand/reserved) | After (onHand/reserved) | Oversold / lost updates? |
|------|-----|---------|---------------------------|---------------------------|---------------------------|
|      |     |         |                           |                           |                           |
