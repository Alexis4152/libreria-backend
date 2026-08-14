-- ============================================================
-- EcommerceLibreria - Script de inicializacion (schema + seed)
-- Combina 01_schema.sql + 02_seed.sql para correrlo de una sola vez
-- contra la base de datos ya creada por el recurso Postgres de Coolify.
-- Idempotente: usa IF NOT EXISTS / ON CONFLICT, se puede re-ejecutar sin duplicar datos.
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

-- ============================================================
-- SEED: datos demo
-- Password admin: Admin123!   |   Password cliente demo: Cliente123!
-- ============================================================

INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('USER') ON CONFLICT (name) DO NOTHING;

INSERT INTO users (email, password_hash, first_name, last_name, phone, role_id, is_active)
SELECT 'admin@libreria-demo.com', '$2b$10$nwHCWxjudXLk2xshHHyrpeyPamrIJ/TVxRCWEYHETsAcYf.koWqCO',
       'Admin', 'Libreria', '5555550100', r.id, TRUE
FROM roles r WHERE r.name = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@libreria-demo.com');

INSERT INTO users (email, password_hash, first_name, last_name, phone, role_id, is_active)
SELECT 'cliente@demo.com', '$2b$10$1H9H17NH8WIGTbIVRurtQeXxVeCWExR0PNJCwCPHmJginz/g7K4fG',
       'Cliente', 'Demo', '5555550200', r.id, TRUE
FROM roles r WHERE r.name = 'USER'
AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'cliente@demo.com');

-- ============ CATEGORIAS ============
INSERT INTO categories (name, slug, description) VALUES
 ('Literatura', 'literatura', 'Novela, cuento y poesia'),
 ('Infantil', 'infantil', 'Libros para ninas y ninos'),
 ('Medicina', 'medicina', 'Ciencias de la salud'),
 ('Tecnologia', 'tecnologia', 'Programacion y sistemas'),
 ('Historia', 'historia', 'Historia universal y de Mexico'),
 ('Negocios', 'negocios', 'Administracion y finanzas'),
 ('Psicologia', 'psicologia', 'Psicologia y desarrollo personal'),
 ('Arte', 'arte', 'Arte y diseno')
ON CONFLICT (slug) DO NOTHING;

-- ============ AUTORES ============
INSERT INTO authors (name) VALUES
 ('Gabriel Garcia Marquez'), ('Isabel Allende'), ('Yuval Noah Harari'),
 ('Robert C. Martin'), ('Daniel Kahneman'), ('J.K. Rowling'),
 ('Mario Vargas Llosa'), ('Eric Ries'), ('Andrew Ng'), ('Julia Cameron')
ON CONFLICT DO NOTHING;

-- ============ EDITORIALES ============
INSERT INTO publishers (name) VALUES
 ('Planeta'), ('Penguin Random House'), ('O''Reilly Media'),
 ('Debate'), ('Salamandra'), ('Alfaguara')
ON CONFLICT DO NOTHING;

-- ============ LIBROS DEMO ============
INSERT INTO books (sku, isbn, title, subtitle, description_short, description_long, category_id, publisher_id,
                    price, promo_price, stock, publication_year, page_count, language, cover_type,
                    width_cm, height_cm, depth_cm, weight_grams, is_featured, is_new, status)
SELECT v.sku, v.isbn, v.title, v.subtitle, v.desc_short, v.desc_long,
       c.id, p.id, v.price, v.promo_price, v.stock, v.pub_year, v.pages, v.lang, v.cover,
       v.w, v.h, v.d, v.weight, v.featured, v.is_new, 'ACTIVE'
FROM (VALUES
 ('LIB-0001','9780307474728','Cien anos de soledad', NULL,
  'La obra maestra del realismo magico.', 'Cien anos de soledad narra la historia de la familia Buendia a lo largo de siete generaciones en el pueblo ficticio de Macondo.',
  'Literatura','Alfaguara', 349.00, 299.00, 25, 1967, 471, 'Espanol', 'PASTA_BLANDA', 13.0, 21.0, 3.0, 450, TRUE, FALSE),
 ('LIB-0002','9788401352836','La casa de los espiritus', NULL,
  'Saga familiar chilena marcada por lo sobrenatural.', 'Una de las novelas fundacionales del boom latinoamericano, narrada a traves de varias generaciones de la familia Trueba.',
  'Literatura','Debate', 329.00, NULL, 18, 1982, 448, 'Espanol', 'PASTA_BLANDA', 13.0, 21.0, 3.0, 420, TRUE, FALSE),
 ('LIB-0003','9780062316097','Sapiens: De animales a dioses', 'Una breve historia de la humanidad',
  'Un recorrido por la historia de nuestra especie.', 'Sapiens explora como Homo sapiens llego a dominar el planeta a traves de revoluciones cognitivas, agricolas y cientificas.',
  'Historia','Debate', 449.00, 399.00, 30, 2011, 496, 'Espanol', 'PASTA_DURA', 15.0, 23.0, 4.0, 650, TRUE, TRUE),
 ('LIB-0004','9780134685991','Effective Java', NULL,
  'Guia esencial de mejores practicas en Java.', 'Un compendio de 90 items sobre como escribir codigo Java robusto, mantenible y eficiente.',
  'Tecnologia','O''Reilly Media', 899.00, NULL, 12, 2018, 412, 'Ingles', 'PASTA_BLANDA', 18.0, 23.5, 3.5, 700, FALSE, FALSE),
 ('LIB-0005','9780132350884','Clean Code', 'A Handbook of Agile Software Craftsmanship',
  'Como escribir codigo limpio y mantenible.', 'Robert C. Martin presenta principios, patrones y practicas para escribir buen software.',
  'Tecnologia','Penguin Random House', 799.00, 699.00, 20, 2008, 464, 'Ingles', 'PASTA_BLANDA', 18.0, 23.5, 3.0, 680, TRUE, FALSE),
 ('LIB-0006','9780374533557','Pensar rapido, pensar despacio', NULL,
  'Los dos sistemas que gobiernan nuestra mente.', 'Daniel Kahneman explica el sistema rapido/intuitivo y el sistema lento/racional que gobiernan nuestras decisiones.',
  'Psicologia','Debate', 429.00, NULL, 15, 2011, 512, 'Espanol', 'PASTA_BLANDA', 15.0, 23.0, 3.5, 600, FALSE, FALSE),
 ('LIB-0007','9788478886459','Harry Potter y la piedra filosofal', NULL,
  'El inicio de la saga magica mas famosa.', 'Harry Potter descubre en su onceavo cumpleanos que es un mago y es invitado a estudiar en Hogwarts.',
  'Infantil','Salamandra', 379.00, 329.00, 40, 1997, 254, 'Espanol', 'PASTA_DURA', 15.0, 23.0, 2.5, 400, TRUE, FALSE),
 ('LIB-0008','9788420471839','La ciudad y los perros', NULL,
  'Novela sobre la vida en un colegio militar limeno.', 'La primera novela de Mario Vargas Llosa, ambientada en el colegio militar Leoncio Prado.',
  'Literatura','Alfaguara', 359.00, NULL, 10, 1963, 419, 'Espanol', 'PASTA_BLANDA', 13.0, 21.0, 3.0, 430, FALSE, FALSE),
 ('LIB-0009','9780307887894','El metodo Lean Startup', NULL,
  'Como crear empresas de exito con innovacion continua.', 'Eric Ries presenta un enfoque cientifico para crear y administrar startups exitosas.',
  'Negocios','Debate', 459.00, 399.00, 22, 2011, 336, 'Espanol', 'PASTA_BLANDA', 15.0, 23.0, 2.5, 480, TRUE, TRUE),
 ('LIB-0010','9781492041139','Deep Learning con Python', NULL,
  'Introduccion practica al aprendizaje profundo.', 'Fundamentos y aplicaciones practicas de redes neuronales usando Python y frameworks modernos.',
  'Tecnologia','O''Reilly Media', 950.00, NULL, 8, 2021, 528, 'Ingles', 'PASTA_BLANDA', 18.0, 23.5, 4.0, 780, FALSE, TRUE),
 ('LIB-0011','9780743509998','El camino del artista', NULL,
  'Un curso para descubrir y recuperar tu creatividad.', 'Julia Cameron ofrece un programa de doce semanas para liberar la creatividad personal.',
  'Psicologia','Planeta', 389.00, NULL, 16, 1992, 288, 'Espanol', 'PASTA_BLANDA', 13.5, 21.0, 2.0, 380, FALSE, FALSE),
 ('LIB-0012','9788434427094','Breve historia de casi todo', NULL,
  'Un recorrido ameno por la ciencia.', 'Bill... es decir, un recorrido divulgativo por los grandes descubrimientos cientificos de la humanidad.',
  'Historia','Planeta', 419.00, 379.00, 14, 2003, 624, 'Espanol', 'PASTA_DURA', 15.5, 23.0, 4.5, 820, FALSE, FALSE),
 ('LIB-0013','9780500204337','Historia del arte', NULL,
  'La historia del arte contada de forma accesible.', 'Un clasico de divulgacion sobre la historia del arte occidental, desde las cuevas hasta el arte moderno.',
  'Arte','Penguin Random House', 599.00, NULL, 9, 1950, 688, 'Espanol', 'PASTA_DURA', 17.0, 24.0, 5.0, 1200, FALSE, FALSE),
 ('LIB-0014','9788499086044','Anatomia para el estudiante de medicina', NULL,
  'Manual ilustrado de anatomia humana.', 'Texto de referencia con ilustraciones detalladas del cuerpo humano para estudiantes de medicina.',
  'Medicina','Planeta', 1250.00, 1099.00, 6, 2019, 960, 'Espanol', 'PASTA_DURA', 21.0, 27.5, 6.0, 2100, FALSE, FALSE),
 ('LIB-0015','9788408173777','Fisiopatologia clinica basica', NULL,
  'Fundamentos de fisiopatologia para clinicos.', 'Un compendio de los mecanismos fisiopatologicos de las enfermedades mas comunes.',
  'Medicina','Debate', 980.00, NULL, 0, 2020, 540, 'Espanol', 'PASTA_BLANDA', 18.0, 24.0, 3.5, 900, FALSE, FALSE)
) AS v(sku, isbn, title, subtitle, desc_short, desc_long, cat_name, pub_name, price, promo_price, stock,
       pub_year, pages, lang, cover, w, h, d, weight, featured, is_new)
JOIN categories c ON c.name = v.cat_name
JOIN publishers p ON p.name = v.pub_name
WHERE NOT EXISTS (SELECT 1 FROM books b WHERE b.sku = v.sku);

-- Relacion libro-autor (algunas asociaciones demostrativas)
INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id FROM books b, authors a
WHERE (b.sku='LIB-0001' AND a.name='Gabriel Garcia Marquez')
   OR (b.sku='LIB-0002' AND a.name='Isabel Allende')
   OR (b.sku='LIB-0003' AND a.name='Yuval Noah Harari')
   OR (b.sku='LIB-0004' AND a.name='Robert C. Martin')
   OR (b.sku='LIB-0005' AND a.name='Robert C. Martin')
   OR (b.sku='LIB-0006' AND a.name='Daniel Kahneman')
   OR (b.sku='LIB-0007' AND a.name='J.K. Rowling')
   OR (b.sku='LIB-0008' AND a.name='Mario Vargas Llosa')
   OR (b.sku='LIB-0009' AND a.name='Eric Ries')
   OR (b.sku='LIB-0010' AND a.name='Andrew Ng')
   OR (b.sku='LIB-0011' AND a.name='Julia Cameron')
ON CONFLICT DO NOTHING;

-- Imagen principal placeholder para cada libro demo
INSERT INTO book_images (book_id, url, is_primary, sort_order, alt_text)
SELECT b.id, '/uploads/books/placeholder.svg', TRUE, 0, b.title
FROM books b
WHERE NOT EXISTS (SELECT 1 FROM book_images bi WHERE bi.book_id = b.id);

-- ============ CONFIGURACION DE TIENDA (fila unica) ============
INSERT INTO store_config (store_name, legal_name, rfc, address, phone, email,
                           primary_color, secondary_color, welcome_message, ticket_message,
                           social_links, shipping_info, footer_text)
SELECT 'Libreria Online Demo', 'Libreria Online Demo S.A. de C.V.', 'LOD010101AAA',
       'Av. Reforma 123, Ciudad de Mexico', '5555550000', 'contacto@libreria-demo.com',
       '#155DEA', '#0F172A',
       'Bienvenido a tu libreria online de confianza',
       'Gracias por tu compra en Libreria Online Demo',
       '{"facebook":"","instagram":"","twitter":""}'::jsonb,
       'Envios a todo Mexico en 3-5 dias habiles',
       'Libreria Online Demo - Todos los derechos reservados'
WHERE NOT EXISTS (SELECT 1 FROM store_config);

-- ============ CONFIGURACION DE CORREO (fila unica, deshabilitada por default) ============
INSERT INTO email_config (enabled, smtp_host, smtp_port)
SELECT FALSE, 'smtp.gmail.com', 587
WHERE NOT EXISTS (SELECT 1 FROM email_config);
