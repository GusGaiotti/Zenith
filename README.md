# Zenith

Aplicacao de financas compartilhadas em monorepo:
- `backend/`: Spring Boot, Java 21, PostgreSQL
- `frontend/`: Next.js, React, TypeScript

## O que entrega

- autenticacao com access token + refresh token
- ledger compartilhado entre duas pessoas
- categorias, transacoes e dashboard financeiro
- convites entre membros
- assistente de IA com contexto financeiro e limites de uso

## Stack

- Java 21
- Spring Boot
- PostgreSQL
- Next.js 16
- React 19
- Tailwind CSS 4

## Rodando localmente

### Banco

```bash
docker compose up -d
```

### Backend

Crie `backend/.env` a partir de [backend/.env.example](./backend/.env.example).

Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Frontend

Crie `frontend/.env`:

```env
API_URL=http://localhost:8080
SITE_URL=http://localhost:3000
```

Depois:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

## Ambientes

### Desenvolvimento

- `AI_MODE=local` para usar Ollama
- `AI_MODE=off` para desligar a IA

### Producao

- frontend na Vercel com root `frontend/`
- backend no Render com root `backend/`
- banco PostgreSQL no Neon
- `AI_MODE=openai`
- `OPENAI_API_KEY` configurada apenas no backend

Em producao, o acesso da IA pode ser liberado por:
- `AI_PROD_ALLOWLIST_EMAILS`
- `users.ai_enabled = true`

## Variaveis importantes

Backend:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `AI_MODE`
- `OPENAI_API_KEY`
- `OPENAI_MODEL`

Frontend:
- `API_URL`
- `SITE_URL`

## Validacao

Backend:

```powershell
cd backend
.\mvnw.cmd -B verify
```

Frontend:

```powershell
cd frontend
npm.cmd run lint
npm.cmd run typecheck
npm.cmd run build
```
