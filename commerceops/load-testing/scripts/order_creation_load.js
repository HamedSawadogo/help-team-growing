import http from 'k6/http';
import { check, sleep } from 'k6';

// Sustained order creation: each iteration creates an order with 1-3 lines.
// Exercises OrderService.createOrder - stock reservation plus the blocking
// "external notification" call made from inside the transaction.
//
// Usage:
//   BASE_URL=http://localhost:8080 k6 run -e VUS=20 -e DURATION=1m order_creation_load.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = parseInt(__ENV.VUS || '10', 10);
const DURATION = __ENV.DURATION || '1m';

export const options = {
    scenarios: {
        order_creation: {
            executor: 'constant-vus',
            vus: VUS,
            duration: DURATION,
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.1'],
    },
};

const CUSTOMER_COUNT = parseInt(__ENV.CUSTOMER_COUNT || '500', 10);
const PRODUCT_COUNT = parseInt(__ENV.PRODUCT_COUNT || '100', 10);

export default function () {
    const customerId = 1 + Math.floor(Math.random() * CUSTOMER_COUNT);
    const itemCount = 1 + Math.floor(Math.random() * 3);
    const items = [];
    for (let i = 0; i < itemCount; i++) {
        items.push({
            productId: 1 + Math.floor(Math.random() * PRODUCT_COUNT),
            quantity: 1 + Math.floor(Math.random() * 5),
        });
    }

    const res = http.post(
        `${BASE_URL}/api/orders`,
        JSON.stringify({ customerId, items }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    check(res, { 'order created': (r) => r.status === 200 });

    sleep(0.5);
}
