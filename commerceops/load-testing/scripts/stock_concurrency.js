import http from 'k6/http';
import { check } from 'k6';

// Fires concurrent orders for the SAME product, each requesting a quantity
// close to the available stock, so that if none of them raced, at most one
// or two should succeed. StockService.reserve() is a plain read-check-write
// with no lock, so under real concurrency more orders can succeed than
// available stock allows - the goal here is to drive quantity_reserved past
// quantity_on_hand and see it in the stock endpoint afterward.
//
// Usage:
//   BASE_URL=http://localhost:8080 k6 run -e VUS=20 -e PRODUCT_ID=1 stock_concurrency.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = parseInt(__ENV.VUS || '20', 10);
const CUSTOMER_ID = parseInt(__ENV.CUSTOMER_ID || '1', 10);
const PRODUCT_ID = parseInt(__ENV.PRODUCT_ID || '1', 10);

export const options = {
    scenarios: {
        stock_race: {
            executor: 'shared-iterations',
            vus: VUS,
            iterations: VUS,
            maxDuration: '30s',
        },
    },
};

export function setup() {
    const before = http.get(`${BASE_URL}/api/products/${PRODUCT_ID}/stock`);
    if (before.status !== 200) {
        throw new Error(`setup: could not read stock for product ${PRODUCT_ID}: ${before.status}`);
    }
    const stock = JSON.parse(before.body);

    // Each VU asks for a bit more than (available / VUS), so the sum of all
    // requested quantities comfortably exceeds what's available - a correct
    // implementation should reject most of them.
    const perOrderQuantity = Math.max(1, Math.floor((stock.available / VUS) * 1.5));

    return { before: stock, perOrderQuantity };
}

export default function (data) {
    const res = http.post(
        `${BASE_URL}/api/orders`,
        JSON.stringify({
            customerId: CUSTOMER_ID,
            items: [{ productId: PRODUCT_ID, quantity: data.perOrderQuantity }],
        }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    check(res, { 'order accepted or rejected cleanly': (r) => r.status === 200 || r.status === 409 });
}

export function teardown(data) {
    const after = http.get(`${BASE_URL}/api/products/${PRODUCT_ID}/stock`);
    const stock = JSON.parse(after.body);
    console.log(
        `[stock_concurrency] product ${PRODUCT_ID}: before=${JSON.stringify(data.before)}, ` +
        `after=${JSON.stringify(stock)}, perOrderQuantity=${data.perOrderQuantity}, vus=${VUS}`
    );
    if (stock.quantityReserved > stock.quantityOnHand) {
        console.log('[stock_concurrency] OVERSOLD: quantityReserved exceeds quantityOnHand');
    }
}
