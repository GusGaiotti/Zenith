# Zenith

Aplicacao de financas compartilhadas para duas pessoas, em monorepo:
- `backend/`: Spring Boot (Java 21)
- `frontend/`: Next.js + React + TypeScript

## Visao Geral

Zenith cobre:
- autenticacao com access token + refresh token
- ledger compartilhado
- categorias e transacoes
- dashboard financeiro
- convites entre membros
- funcionalidade **Perguntar para IA** no menu lateral

## Estrategia da IA por Ambiente

A integracao de IA e feita **somente no backend**.

- `AI_MODE=local` (dev/local): usa Ollama
- `AI_MODE=openai` (producao): usa OpenAI
- `AI_MODE=off`: desativa IA com fallback seguro

## Controle de Acesso em Producao (Allowlist)

Em ambiente `prod`, o endpoint de IA so libera acesso para usuarios explicitamente permitidos:
- `users.ai_enabled=true`, ou
- email presente em `AI_PROD_ALLOWLIST_EMAILS`

Se nao estiver allowlisted, a API retorna `403`.

## Requisitos

- Docker + Docker Compose
- Java 21
- Node.js 22+
- npm

## Clone e Setup Completo

### 1. Clonar repositorio

```bash
git clone <URL_DO_REPOSITORIO>
cd zenith
```

### 2. Subir banco local

```bash
docker compose up -d
```

Banco local padrao:
- host: `localhost`
- porta: `5432`
- banco: `zenith_dev`
- usuario: `postgres`
- senha: `postgres`

### 3. Configurar backend

Crie `backend/.env`.

Exemplo:

```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

DB_URL=jdbc:postgresql://localhost:5432/zenith_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres

JWT_SECRET=replace-with-base64-secret-at-least-32-bytes-decoded
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

CORS_ALLOWED_ORIGINS=http://localhost:3000

AUTH_REFRESH_COOKIE_NAME=refresh_token
AUTH_REFRESH_COOKIE_PATH=/api/v1/auth
AUTH_REFRESH_COOKIE_SECURE=false
AUTH_REFRESH_COOKIE_SAME_SITE=Lax
AUTH_REFRESH_COOKIE_DOMAIN=

AI_MODE=local
AI_TIMEOUT_MS=8000
AI_MAX_RESPONSE_TOKENS=300
AI_LIMITS_ENABLED=true
AI_RATE_LIMIT_PER_USER=8
AI_RATE_LIMIT_PER_IP=20
AI_DAILY_QUOTA_PER_USER=50

OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.1:8b

OPENAI_BASE_URL=https://api.openai.com/v1
OPENAI_API_KEY=
OPENAI_MODEL=gpt-4o-mini
AI_PROD_ALLOWLIST_EMAILS=
```

### 4. Iniciar backend

Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
cd backend
./mvnw spring-boot:run
```

### 5. Configurar frontend

Crie `frontend/.env`:

```env
API_URL=http://localhost:8080
SITE_URL=http://localhost:3000
```

### 6. Iniciar frontend

```bash
cd frontend
npm install
npm run dev
```

Em PowerShell, se necessario:

```powershell
npm.cmd install
npm.cmd run dev
```

## Fluxo de Uso: Perguntar para IA

1. Entrar no app autenticado e com ledger ativo.
2. Clicar em **Perguntar para IA** no sidebar (desktop) ou nav mobile.
3. Informar pergunta, mes de referencia e opcional de amostra de transacoes.
4. Enviar.

Endpoint backend:
- `POST /api/v1/ledgers/{ledgerId}/ai/ask`

Request:
- `question` (max 300)
- `yearMonth` opcional (`yyyy-MM`)
- `includeTransactions` opcional (default `false`)

Response:
- `answer`
- `contextLevelUsed` (`SUMMARY | EXTENDED | SAMPLED_TRANSACTIONS`)
- `disclaimer`

## Controles de Custo e Privacidade

- contexto padrao: resumo mensal (nao envia historico completo por default)
- maximo de meses em contexto: `6`
- amostragem maxima de transacoes: `50`
- timeout de provider configuravel
- limite de taxa por usuario e por IP
- quota diaria por usuario
- logs tecnicos sem payload financeiro bruto
- frontend nunca chama Ollama/OpenAI diretamente

## Comandos de Validacao (Obrigatorios)

### Backend

```powershell
cd backend
.\mvnw.cmd test
```

### Frontend

```bash
cd frontend
npm run lint
npm run typecheck
npm run build
```

## Troubleshooting Rapido

- `403` no endpoint de IA em prod: usuario nao allowlisted.
- `429` no endpoint de IA: rate limit ou quota diaria excedida.
- fallback de IA: provider indisponivel/timeout, ver `AI_MODE` e conectividade.
- erro de CORS/auth: revisar `CORS_ALLOWED_ORIGINS` e cookies de refresh.
