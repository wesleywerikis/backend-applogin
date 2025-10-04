# app-login-springboot-jwt

🔒 **API de autenticação** com **Spring Boot 3 (Java 17)**, **JWT**, **JPA**, **Flyway** e perfis para **H2 (dev)** e **MariaDB (prod)**.  
Inclui **cadastro** e **login**, DTOs validados, mapeamento com **MapStruct**, hashing de senha com **BCrypt** e um endpoint protegido `/api/users/me`.

> Pacote base: `br.com.applogin.backend_applogin`

---

## Sumário
- [Visão geral](#visão-geral)
- [Stack & dependências](#stack--dependências)
- [Arquitetura & camadas](#arquitetura--camadas)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Configuração (application.yml)](#configuração-applicationyml)
- [Banco & migrações (Flyway)](#banco--migrações-flyway)
- [Como rodar (dev H2)](#como-rodar-dev-h2)
- [Como rodar (prod MariaDB)](#como-rodar-prod-mariadb)
- [Endpoints](#endpoints)
- [Exemplos de requisição](#exemplos-de-requisição)
- [Erros & validação](#erros--validação)
- [Segurança (JWT)](#segurança-jwt)
- [Dicas de produção](#dicas-de-produção)
- [Roadmap](#roadmap)
- [Licença](#licença)

---

## Visão geral

Este projeto fornece uma base **enterprise-style** para autenticação de usuários com **Spring Security + JWT**:

- Cadastro de usuário com validação e hash de senha (**BCrypt**)
- Login por **username ou email**
- Geração de **JWT** com roles embutidas
- Endpoint protegido que retorna os dados do usuário autenticado
- Perfis: **dev/H2** para desenvolvimento rápido e **prod/MariaDB**
- **Flyway** para versionamento de esquema

---

## Stack & dependências

- **Java 17**, **Spring Boot 3.3.x**
- **Web, Security, Validation, Data JPA**
- **JWT (io.jsonwebtoken jjwt-api/impl/jackson)**  
- **JPA/Hibernate**, **H2** (dev), **MariaDB** (prod)
- **Flyway** (migrações)
- **Lombok** (boilerplate), **MapStruct** (mapper DTO ⇄ entidade)
- **Maven** (build)

Arquivo de build: [`pom.xml`](./pom.xml)

---

## Arquitetura & camadas

- **controller** – Endpoints REST, conversam com serviços e retornam DTOs.
- **service / service.impl** – Regras de negócio (cadastro, validação de unicidade, verificação de senha).
- **domain.entity** – Entidades JPA (`User`, `Role`).  
- **domain.repository** – Repositórios Spring Data (`UserRepository`, `RoleRepository`).
- **dto** – Objetos de transporte de dados para entrada/saída.
- **mapper** – MapStruct para converter **User → UserResponse**.
- **auth** – Utilitários de **JWT**.
- **exception** – Tratamento global de erros padronizados.
- **util** – Configurações utilitárias (ex.: `PasswordEncoderConfig`).

---

## Estrutura de pastas

```
src/main/java/br/com/applogin/backend_applogin
├─ BackendApploginApplication.java
├─ auth/
│  └─ JwtUtil.java
├─ controller/
│  ├─ AuthController.java          # /api/auth/register, /api/auth/login
│  └─ UserController.java          # /api/users/me (protegido)
├─ domain/
│  ├─ entity/
│  │  ├─ Role.java
│  │  └─ User.java
│  └─ repository/
│     ├─ RoleRepository.java
│     └─ UserRepository.java
├─ dto/
│  ├─ AuthLoginRequest.java
│  ├─ AuthRegisterRequest.java     
│  └─ UserResponse.java
├─ exception/
│  ├─ ApiError.java
│  └─ GlobalExceptionHandler.java
├─ mapper/
│  └─ UserMapper.java
├─ service/
│  ├─ UserService.java
│  └─ impl/
│     └─ UserServiceImpl.java
└─ util/
   └─ PasswordEncoderConfig.java

src/main/resources
├─ application.yml                  # perfis: dev (default) e prod
├─ application-dev.yml              # H2 + Flyway
└─ db/migration/V1__init.sql        # esquema + seed de roles
```

> **Nota:** a classe DTO de cadastro está como `AuthRegusterRequest` no código enviado. Funciona, mas se quiser renomear para `AuthRegisterRequest`, ajuste os imports e usos.

---

## Configuração (application.yml)

**`application.yml`** (trechos principais):

```yaml
server:
  port: 8080

spring:
  profiles:
    default: dev

app:
  jwt:
    secret: "x+IGkB92B98mKxe0QIi8+6eSQrjoGvvtjgHgt0SrIV+c+PbL+FpWd5jhNQv2ZJ6qhpJLA57SRkeHhdjs1koNHg=="
    expiration-minutes: 120

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mariadb://localhost:3306/enterprise_auth?createDatabaseIfNotExist=true
    username: root
    password: root
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate.ddl-auto: validate
    properties:
      hibernate.dialect: org.hibernate.dialect.MariaDBDialect
  flyway:
    enabled: true

app:
  cors:
    allowed-origins: "http://localhost:5500,http://127.0.0.1:5500"
```

**`application-dev.yml`** (H2 em memória):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:enterprise_auth;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password: sa
    driver-class-name: org.h2.Driver
  jpa:
    hibernate.ddl-auto: validate
    open-in-view: false
    properties:
      hibernate.format_sql: true
      hibernate.show_sql: true
  h2:
    console:
      enabled: true
      path: /h2
  flyway:
    enabled: true
```

---

## Banco & migrações (Flyway)

**`V1__init.sql`** cria o esquema mínimo e faz seed de roles:

```sql
CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

INSERT INTO roles(name) VALUES ('USER'), ('ADMIN');
```

---

## Como rodar (dev H2)

Pré‑requisitos: **JDK 17+** e **Maven**.

```bash
mvn spring-boot:run
# App: http://localhost:8080
# H2 console: http://localhost:8080/h2
# JDBC URL: jdbc:h2:mem:enterprise_auth   |  user: sa  |  pass: sa
```

> Flyway roda automaticamente no start e cria o esquema.

---

## Como rodar (prod MariaDB)

1. Suba/tenha um MariaDB acessível em `localhost:3306` (ou ajuste a URL).  
2. Exporte o perfil:

```bash
export SPRING_PROFILES_ACTIVE=prod   # no Windows: set SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

> **Dica:** para criar um banco docker rápido:
>
> ```bash
> docker run -d --name mariadb -e MARIADB_ROOT_PASSWORD=root -p 3306:3306 mariadb:11
> ```

---

## Endpoints

### Auth
- `POST /api/auth/register` – cadastra um usuário com role `USER`  
  **Body**: `{"username":"...", "email":"...", "password":"..."}`  
  **200 OK** → `UserResponse` (sem senha)

- `POST /api/auth/login` – login por username **ou** email  
  **Body**: `{"usernameOrEmail":"...", "password":"..."}`  
  **200 OK** → `{"token":"<JWT>","tokenType":"Bearer","expiresInSeconds":7200,"user":{...}}`

### Users (protegido – Bearer JWT)
- `GET /api/users/me` – retorna o usuário autenticado  
  **Headers**: `Authorization: Bearer <TOKEN>`  
  **200 OK** → `UserResponse`

---

## Exemplos de requisição

### Registro
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"wesley","email":"wesley@dev.com","password":"123456"}'
```

### Login
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"wesley","password":"123456"}'
```

### /users/me (protegido)
```bash
TOKEN="COLE_AQUI_O_TOKEN"
curl -s http://localhost:8080/api/users/me -H "Authorization: Bearer $TOKEN"
```

---

## Erros & validação

- **400 Bad Request** – violações de regra de negócio (ex.: username/email já usados).
- **422 Unprocessable Entity** – validações de DTO (ex.: senha curta, email inválido).
- **401 Unauthorized** – credenciais inválidas no login **ou** ausência/expiração de JWT.
- **500 Internal Server Error** – falhas não tratadas (ver `GlobalExceptionHandler`).

Formato padrão de erro:
```json
{ "message": "Descrição do erro" }
```

---

## Segurança (JWT)

- O segredo está em `app.jwt.secret` (perfil/config).  
- O token inclui `sub` (username) e `roles` (para autorização futura).  
- Expiração configurável por `app.jwt.expiration-minutes`.

> Gere um segredo robusto (base64) para produção. Exemplos:
> - Com `openssl`: `openssl rand -base64 64`
> - Ou qualquer gerador de 64+ bytes aleatórios

---

## Dicas de produção

- Usar **Cookie HTTPOnly** para refresh token.
- Ativar **Actuator** e logs estruturados.
- Configurar **CORS** estrito para o domínio do front.
- Aplicar **métricas e tracing** (Micrometer/OTEL).
- Sanitizar mensagens de exceção para não vazar detalhes.

---

## Roadmap

- [ ] Refresh token + rotação/blacklist  
- [ ] Recuperação de senha (token por e‑mail)  
- [ ] Verificação de e‑mail no cadastro  
- [ ] RBAC por endpoint (hasRole/hasAuthority)  
- [ ] Padronização de paginação e filtros (DTOs base)

---

## Licença

Este projeto é distribuído sob a licença **MIT**. Veja [LICENSE](LICENSE) (se aplicável).
