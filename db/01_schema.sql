-- ============================================================
-- EcommerceLibreria - Esquema de base de datos PostgreSQL
-- Convenciones: snake_case, BIGSERIAL PK, FKs <tabla>_id,
-- auditoria (created_at/by, updated_at/by, deleted_at/by, is_active),
-- enums como VARCHAR + CHECK (mismo patron que DemoPV-Backend)
-- ============================================================

-- ============ ROLES Y USUARIOS ============

CREATE TABLE IF NOT EXISTS roles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(30) NOT NULL UNIQUE CHECK (name IN ('ADMIN', 'USER')),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(30),
    role_id             BIGINT NOT NULL REFERENCES roles(id),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

CREATE TABLE IF NOT EXISTS addresses (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    label               VARCHAR(50),
    recipient_name      VARCHAR(150) NOT NULL,
    phone               VARCHAR(30) NOT NULL,
    address_line1       VARCHAR(255) NOT NULL,
    address_line2       VARCHAR(255),
    city                VARCHAR(100) NOT NULL,
    state               VARCHAR(100) NOT NULL,
    postal_code         VARCHAR(15) NOT NULL,
    country             VARCHAR(100) NOT NULL DEFAULT 'Mexico',
    is_default          BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);

-- ============ CATALOGOS ============

CREATE TABLE IF NOT EXISTS categories (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    slug                VARCHAR(120) NOT NULL UNIQUE,
    description         VARCHAR(500),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);

CREATE TABLE IF NOT EXISTS authors (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(150) NOT NULL,
    bio                 VARCHAR(1000),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_authors_name ON authors(name);

CREATE TABLE IF NOT EXISTS publishers (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(150) NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_publishers_name ON publishers(name);

-- ============ LIBROS ============

CREATE TABLE IF NOT EXISTS books (
    id                  BIGSERIAL PRIMARY KEY,
    sku                 VARCHAR(50) NOT NULL UNIQUE,
    isbn                VARCHAR(20),
    title               VARCHAR(300) NOT NULL,
    subtitle            VARCHAR(300),
    description_short   VARCHAR(500),
    description_long    TEXT,
    category_id         BIGINT NOT NULL REFERENCES categories(id),
    publisher_id        BIGINT REFERENCES publishers(id),
    price               NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    promo_price         NUMERIC(12,2) CHECK (promo_price >= 0),
    stock               INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    publication_year    INTEGER,
    page_count          INTEGER,
    language            VARCHAR(50),
    cover_type          VARCHAR(20) CHECK (cover_type IN ('PASTA_BLANDA', 'PASTA_DURA', 'DIGITAL')),
    width_cm            NUMERIC(6,2),
    height_cm           NUMERIC(6,2),
    depth_cm            NUMERIC(6,2),
    weight_grams        INTEGER,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    is_new              BOOLEAN NOT NULL DEFAULT FALSE,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id  BIGINT REFERENCES users(id),
    updated_at          TIMESTAMP,
    updated_by_user_id  BIGINT REFERENCES users(id),
    deleted_at          TIMESTAMP,
    deleted_by_user_id  BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_books_sku ON books(sku);
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_category_id ON books(category_id);
CREATE INDEX IF NOT EXISTS idx_books_publisher_id ON books(publisher_id);
CREATE INDEX IF NOT EXISTS idx_books_is_featured ON books(is_featured);

CREATE TABLE IF NOT EXISTS book_authors (
    book_id     BIGINT NOT NULL REFERENCES books(id),
    author_id   BIGINT NOT NULL REFERENCES authors(id),
    PRIMARY KEY (book_id, author_id)
);
CREATE INDEX IF NOT EXISTS idx_book_authors_author_id ON book_authors(author_id);

CREATE TABLE IF NOT EXISTS book_images (
    id          BIGSERIAL PRIMARY KEY,
    book_id     BIGINT NOT NULL REFERENCES books(id),
    url         VARCHAR(500) NOT NULL,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    alt_text    VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_book_images_book_id ON book_images(book_id);

-- ============ PEDIDOS ============

CREATE TABLE IF NOT EXISTS orders (
    id                      BIGSERIAL PRIMARY KEY,
    folio                   VARCHAR(30) NOT NULL UNIQUE,
    user_id                 BIGINT REFERENCES users(id),
    buyer_first_name        VARCHAR(100) NOT NULL,
    buyer_last_name         VARCHAR(100) NOT NULL,
    buyer_email             VARCHAR(150) NOT NULL,
    buyer_phone             VARCHAR(30) NOT NULL,
    shipping_address_line1  VARCHAR(255) NOT NULL,
    shipping_address_line2  VARCHAR(255),
    shipping_city           VARCHAR(100) NOT NULL,
    shipping_state          VARCHAR(100) NOT NULL,
    shipping_postal_code    VARCHAR(15) NOT NULL,
    shipping_country        VARCHAR(100) NOT NULL DEFAULT 'Mexico',
    subtotal                NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0),
    total                   NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                              CHECK (status IN ('PENDIENTE','PAGADO','PREPARANDO','ENVIADO','ENTREGADO','CANCELADO')),
    payment_status          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                              CHECK (payment_status IN ('PENDIENTE','APROBADO','RECHAZADO')),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_user_id      BIGINT REFERENCES users(id),
    updated_at              TIMESTAMP,
    updated_by_user_id      BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_orders_folio ON orders(folio);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

CREATE TABLE IF NOT EXISTS order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders(id),
    book_id     BIGINT NOT NULL REFERENCES books(id),
    sku         VARCHAR(50) NOT NULL,
    title       VARCHAR(300) NOT NULL,
    quantity    INTEGER NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    subtotal    NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0)
);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_book_id ON order_items(book_id);

CREATE TABLE IF NOT EXISTS order_status_history (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id),
    previous_status     VARCHAR(20),
    new_status          VARCHAR(20) NOT NULL,
    changed_by_user_id  BIGINT REFERENCES users(id),
    changed_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    note                VARCHAR(500)
);
CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON order_status_history(order_id);

CREATE TABLE IF NOT EXISTS payments (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id),
    method              VARCHAR(30) NOT NULL DEFAULT 'DUMMY_CARD',
    card_last4          VARCHAR(4),
    card_brand          VARCHAR(20),
    transaction_id      VARCHAR(60) NOT NULL,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('APROBADO','RECHAZADO')),
    authorization_code  VARCHAR(20),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);

-- ============ CONFIGURACION DE TIENDA ============

CREATE TABLE IF NOT EXISTS store_config (
    id                  BIGSERIAL PRIMARY KEY,
    store_name          VARCHAR(150) NOT NULL,
    legal_name          VARCHAR(150),
    rfc                 VARCHAR(20),
    address             VARCHAR(255),
    phone               VARCHAR(30),
    email               VARCHAR(150),
    logo_url            VARCHAR(500),
    favicon_url         VARCHAR(500),
    primary_color       VARCHAR(10) NOT NULL DEFAULT '#155DEA',
    secondary_color     VARCHAR(10) NOT NULL DEFAULT '#0F172A',
    welcome_message     VARCHAR(500),
    ticket_message      VARCHAR(500),
    social_links        JSONB,
    shipping_info       VARCHAR(500),
    footer_text         VARCHAR(500),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by_user_id  BIGINT REFERENCES users(id)
);

-- ============ CONFIGURACION DE CORREO (SMTP) ============
-- Fila unica, editable desde /api/admin/email-config. La contrasena NUNCA se devuelve
-- al frontend (solo un booleano "passwordConfigured"); se guarda en texto plano aqui
-- porque el propio Backend la necesita para autenticarse contra el SMTP en cada envio,
-- igual que cualquier credencial de servicio administrada en BD.
CREATE TABLE IF NOT EXISTS email_config (
    id                  BIGSERIAL PRIMARY KEY,
    enabled             BOOLEAN NOT NULL DEFAULT FALSE,
    smtp_host           VARCHAR(255) NOT NULL DEFAULT 'smtp.gmail.com',
    smtp_port           INTEGER NOT NULL DEFAULT 587,
    smtp_username       VARCHAR(150),
    smtp_password       VARCHAR(255),
    from_address        VARCHAR(150),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by_user_id  BIGINT REFERENCES users(id)
);
