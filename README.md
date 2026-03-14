# Zenith

Zenith é um monorepo de finanças compartilhadas pensado como produto real: autenticação segura, dashboard mensal, colaboração entre duas pessoas e um assistente contextual para leitura rápida do mês.

## O que o projeto cobre

- autenticação com access token, refresh token e renovação de sessão
- ledger compartilhado entre duas pessoas
- dashboard com entradas, saídas, categorias, tendência e leitura de contribuição
- convites entre membros
- assistente financeiro com cotas, controle de acesso e fallback seguro

## Stack

- `backend/`: Java 21, Spring Boot, PostgreSQL
- `frontend/`: Next.js 16, React 19, TypeScript, Tailwind CSS 4

## Estrutura

- `backend/src/main`: API, regras de negócio, segurança e integração com IA
- `backend/src/test`: testes unitários e de controller
- `frontend/src/app`: rotas, layouts e páginas
- `frontend/src/components`: interface, dashboard, autenticação e experiência da IA

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

Crie `frontend/.env` com:

```env
API_URL=http://localhost:8080
SITE_URL=http://localhost:3000
```

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

## Variáveis principais

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

O frontend nunca fala diretamente com o provider de IA. Toda chamada passa pelo backend autenticado.

Modos suportados:

- `AI_MODE=off`: desliga o provider e mantém apenas o fallback seguro
- `AI_MODE=local`: usa o provider local para desenvolvimento
- `AI_MODE=openai`: usa o provider remoto para homologação ou produção

Comportamento esperado por ambiente:

- `dev`: pode rodar com `off` ou `local`, sem custo externo
- `staging`: normalmente usa `openai` com chave própria e limites mais conservadores
- `prod`: usa `openai`, cotas ativas e acesso controlado por conta

Controle de acesso:

- o backend pode bloquear o assistente por usuário
- o frontend respeita esse estado e remove o acesso direto na navegação
- o endpoint continua protegido no servidor, mesmo que alguém tente chamar a API manualmente

Contexto da análise:

- a resposta sempre parte do mês selecionado no frontend
- quando houver histórico suficiente, a IA pode comparar o mês atual com meses anteriores próximos
- a opção de amostra de lançamentos envia uma seleção limitada de transações relevantes do período para enriquecer a resposta

Para produção:

- `OPENAI_API_KEY` deve existir apenas no backend
- `OPENAI_MODEL` define o modelo usado pelo provider remoto
- o acesso pode ser liberado por allowlist e pelo flag `ai_enabled`, dependendo do perfil ativo

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

## Observações

- o projeto foi estruturado para deploy independente de frontend e backend
- a camada de IA foi desenhada com fallback seguro para não quebrar a experiência quando o provider estiver indisponível
- a documentação aqui prioriza uso, contexto técnico e comportamento do sistema sem virar playbook operacional
