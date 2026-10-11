import http from 'k6/http';
import { check } from 'k6';

// Fires concurrent payments against a SINGLE freshly-created invoice, each
// with a distinct idempotency key (so idempotency dedup does not mask the
// race). PaymentService.pay() applies invoice.paidAmount as a plain
// read-modify-write with no lock, so this is meant to demonstrate the lost
// update / overpay race, not just load the app.
//
// Usage:
//   BASE_URL=http://localhost:8080 k6 run -e VUS=20 payment_concurrency.js
//
// After the run, compare the logged "expected total paid" against the
// invoice's actual paidAmount (GET /api/invoices/{id}) - under the current
// implementation they will very likely not match.

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = parseInt(__ENV.VUS || '20', 10);
const CUSTOMER_ID = parseInt(__ENV.CUSTOMER_ID || '1', 10);
const PRODUCT_ID = parseInt(__ENV.PRODUCT_ID || '1', 10);

export const options = {
    scenarios: {
        payment_race: {
            executor: 'shared-iterations',
            vus: VUS,
            iterations: VUS,
            maxDuration: '30s',
        },
    },
};

export function setup() {
    const orderRes = http.post(
        `${BASE_URL}/api/orders`,
        JSON.stringify({ customerId: CUSTOMER_ID, items: [{ productId: PRODUCT_ID, quantity: 1000 }] }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    if (orderRes.status !== 200) {
        throw new Error(`setup: order creation failed with status ${orderRes.status}: ${orderRes.body}`);
    }
    const order = JSON.parse(orderRes.body);

    const invoiceRes = http.post(`${BASE_URL}/api/invoices/from-order/${order.id}`);
    if (invoiceRes.status !== 200) {
        throw new Error(`setup: invoice issuance failed with status ${invoiceRes.status}: ${invoiceRes.body}`);
    }
    const invoice = JSON.parse(invoiceRes.body);

    const perPaymentAmount = (invoice.totalAmount / VUS).toFixed(2);

    return { invoiceId: invoice.id, totalAmount: invoice.totalAmount, perPaymentAmount };
}

export default function (data) {
    const idempotencyKey = `k6-race-${data.invoiceId}-${__VU}-${__ITER}-${Date.now()}`;
    const res = http.post(
        `${BASE_URL}/api/payments`,
        JSON.stringify({
            invoiceId: data.invoiceId,
            amount: parseFloat(data.perPaymentAmount),
            idempotencyKey,
        }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    check(res, { 'payment accepted or rejected cleanly': (r) => r.status === 200 || r.status === 422 });
}

export function teardown(data) {
    const res = http.get(`${BASE_URL}/api/invoices/${data.invoiceId}`);
    const invoice = JSON.parse(res.body);
    const expectedIfNoRace = (parseFloat(data.perPaymentAmount) * VUS).toFixed(2);
    console.log(
        `[payment_concurrency] invoice ${data.invoiceId}: paidAmount=${invoice.paidAmount}, ` +
        `expected if every payment applied cleanly and none were rejected=${expectedIfNoRace} ` +
        `(status=${invoice.status})`
    );
}
