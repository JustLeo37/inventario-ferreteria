# Inventario Ferretería – API REST

API REST para gestionar el inventario y las ventas de una ferretería.
Curso: Desarrollo Web Integrado (100000ST62) – APF2, Semana 10.

**Tecnologías:** Java 21, Spring Boot 4.1.1, Spring Data JPA / Hibernate, Spring Security, JWT (jjwt 0.12.6), MySQL, Maven (wrapper incluido).

## Requisitos

- Java 21 (`java -version`)
- MySQL en `localhost:3306`
- Postman o Thunder Client (para las pruebas)

No hace falta instalar Maven: el proyecto incluye `mvnw` / `mvnw.cmd`.

## Configuración

1. Crear la base de datos vacía:

   ```sql
   CREATE DATABASE ferreteria;
   ```

2. Definir la contraseña del usuario `root` de MySQL como variable de entorno (la lee `application.properties`):

   - Windows (PowerShell):
     ```powershell
     $env:DB_PASSWORD="tu_clave_de_mysql"
     ```
   - Linux / macOS:
     ```bash
     export DB_PASSWORD="tu_clave_de_mysql"
     ```

3. (Opcional) Cambiar la clave de firma del JWT con la variable `JWT_SECRET` (mínimo 32 caracteres). Si no se define, se usa una clave de desarrollo. El token expira en 1 hora.

Las tablas (`categorias`, `productos`, `usuarios`, `ventas`, `detalle_venta`) las crea Hibernate automáticamente (`ddl-auto=update`).

## Ejecución

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`. Al primer arranque se crean dos usuarios de prueba:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `vendedor` | `vendedor123` | USER |

## Pruebas

**Automáticas (JUnit / Mockito):**

```bash
./mvnw test        # en Windows: .\mvnw.cmd test
```

**Manuales (Postman):**

1. Importar `postman/APF2_Inventario_Ferreteria_postman_collection.json`.
2. Con la app corriendo, ejecutar la colección completa (Runner) en este orden: Seguridad, CRUD y JPQL, Ventas. Los tokens y los ids se guardan solos en variables de la colección.
3. Para probar a mano: `POST /api/auth/login` y enviar el token en la cabecera `Authorization: Bearer <token>`.

## Seguridad y roles

| Ruta | Acceso |
|---|---|
| `/api/auth/**` (login, registro) | Público |
| `GET /api/**` | ADMIN y USER |
| `POST /api/ventas` | ADMIN y USER |
| `GET /api/ventas/reportes/**` | Solo ADMIN |
| `POST`, `PUT`, `DELETE` en `/api/**` (resto) | Solo ADMIN |

Sin token responde **401**; con un token de rol insuficiente responde **403**.

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Devuelve el JWT |
| POST | `/api/auth/register` | Registra un usuario |
| GET / POST | `/api/categorias` | Listar / crear categorías |
| GET / POST | `/api/productos` | Listar / crear productos |
| GET / PUT / DELETE | `/api/productos/{id}` | Consultar / actualizar / eliminar |
| GET | `/api/productos/paginado` | Paginación y orden |
| GET | `/api/productos/buscar`, `/categoria/{c}`, `/stock-bajo`, `/valor-inventario` | Consultas JPQL |
| POST | `/api/ventas` | Registra una venta (transacción: descuenta stock o revierte todo) |
| GET | `/api/ventas`, `/api/ventas/por-fechas` | Listado y filtro por fechas |
| GET | `/api/ventas/reportes/mas-vendidos`, `/reportes/por-vendedor` | Reportes JPQL (ADMIN) |

## Estructura

```
src/main/java/com/ferreteria/inventario/
  controller/   Controladores REST
  service/      Lógica de negocio y @Transactional
  repository/   Spring Data JPA y consultas JPQL (@Query)
  model/        Entidades JPA
  dto/          Objetos de petición y reporte
  security/     SecurityConfig, JwtUtil, JwtAuthFilter
  exception/    GlobalExceptionHandler
postman/        Colección de pruebas
```
