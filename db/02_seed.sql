-- ============================================================
-- EcommerceLibreria - Datos demo
-- Password admin: Admin123!   |   Password cliente demo: Cliente123!
-- (hashes generados con BCrypt, compatibles con Spring Security BCryptPasswordEncoder)
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
-- Sin credenciales reales aqui a proposito (este script se versiona en el repo). El ADMIN
-- las captura desde /api/admin/email-config una vez desplegado el sistema.
INSERT INTO email_config (enabled, smtp_host, smtp_port)
SELECT FALSE, 'smtp.gmail.com', 587
WHERE NOT EXISTS (SELECT 1 FROM email_config);
