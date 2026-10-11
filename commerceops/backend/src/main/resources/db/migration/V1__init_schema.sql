-- CommerceOps core schema

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE customers (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    email       VARCHAR(200),
    phone       VARCHAR(50),
    segment     VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_customers_name ON customers (name);
CREATE INDEX idx_customers_segment ON customers (segment);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    parent_id   BIGINT REFERENCES categories(id)
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    sku         VARCHAR(40) NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    unit_price  NUMERIC(14,2) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_category ON products (category_id);

CREATE TABLE stock_levels (
    product_id          BIGINT PRIMARY KEY REFERENCES products(id),
    quantity_on_hand     INTEGER NOT NULL DEFAULT 0,
    quantity_reserved    INTEGER NOT NULL DEFAULT 0,
    version              BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE stock_movements (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    delta           INTEGER NOT NULL,
    movement_type   VARCHAR(30) NOT NULL,
    reference_type  VARCHAR(30),
    reference_id    BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movements_product ON stock_movements (product_id);

CREATE TABLE contracts (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    reference   VARCHAR(40) NOT NULL UNIQUE,
    start_date  DATE NOT NULL,
    end_date    DATE,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    order_date      TIMESTAMP NOT NULL DEFAULT now(),
    total_amount    NUMERIC(14,2) NOT NULL DEFAULT 0,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_order_date ON orders (order_date);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id),
    quantity    INTEGER NOT NULL,
    unit_price  NUMERIC(14,2) NOT NULL,
    line_total  NUMERIC(14,2) NOT NULL
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);

CREATE TABLE invoices (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT REFERENCES orders(id),
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    invoice_number  VARCHAR(30) NOT NULL UNIQUE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    issue_date      TIMESTAMP NOT NULL DEFAULT now(),
    due_date        DATE NOT NULL,
    total_amount    NUMERIC(14,2) NOT NULL,
    paid_amount     NUMERIC(14,2) NOT NULL DEFAULT 0,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_customer ON invoices (customer_id);
-- intentionally no composite index on (status, due_date): the "unpaid invoices"
-- report query becomes a lab exercise for the LARGE profile.

CREATE TABLE invoice_lines (
    id          BIGSERIAL PRIMARY KEY,
    invoice_id  BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(300) NOT NULL,
    quantity    INTEGER NOT NULL,
    unit_price  NUMERIC(14,2) NOT NULL,
    line_total  NUMERIC(14,2) NOT NULL
);

CREATE INDEX idx_invoice_lines_invoice ON invoice_lines (invoice_id);

CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    invoice_id          BIGINT NOT NULL REFERENCES invoices(id),
    idempotency_key     VARCHAR(100) NOT NULL UNIQUE,
    amount              NUMERIC(14,2) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_reference  VARCHAR(100),
    attempt_count       INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_invoice ON payments (invoice_id);
CREATE INDEX idx_payments_status ON payments (status);

CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    type        VARCHAR(40) NOT NULL,
    payload     TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE audit_events (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(40) NOT NULL,
    entity_id   BIGINT NOT NULL,
    action      VARCHAR(40) NOT NULL,
    actor       VARCHAR(100),
    details     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_events_entity ON audit_events (entity_type, entity_id);

INSERT INTO roles (name) VALUES ('ADMIN'), ('SALES'), ('ACCOUNTING');
