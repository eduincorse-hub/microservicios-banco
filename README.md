# Reto Técnico — Arquitectura de Microservicios (Nivel Senior)

Sistema bancario implementado con **dos microservicios Spring Boot** que se comunican de forma **asíncrona mediante RabbitMQ**.

| Microservicio | Responsabilidad | Puerto | Base de datos |
|---|---|---|---|
| `ms-clientes` | Persona y Cliente | 8080 | `db_banco_clientes` |
| `ms-cuentas` | Cuenta, Movimientos y Reportes | 8081 | `db_banco_cuentas` |

---

## Arquitectura

```
                  ┌──────────────────┐   cliente.creado / actualizado / eliminado   ┌──────────────────────┐
 HTTP :8080 ───►  │   ms-clientes    │ ──────────► RabbitMQ (clientes.exchange) ──► │     ms-cuentas       │ ◄─── HTTP :8081
                  │ Persona, Cliente │             topic + DLQ + reintentos         │ Cuenta, Movimiento,  │
                  └────────┬─────────┘                                              │ Reportes, réplica de │
                           │                                                        │ clientes             │
                           ▼                                                        └──────────┬───────────┘
                  db_banco_clientes  ◄──────────────  MySQL 8  ──────────────►       db_banco_cuentas
```

- **Base de datos por servicio**: cada microservicio es dueño de su esquema; no existen FK entre bases.
- **Comunicación asíncrona**: `ms-clientes` publica eventos de dominio (`cliente.creado`, `cliente.actualizado`, `cliente.eliminado`) en un exchange tipo *topic*.
- **Réplica local de clientes**: `ms-cuentas` mantiene una copia mínima del cliente (id, nombre, estado) alimentada por esos eventos. Puede crear cuentas y generar reportes **aunque `ms-clientes` esté caído** (consistencia eventual).
- **Eventos publicados después del commit** (`@TransactionalEventListener(AFTER_COMMIT)`): nunca se anuncia un cambio que terminó en rollback.
- **Eliminación de un cliente**: `ms-cuentas` desactiva la réplica y sus cuentas, conservando el historial de movimientos.

---

## Tecnologías

Java 21 · Spring Boot 4 · Spring Data JPA (Hibernate) · Bean Validation · MapStruct · Lombok · MySQL 8 · RabbitMQ 4 · JUnit 5 · Mockito · Testcontainers · Docker / Docker Compose · Postman

---

## Ejecución

**Requisito:** Docker Desktop.

```bash
docker compose up -d --build
docker compose ps        # esperar a que los 4 contenedores estén "healthy"
```

| Servicio | URL |
|---|---|
| ms-clientes | http://localhost:8080/api |
| ms-cuentas | http://localhost:8081/api |
| RabbitMQ (consola) | http://localhost:15672 (admin / admin) |
| MySQL | localhost:3307 (root / root) |

El esquema de base de datos se crea automáticamente con [`BaseDatos.sql`](BaseDatos.sql) al iniciar el contenedor de MySQL. Hibernate solo **valida** el esquema (`ddl-auto: validate`); no lo modifica.

Para reiniciar todo desde cero:

```bash
docker compose down -v
docker compose up -d --build
```

---

## Validación con Postman

1. Importar la colección [`postman/cliente-cuenta-Microservicios.postman_collection.json`](postman/).
2. Ejecutarla completa con el **Collection Runner**, configurando un **delay de 500 ms** entre peticiones.

> El delay permite que los eventos de RabbitMQ sincronicen la réplica de clientes en `ms-cuentas` antes de crear sus cuentas (consistencia eventual).

La colección reproduce en orden los casos de uso del enunciado:

1. Creación de clientes (Jose Lema, Marianela Montalvo, Juan Osorio).
2. Creación de cuentas (478758, 225487, 495878, 496825).
3. Nueva cuenta corriente para Jose Lema (585545).
4. Movimientos (retiro 575, depósito 600, depósito 150, retiro 540).
5. Reporte de estado de cuenta de Marianela Montalvo.

Además incluye el CRUD completo de cada recurso y los casos de error (duplicados, datos inválidos, recursos inexistentes y **"Saldo no disponible"**). Cada petición tiene pruebas automáticas.

---

## Endpoints

| Recurso | Verbos | Descripción |
|---|---|---|
| `/api/clientes` | GET, POST, PUT, PATCH, DELETE | CRUD de clientes (F1) |
| `/api/cuentas` | GET, POST, PUT, PATCH, DELETE | CRUD de cuentas (F1). Filtro opcional `?clienteId=` |
| `/api/movimientos` | GET, POST, PUT, DELETE | Registro de movimientos (F1, F2, F3). Filtro opcional `?numeroCuenta=` |
| `/api/reportes?fecha={inicio},{fin}&cliente={clienteId}` | GET | Estado de cuenta (F4). Fechas en formato `yyyy-MM-dd` |

### Clientes

- `Cliente` hereda de `Persona` (herencia JPA `JOINED`): `persona` tiene su PK y `cliente` comparte esa PK como FK.
- `clienteId` es la clave de negocio (UUID generado por el sistema, inmutable). El `id` interno nunca se expone.
- La contraseña se guarda con **BCrypt** y **nunca** se devuelve en las respuestas.
- `PUT` reemplaza el recurso completo; `PATCH` actualiza solo los campos enviados.

### Cuentas

- El tipo de cuenta acepta `Ahorro`, `Ahorros` o `Corriente` (sin importar mayúsculas) y se normaliza a `AHORROS` / `CORRIENTE`.
- Solo se pueden editar el **tipo** y el **estado**. El número, el saldo inicial y el cliente dueño son inmutables.
- No se puede eliminar una cuenta con movimientos: debe desactivarse con `PATCH`.

### Movimientos (F2 y F3)

- El **signo del valor** define el tipo: positivo = `DEPOSITO`, negativo = `RETIRO`.
- Cada movimiento actualiza el saldo disponible de la cuenta y guarda el **saldo resultante**.
- Si el retiro deja el saldo por debajo de 0 se responde **HTTP 422** con el mensaje **`"Saldo no disponible"`**, incluyendo el saldo disponible y el valor solicitado.
- La regla vive en la entidad `Cuenta` (modelo de dominio rico).
- La fecha la asigna el servidor (`Clock` con zona `America/Guayaquil`).
- Concurrencia controlada con **bloqueo pesimista** (`SELECT ... FOR UPDATE`) sobre la cuenta: dos retiros simultáneos no pueden dejar el saldo negativo.
- Solo se puede editar o eliminar el **último movimiento** de una cuenta. Los anteriores se corrigen registrando un movimiento de ajuste, para no romper los saldos históricos.

Ejemplo de respuesta de F3:

```json
{
  "title": "Saldo no disponible",
  "status": 422,
  "detail": "Saldo no disponible",
  "numeroCuenta": 495878,
  "saldoDisponible": 150.00,
  "valorSolicitado": -200,
  "timestamp": "2026-10-11T21:04:37Z"
}
```

### Reporte (F4)

```
GET /api/reportes?fecha=2026-10-01,2026-10-11&cliente={clienteId}
```

Respuesta agrupada **cliente → cuentas (con sus saldos) → movimientos**:

```json
{
  "clienteId": "d4f49a45-...",
  "cliente": "Marianela Montalvo",
  "fechaInicio": "2026-10-01",
  "fechaFin": "2026-10-11",
  "cuentas": [
    {
      "numeroCuenta": 225487,
      "tipo": "CORRIENTE",
      "saldoInicial": 100.00,
      "estado": true,
      "saldoDisponible": 700.00,
      "movimientos": [
        { "fecha": "2026-10-11T16:44:12", "tipoMovimiento": "DEPOSITO", "movimiento": 600.00, "saldoDisponible": 700.00 }
      ]
    },
    {
      "numeroCuenta": 496825,
      "tipo": "AHORROS",
      "saldoInicial": 540.00,
      "estado": true,
      "saldoDisponible": 0.00,
      "movimientos": [
        { "fecha": "2026-10-11T16:44:13", "tipoMovimiento": "RETIRO", "movimiento": -540.00, "saldoDisponible": 0.00 }
      ]
    }
  ]
}
```

- Una cuenta sin movimientos en el rango aparece igualmente, con su lista vacía.
- El día final del rango se incluye completo.
- Se resuelve con **2 consultas** (sin problema N+1), apoyadas por el índice `(numero_cuenta, fecha)`.
- El nombre del cliente se obtiene de la réplica local: el reporte no depende de `ms-clientes`.

---

## Manejo de errores

Todas las respuestas de error usan el formato estándar **`ProblemDetail` (RFC 9457)**, generado por un `@RestControllerAdvice` global en cada microservicio:

| Código | Situación |
|---|---|
| 400 | Datos inválidos (con detalle por campo), JSON mal formado, parámetros inválidos |
| 404 | Recurso inexistente |
| 409 | Recurso duplicado |
| 422 | Regla de negocio: saldo no disponible, cuenta o cliente inactivo, cuenta con historial, cliente no sincronizado |
| 500 | Error inesperado (sin exponer detalles internos; el detalle se registra en el log) |

---

## Pruebas

```bash
cd ms-clientes && ./mvnw test
cd ms-cuentas  && ./mvnw test     # requiere Docker en ejecución (Testcontainers)
```

| Tipo | Clase | Requisito |
|---|---|---|
| Unitaria de dominio | `ClienteTest` | **F5** |
| Unitaria de dominio | `CuentaTest` (reglas de saldo) | F2 / F3 |
| Unitaria de endpoint | `ClienteControllerTest`, `MovimientoControllerTest` | Pruebas de endpoints |
| Integración | `FlujoCuentasIntegrationTest` | **F6** |

La prueba de integración levanta **MySQL 8 y RabbitMQ reales** con Testcontainers, carga el mismo `BaseDatos.sql` y recorre el flujo completo: evento de cliente por RabbitMQ → creación de cuenta → retiro → "Saldo no disponible" → reporte.

---

## Estructura de cada microservicio

```
controller/   → endpoints REST (sin lógica de negocio)
service/      → interfaces + impl/ (reglas de negocio y transacciones)
repository/   → Spring Data JPA (patrón Repository)
entity/       → modelo de dominio rico (las reglas viven en las entidades)
dto/          → contratos de la API (records)
mapper/       → MapStruct (entidad ↔ DTO)
messaging/    → publicación / consumo de eventos RabbitMQ
exception/    → excepciones de negocio + manejador global
config/       → configuración (RabbitMQ, reloj, BCrypt)
```

```
.
├── BaseDatos.sql
├── docker-compose.yml
├── postman/
│   └── cliente-cuenta-Microservicios.postman_collection.json
├── ms-clientes/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── ms-cuentas/
    ├── Dockerfile
    ├── pom.xml
    └── src/
```

---

## Factores no funcionales

### Rendimiento
- Índices en las claves de búsqueda y en `movimiento(numero_cuenta, fecha)`.
- `@EntityGraph` / `JOIN FETCH` para evitar el problema N+1.
- `open-in-view: false` y transacciones `readOnly` en las consultas.
- Mapeo con MapStruct (código generado en compilación, sin reflexión).

### Escalabilidad
- Servicios sin estado: pueden escalar horizontalmente.
- Base de datos por servicio y comunicación por eventos (bajo acoplamiento).
- Exchange tipo *topic*: nuevos consumidores pueden suscribirse sin modificar el productor.

### Resiliencia
- Colas durables: si `ms-cuentas` se detiene, los eventos esperan en la cola y se procesan al volver.
- Reintentos con *backoff* exponencial y **Dead Letter Queue** para mensajes que fallan repetidamente.
- Consumidor **idempotente** (upsert) ante entregas duplicadas.
- Healthchecks con Actuator y `depends_on: service_healthy` en Docker Compose.

### Seguridad
- Contraseñas cifradas con **BCrypt**; nunca se exponen en la API.
- Contenedores ejecutados con usuario no root.
- Validación de entrada con Bean Validation y restricciones en la base de datos.

---

## Despliegue (F7)

- **Dockerfile multi-stage** por microservicio: una etapa compila con Maven y la imagen final contiene solo el JRE y el `.jar`.
- Configuración por **variables de entorno**: el mismo artefacto corre en local y en contenedores.
- `docker-compose.yml` orquesta MySQL, RabbitMQ y ambos microservicios en una red interna.
- `ms-cuentas` no depende de `ms-clientes` para arrancar, reflejando el desacoplamiento de la comunicación asíncrona.

---

## Mejoras propuestas

- **Patrón Outbox** para garantizar la entrega de eventos aunque RabbitMQ no esté disponible al momento del commit.
- **API Gateway** (Spring Cloud Gateway) como punto único de entrada.
- Autenticación y autorización con **OAuth2 / JWT**.
- Paginación en los listados.
- Observabilidad: trazas distribuidas (OpenTelemetry) y métricas (Prometheus / Grafana).
- Orquestación en Kubernetes para escalado automático.
