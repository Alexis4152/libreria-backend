# EcommerceLibreria — Backend

API REST para una librería online, construida como evolución del stack técnico de **DemoPV-Backend** (mismo Java/Spring Boot/JWT/BCrypt/PostgreSQL), pero rediseñada para comercio electrónico en vez de punto de venta físico.

## Tecnologías

- **Java 17**, **Spring Boot 3.2.5**, Maven
- Spring Web, Spring Data JPA (Hibernate), Spring Security
- **PostgreSQL** (runtime), **H2** en memoria (solo pruebas)
- JWT: `io.jsonwebtoken` (jjwt) 0.11.5, `BCryptPasswordEncoder`
- Lombok
- JUnit 5 + `TestRestTemplate` (pruebas de integración contra la API real)

## Arquitectura

```
controller/  →  service/ (interfaz) + service/impl/  →  repository/  →  entity/
dto/request/ , dto/response/   (nunca se devuelven entities directamente)
mapper/                         (entity ↔ DTO, manual, sin MapStruct)
security/                       (JWT: filtro, provider, UserDetailsService)
payment/                        (PaymentProcessor + DummyPaymentProcessorImpl)
exception/                      (jerarquía propia + GlobalExceptionHandler)
```

Todas las entidades administrables (`Category`, `Author`, `Publisher`, `Book`, `Address`, `User`) extienden `AuditableEntity` (`@MappedSuperclass`): `isActive`, `createdBy/At`, `updatedBy/At`, `deletedBy/At`. Nunca se hace `DELETE` físico — todo es borrado lógico (`is_active = false`).

## Prerrequisitos

- JDK 17
- Maven 3.9+
- PostgreSQL 15+ corriendo localmente

## Configuración de PostgreSQL

```sql
CREATE DATABASE ecommerce_libreria;
```

Luego ejecuta, en orden, los scripts de `db/`:

```bash
psql -U postgres -d ecommerce_libreria -f db/01_schema.sql
psql -U postgres -d ecommerce_libreria -f db/02_seed.sql
```

`01_schema.sql` crea tablas, PKs, FKs, constraints e índices. `02_seed.sql` inserta roles, un usuario ADMIN y uno USER de prueba, categorías, autores, editoriales, 15 libros demo y la configuración inicial de la tienda.

## Variables de entorno

La app funciona con valores por defecto de desarrollo sin configurar nada, pero en producción **siempre** debes sobreescribir estas variables (nunca commitear secretos reales):

| Variable | Default (dev) | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ecommerce_libreria` | Cadena de conexión |
| `DB_USERNAME` | `postgres` | Usuario de BD |
| `DB_PASSWORD` | `admin` | Password de BD |
| `SERVER_PORT` | `8081` | Puerto HTTP |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5174,http://localhost:3000` | Orígenes permitidos (frontend) |
| `APP_JWT_SECRET` | clave de desarrollo | Secreto de firma JWT — **cambiar en producción** |
| `APP_JWT_EXPIRATION` | `86400000` (24h) | Expiración del token en ms |
| `APP_UPLOADS_DIR` | `uploads` | Carpeta de imágenes subidas (libros, logo) |
| `APP_PAYMENT_DUMMY_MODE` | `AUTO` | `AUTO` \| `ALWAYS_APPROVE` \| `ALWAYS_REJECT` |

## Configuración de correo (SMTP)

A diferencia del resto de la configuración, las credenciales SMTP **no** son una variable de entorno: viven en la tabla `email_config` (fila única) y se administran desde el panel admin (`/api/admin/email-config`, o la sección "Correo" de Configuración de la tienda en el Frontend). `EmailServiceImpl` arma el cliente SMTP a partir de esa tabla en cada envío, así que un cambio de credenciales aplica de inmediato, sin reiniciar el Backend.

Por default queda `enabled = false` (sin credenciales) para no comprometer nada al clonar el repo. Con Gmail, se necesita una cuenta con verificación en 2 pasos y un [App Password](https://myaccount.google.com/apppasswords) — la contraseña real nunca se devuelve por la API (el `GET` solo indica `passwordConfigured: true/false`) y dejar el campo de contraseña en blanco al guardar conserva la que ya estaba.

## Ejecución

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8081`. Las imágenes subidas se sirven en `http://localhost:8081/uploads/**`.

## Pruebas

```bash
mvn test
```

Pruebas de integración (JUnit 5 + `TestRestTemplate`, H2 en memoria) que cubren: registro (rol siempre USER), login válido/inválido, autorización por rol (USER no puede ejecutar operaciones de ADMIN aunque manipule la petición), CRUD y borrado lógico de libros, ajuste de stock, checkout con pago dummy aprobado/rechazado, cálculo de totales, actualización de inventario solo si el pago se aprueba, y transiciones válidas/ inválidas de estado de pedido.

## Usuarios de prueba (sembrados por `02_seed.sql`)

| Rol | Email | Password |
|---|---|---|
| ADMIN | `admin@libreria-demo.com` | `Admin123!` |
| USER | `cliente@demo.com` | `Cliente123!` |

## Flujo de compra (resumen)

1. `GET /api/public/books` — catálogo con búsqueda/filtros/paginación (sin autenticación).
2. `GET /api/public/books/{id}` — detalle de un libro.
3. `POST /api/checkout` — crea el pedido (invitado o autenticado), cobra con el procesador de pago dummy y, solo si se aprueba, descuenta inventario. El stock se valida y bloquea (`SELECT ... FOR UPDATE`) antes de crear el pedido para evitar sobreventa en compras concurrentes.
4. `GET /api/orders` / `GET /api/orders/{id}` — pedidos del usuario autenticado.
5. `GET/PATCH /api/admin/orders/**` — el ADMIN consulta y cambia el estado de cualquier pedido (transiciones validadas: no se puede pasar de `CANCELADO`/`ENTREGADO` a otro estado, ni saltar pasos).

## Endpoints principales

```
/api/auth/**              público   (register, login, me)
/api/public/**            público   (books, categories, authors, publishers, store-config)
/api/checkout             público   (permite compra como invitado)
/api/orders/**            autenticado (pedidos propios)
/api/users/**             autenticado (perfil, direcciones propias)
/api/admin/**             solo ADMIN (books, categories, authors, publishers, orders, customers, store-config, dashboard)
```

## Capa de pagos

`PaymentProcessor` es una interfaz; `DummyPaymentProcessorImpl` es la única implementación de esta versión (aprueba si el número de tarjeta termina en dígito par). Sustituirla por Stripe/Mercado Pago/PayPal/OpenPay no requiere tocar `CheckoutService` ni el modelo de `Order` — solo registrar otro bean de `PaymentProcessor`. Nunca se almacena el número completo de tarjeta ni el CVV, solo los últimos 4 dígitos y una marca simulada.
