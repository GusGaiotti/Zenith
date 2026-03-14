# Zenith

Aplicacao de finanças compartilhadas em monorepo, com dashboard financeiro, fluxo colaborativo entre duas pessoas e assistente contextual para leitura rápida do mês.

- `backend/`: Spring Boot, Java 21, PostgreSQL
- `frontend/`: Next.js, React, TypeScript

## O que entrega

- autenticação com access token + refresh token
- ledger compartilhado entre duas pessoas
- categorias, transações e dashboard financeiro
- convites entre membros
- assistente com contexto financeiro e limites de uso

## Stack

- Java 21
- Spring Boot
- PostgreSQL
- Next.js 16
- React 19
- Tailwind CSS 4

## Rodando localmente

Banco:

```bash
docker compose up -d
```

Backend:

Crie `backend/.env` a partir de [backend/.env.example](./backend/.env.example).

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend:

Crie `frontend/.env`:

```env
API_URL=http://localhost:8080
SITE_URL=http://localhost:3000
```

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

## Ambientes

- desenvolvimento com IA local ou desativada
- produção compatível com Vercel, Render e Neon
- `OPENAI_API_KEY` configurada apenas no backend

## Variáveis importantes

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

## Validação

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
