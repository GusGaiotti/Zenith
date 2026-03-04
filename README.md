# Zenith

Aplicação de finanças compartilhadas para casal, organizada em monorepo com backend em Spring Boot e frontend em Next.js.

## Stack

- `backend/`: Java 21, Spring Boot 3.5, Spring Security, JPA, Flyway e PostgreSQL
- `frontend/`: Next.js 16, React 19, TypeScript, Axios, Zustand e TanStack Query
- `docker-compose.yml`: PostgreSQL local para desenvolvimento

## Funcionalidades

- cadastro e login
- sessão com access token + refresh token em cookie
- ledger compartilhado para até duas pessoas
- categorias e transações
- dashboard com resumo, tendências e divisão por categoria
- convite para entrar em um ledger

## Requisitos

- Docker com Compose
- Java 21
- Node.js 22
- npm

## Como rodar localmente

### 1. Suba o banco local

Na raiz do projeto:

```bash
docker compose up -d
```

Valores padrão do banco local:

- host: `localhost`
- porta: `5432`
- banco: `zenith_dev`
- usuário: `postgres`
- senha: `postgres`

Para parar o banco:

```bash
docker compose down
```

Para remover o volume local:

```bash
docker compose down -v
```

### 2. Configure o backend

Crie `backend/.env` a partir de `backend/.env.example`.

Exemplo:

```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

DB_URL=jdbc:postgresql://localhost:5432/zenith_dev
DB_USERNAME=postgres
DB_PASSWORD=change-me

JWT_SECRET=replace-with-a-base64-secret-at-least-32-bytes-when-decoded
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

CORS_ALLOWED_ORIGINS=http://localhost:3000

AUTH_REFRESH_COOKIE_NAME=refresh_token
AUTH_REFRESH_COOKIE_PATH=/api/v1/auth
AUTH_REFRESH_COOKIE_SECURE=false
AUTH_REFRESH_COOKIE_SAME_SITE=Lax
AUTH_REFRESH_COOKIE_DOMAIN=
```

Observações:

- `JWT_SECRET` precisa ser Base64 válido
- a chave decodificada deve ter pelo menos 32 bytes
- em ambiente local, `AUTH_REFRESH_COOKIE_SECURE=false` é o comportamento esperado

### 3. Inicie o backend

Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
cd backend
chmod +x ./mvnw
./mvnw spring-boot:run
```

Se tudo estiver certo:

- a API sobe em `http://localhost:8080`
- o Flyway aplica as migrations automaticamente
- o health check fica em `http://localhost:8080/actuator/health`

### 4. Configure o frontend

Crie `frontend/.env` a partir de `frontend/.env.example`.

Exemplo:

```env
API_URL=http://localhost:8080
SITE_URL=http://localhost:3000
```

Observações:

- `API_URL` é usado pelo Next.js para reescrever `/api/*` para o backend
- `SITE_URL` é usado para metadados da aplicação

### 5. Inicie o frontend

```bash
cd frontend
npm install
npm run dev
```

Em PowerShell, se `npm` estiver bloqueado:

```powershell
npm.cmd install
npm.cmd run dev
```

Se tudo estiver certo:

- o frontend sobe em `http://localhost:3000`
- o navegador acessa o backend via rewrite em `/api/v1/*`

## Comandos úteis

### Frontend

```bash
cd frontend
npm run lint
npm run typecheck
npm run build
```

### Backend

Windows:

```powershell
cd backend
.\mvnw.cmd verify
```

macOS/Linux:

```bash
cd backend
./mvnw verify
```

## Problemas comuns

### O backend não conecta no PostgreSQL

Verifique:

- se o Docker está rodando
- se `docker compose ps` mostra o container `postgres` saudável
- se `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` batem com o banco local

### O frontend não autentica

Verifique:

- se o backend está em `http://localhost:8080`
- se `frontend/.env` aponta para a URL correta
- se `CORS_ALLOWED_ORIGINS` inclui `http://localhost:3000`

### O build do frontend falha ao baixar fontes

O projeto usa `next/font/google`. Em ambientes com rede restrita, o `npm run build` pode falhar ao buscar as fontes.

## Qualidade

O CI do repositório roda:

- lint do frontend
- typecheck do frontend
- build do frontend
- `verify` do backend

Antes de abrir PR ou atualizar a branch principal, vale rodar esses checks localmente.
