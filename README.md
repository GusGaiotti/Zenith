# Zenith

Monorepo de financas compartilhadas com foco em um produto realista: autenticacao segura, dashboard mensal, colaboracao entre duas pessoas e um assistente contextual para leitura rapida do mes.

- `backend/`: Spring Boot, Java 21, PostgreSQL
- `frontend/`: Next.js, React, TypeScript

## Principais fluxos

- autenticacao com access token, refresh token e renovacao de sessao
- ledger compartilhado entre duas pessoas
- dashboard com entradas, saidas, categorias, tendencia e leitura de contribuicao
- convites entre membros
- assistente com contexto financeiro, cotas e fallback seguro

## Stack

- Java 21
- Spring Boot
- PostgreSQL
- Next.js 16
- React 19
- Tailwind CSS 4

## Estrutura

- `backend/src/main`: API, regras de negocio, seguranca e integracao com IA
- `backend/src/test`: testes unitarios e de controller
- `frontend/src/app`: rotas e layouts
- `frontend/src/components`: interface, dashboard, autenticacao e IA

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

## Variaveis principais

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

## IA por ambiente

- `AI_MODE=off`: desliga o assistente e mantem apenas o fallback seguro
- `AI_MODE=local`: usa provider local para desenvolvimento
- `AI_MODE=openai`: habilita o provider remoto para homologacao ou producao

Em producao:
- a chave `OPENAI_API_KEY` deve existir apenas no backend
- o acesso pode ser restringido por conta, mesmo com o modo ativo
- o frontend apenas consome a API autenticada; nao fala diretamente com o provider

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
