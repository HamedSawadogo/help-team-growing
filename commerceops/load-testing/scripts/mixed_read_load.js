import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// Mixed read load across the endpoints that matter for the perf lab:
// dashboard (heavy aggregation), customer search (pagination), order list
// (the N+1 endpoint), invoice detail (the subtler N+1), unpaid invoices
// (unindexed filter/sort). Point VUS/DURATION at 10/50/100/200 to compare.
//
// Usage:
//   BASE_URL=http://localhost:8080 k6 run -e VUS=50 -e DURATION=2m mixed_read_load.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = parseInt(__ENV.VUS || '10', 10);
const DURATION = __ENV.DURATION || '1m';

export const options = {
    scenarios: {
        mixed_read: {
            executor: 'constant-vus',
            vus: VUS,
            duration: DURATION,
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<2000'],
    },
};

const dashboardTrend = new Trend('dashboard_duration');
const orderListTrend = new Trend('order_list_duration');
const invoiceDetailTrend = new Trend('invoice_detail_duration');
const unpaidTrend = new Trend('unpaid_invoices_duration');

function randomPage(maxPage) {
    return Math.floor(Math.random() * maxPage);
}

export default function () {
    // Dashboard: heavy aggregation query, no pagination to tune.
    let res = http.get(`${BASE_URL}/api/dashboard/summary`);
    dashboardTrend.add(res.timings.duration);
    check(res, { 'dashboard 200': (r) => r.status === 200 });

    // Customer search: offset pagination over a potentially large table.
    res = http.get(`${BASE_URL}/api/customers?page=${randomPage(50)}&size=20`);
    check(res, { 'customers 200': (r) => r.status === 200 });

    // Product catalog browse.
    res = http.get(`${BASE_URL}/api/products?page=${randomPage(20)}&size=20`);
    check(res, { 'products 200': (r) => r.status === 200 });

    // Order list: triggers the N+1 (order.items -> item.product per row).
    res = http.get(`${BASE_URL}/api/orders?page=${randomPage(100)}&size=20`);
    orderListTrend.add(res.timings.duration);
    check(res, { 'orders 200': (r) => r.status === 200 });

    // Order detail for a pseudo-random id.
    const orderId = 1 + Math.floor(Math.random() * 900);
    res = http.get(`${BASE_URL}/api/orders/${orderId}`);
    check(res, { 'order detail 200 or 404': (r) => r.status === 200 || r.status === 404 });

    // Invoice list: triggers the subtler N+1 (invoice.order.orderDate per row).
    res = http.get(`${BASE_URL}/api/invoices?page=${randomPage(50)}&size=20`);
    check(res, { 'invoices 200': (r) => r.status === 200 });

    // Invoice detail: lines + remaining-due calculation.
    const invoiceId = 1 + Math.floor(Math.random() * 600);
    res = http.get(`${BASE_URL}/api/invoices/${invoiceId}`);
    invoiceDetailTrend.add(res.timings.duration);
    check(res, { 'invoice detail 200 or 404': (r) => r.status === 200 || r.status === 404 });

    // Unpaid invoices report: no composite index on (status, due_date).
    res = http.get(`${BASE_URL}/api/invoices/unpaid?page=0&size=20`);
    unpaidTrend.add(res.timings.duration);
    check(res, { 'unpaid invoices 200': (r) => r.status === 200 });

    // Payment search by invoice.
    res = http.get(`${BASE_URL}/api/payments?invoiceId=${invoiceId}&page=0&size=20`);
    check(res, { 'payments search 200': (r) => r.status === 200 });

    sleep(1);
}
