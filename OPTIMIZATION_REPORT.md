# Relatório de Otimizações — Zenith

**Data:** 2026-03-29
**Branch:** `feat/final-polish-release`
**Commits gerados:** 6

---

## Sumário Executivo

Foi realizada uma análise estrutural completa do código-fonte (backend Java/Spring Boot e frontend Next.js/TypeScript). Foram identificados e corrigidos seis problemas distribuídos em cinco arquivos, cobrindo desperdício de memória, redundâncias de código, consultas desnecessárias ao banco de dados, vulnerabilidade de inflação de prompt na IA e degradação de experiência no frontend.

---

## 1. Queries duplicadas e mortas em `TransactionRepository`

**Arquivo:** `backend/.../repository/TransactionRepository.java`
**Commit:** `b444646`

### Problema

Dois métodos — `getMonthlyTotals` e `getPreviousMonthTotals` — possuíam SQL **identicamente iguais** e **nunca eram invocados** por nenhum service. O `DashboardService.getOverview()` busca os totais de receita e despesa via `sumAmountByLedgerAndTypeAndDateRange`, tornando esses dois métodos completamente inacessíveis em produção.

```java
// Ambos os métodos tinham exatamente este SQL:
SELECT
    COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
    COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expense
FROM transactions t
WHERE t.ledger_id = :ledgerId AND t.date >= :startDate AND t.date <= :endDate
```

### Solução

Remoção completa dos dois métodos. O repositório ficou mais enxuto e sem código que poderia confundir futuros mantenedores sobre qual método usar para obter totais mensais.

### Impacto

- **-26 linhas** de código morto removidas
- Elimina ambiguidade de manutenção

---

## 2. Iteração dupla sobre `dailyResults` e casts antiquados em `DashboardService`

**Arquivo:** `backend/.../service/DashboardService.java`
**Commit:** `662e667`

### Problema A — Segunda passagem desnecessária

O método `getPulse()` realizava **dois loops** sobre a mesma lista `dailyResults`. O primeiro loop construía o `Map<LocalDate, BigDecimal>` com os gastos por dia. O segundo loop — escrito separadamente logo abaixo — percorria o mesmo conjunto de dados apenas para encontrar o dia de maior gasto (`highestSpendingDay`).

```java
// Loop 1: construir mapa
for (Object[] row : dailyResults) {
    spendingByDate.put(date, amount);
}

// ... (40 linhas de código) ...

// Loop 2: encontrar máximo (iteração redundante)
for (Object[] row : dailyResults) {
    if (amount.compareTo(maxSpending) > 0) { ... }
}
```

### Solução A

O cálculo do máximo foi incorporado ao primeiro loop, eliminando a segunda varredura. Para um mês com 31 dias, a lista tem no máximo 31 entradas — pequena o suficiente para que o impacto absoluto seja baixo, mas a mudança elimina a inconsistência lógica e reduz a complexidade cognitiva do método.

### Problema B — Casts no estilo Java 8

O método privado `toBigDecimal()` usava verificações `instanceof` com casts explícitos no padrão de Java 8:

```java
if (value instanceof BigDecimal) {
    return (BigDecimal) value;   // cast explícito desnecessário em Java 16+
}
if (value instanceof Number) {
    return new BigDecimal(value.toString());
}
```

O `AiContextBuilder`, no mesmo projeto, já usava o **pattern matching** do Java 16:

```java
if (value instanceof BigDecimal bigDecimal) { ... }
if (value instanceof Number number) { ... }
```

### Solução B

Modernização para `switch` com pattern matching (Java 21):

```java
return switch (value) {
    case null -> BigDecimal.ZERO;
    case BigDecimal bd -> bd;
    case Number n -> new BigDecimal(n.toString());
    default -> BigDecimal.ZERO;
};
```

### Impacto

- Elimina iteração redundante em `getPulse()`
- Uniformiza estilo com o restante do codebase (Java 21)
- **-8 linhas** líquidas após a refatoração

---

## 3. Busca duplicada ao banco em `LedgerService.inviteUser`

**Arquivo:** `backend/.../service/LedgerService.java`
**Commit:** `417ce5e`

### Problema

O método `inviteUser` chamava `ledgerRepository.findByIdWithLock(ledgerId)` para adquirir o bloqueio pessimista e verificar a existência do ledger — mas descartava o resultado. Mais adiante, após todas as validações de negócio, chamava `ledgerRepository.findById(ledgerId).get()` para reobtiver o mesmo objeto:

```java
// Linha 63: busca com lock — resultado descartado
ledgerRepository.findByIdWithLock(ledgerId)
        .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));

// ... validações ...

// Linha 91: busca redundante do mesmo ledger, sem o lock
Ledger ledger = ledgerRepository.findById(ledgerId).get();
```

Isso gerava **duas consultas ao banco** para o mesmo registro no mesmo fluxo, sendo a segunda desnecessária — e tecnicamente pior, pois realizava a busca sem o lock já estabelecido.

### Solução

O resultado de `findByIdWithLock` passou a ser atribuído diretamente à variável `ledger`, eliminando a segunda chamada:

```java
Ledger ledger = ledgerRepository.findByIdWithLock(ledgerId)
        .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));
```

### Problema adicional — `Collectors.toList()` mutável

`buildLedgerResponse` usava `Collectors.toList()` (retorna lista mutável) enquanto o resto do codebase usa `.toList()` (lista imutável, disponível desde Java 16):

```java
// Antes
.collect(Collectors.toList());

// Depois
.toList();
```

### Impacto

- **-1 query** ao banco a cada convite enviado
- Remoção do import `java.util.stream.Collectors` não mais necessário
- Consistência com o padrão de listas imutáveis já adotado no restante do projeto

---

## 4. Verificação redundante e ausência de limite de comprimento em `AskAiService`

**Arquivo:** `backend/.../service/ai/AskAiService.java`
**Commit:** `1e0f6e1`

### Problema A — Verificação `isEmpty()` duplicada

O método `buildUserPrompt` verificava `context.topExpenseCategories().isEmpty()` **duas vezes consecutivas**, em dois blocos `if` separados:

```java
// Bloco 1: lista as categorias
if (!context.topExpenseCategories().isEmpty()) {
    // ...top categorias...
}

// Bloco 2: imediatamente abaixo, mesma condição para "maior categoria"
if (!context.topExpenseCategories().isEmpty()) {
    AiContextBuilder.CategoryTotal leadCategory = context.topExpenseCategories().getFirst();
    // ...
}
```

A lista não pode mudar entre os dois blocos (é imutável), tornando a segunda verificação completamente desnecessária.

### Solução A

Fusão dos dois blocos em um único `if`:

```java
if (!context.topExpenseCategories().isEmpty()) {
    // lista as top categorias...
    // e também extrai a líder — sem segunda verificação
    AiContextBuilder.CategoryTotal leadCategory = context.topExpenseCategories().getFirst();
    // ...
}
```

### Problema B — Ausência de limite no comprimento da pergunta

O método `sanitizeQuestion` não impunha nenhum teto de comprimento. Um usuário poderia enviar uma pergunta com dezenas de milhares de caracteres, inflando o prompt enviado ao provider de IA (OpenAI/Ollama) e:
- Aumentando o custo de tokens desnecessariamente
- Potencialmente atingindo limites de contexto do modelo
- Sobrecarregando a rede e a serialização

### Solução B

Truncamento a 500 caracteres antes de qualquer outra transformação:

```java
private static final int MAX_QUESTION_LENGTH = 500;

String truncated = rawQuestion.length() > MAX_QUESTION_LENGTH
        ? rawQuestion.substring(0, MAX_QUESTION_LENGTH)
        : rawQuestion;
```

O limite é aplicado **antes** do trim e da checagem de injeção, garantindo que uma pergunta intencionalmente longa não consiga contornar o filtro ao colocar o payload ofensivo após os primeiros 500 caracteres.

### Impacto

- Elimina verificação `isEmpty()` redundante por chamada ao assistente
- Protege contra inflação de tokens por perguntas excessivamente longas
- Melhora a previsibilidade do custo de API em produção

---

## 5. Crescimento ilimitado de memória e chamada dupla de `LocalDate.now()` em `AiUsageGuardService`

**Arquivo:** `backend/.../service/ai/AiUsageGuardService.java`
**Commit:** `a0ea6d3`

### Problema A — Maps crescendo indefinidamente

`AiUsageGuardService` mantém três `ConcurrentHashMap` em memória para controle de rate limiting:

- `userRateCounters` — contadores por minuto por usuário
- `ipRateCounters` — contadores por minuto por IP
- `userDailyQuota` — cotas diárias por usuário

Nenhuma entrada era jamais removida. Com o tempo — especialmente com um grande número de IPs ou usuários únicos — essas estruturas cresceriam indefinidamente, consumindo heap gradualmente até pressionar o GC ou causar `OutOfMemoryError` em produção de longa duração.

O único mecanismo de limpeza existente era a verificação interna no `checkWindowCounter`: ao acessar um contador expirado, seu valor era zerado — mas a entrada **permanecia no mapa** para sempre.

### Solução A

Adição de um método `cleanupStaleEntries()` chamado a cada 200 invocações de `assertAllowedAndConsume`. O intervalo de 200 foi escolhido para equilibrar eficiência (não limpar a cada chamada) com controle de crescimento (considerando o limite de 8 req/min por usuário, a limpeza ocorre a cada ~25 minutos de uso contínuo):

```java
private static final int CLEANUP_INTERVAL = 200;
private int assertCallCount = 0;

// No assertAllowedAndConsume:
if (++assertCallCount % CLEANUP_INTERVAL == 0) {
    cleanupStaleEntries(now, windowMs);
}

private void cleanupStaleEntries(long now, long windowMs) {
    LocalDate today = LocalDate.now();
    // Remove entradas de rate window expiradas (2x a janela por segurança)
    userRateCounters.entrySet().removeIf(entry -> {
        synchronized (entry.getValue()) {
            return now - entry.getValue().windowStart >= windowMs * 2;
        }
    });
    ipRateCounters.entrySet().removeIf(entry -> {
        synchronized (entry.getValue()) {
            return now - entry.getValue().windowStart >= windowMs * 2;
        }
    });
    // Remove cotas de dias anteriores
    userDailyQuota.entrySet().removeIf(entry -> {
        synchronized (entry.getValue()) {
            return entry.getValue().day.isBefore(today);
        }
    });
}
```

### Problema B — `LocalDate.now()` chamado duas vezes no mesmo bloco `synchronized`

O método `getDailyCount` chamava `LocalDate.now()` duas vezes dentro do bloco `synchronized`: uma para comparar e outra para atualizar o campo `day`. Embora `LocalDate.now()` seja uma chamada rápida, invocá-la duas vezes dentro de um lock introduz uma inconsistência teórica (o dia pode mudar entre as duas chamadas à meia-noite) e um overhead desnecessário:

```java
synchronized (counter) {
    if (!counter.day.equals(LocalDate.now())) {  // chamada 1
        counter.day = LocalDate.now();            // chamada 2 — pode retornar dia diferente!
        counter.count = 0;
    }
}
```

### Solução B

Cache do valor antes de entrar no lock:

```java
LocalDate today = LocalDate.now();  // uma única chamada, fora do lock
synchronized (counter) {
    if (!counter.day.equals(today)) {
        counter.day = today;
        counter.count = 0;
    }
}
```

### Impacto

- Previne leak de memória em deployments de longa duração com grande volume de IPs únicos
- Elimina inconsistência teórica na virada do dia para contadores diários
- Reduz chamadas a `LocalDate.now()` de 2 para 1 em `getDailyCount`

---

## 6. Loading monolítico bloqueando controles no dashboard (frontend)

**Arquivo:** `frontend/src/app/(app)/dashboard/page.tsx`
**Commit:** `dbbc5bd`

### Problema

O dashboard realizava 6 queries React Query em paralelo (`overview`, `trends`, `split`, `categories`, `pulse`, `transactions`) e calculava um único flag `loading` como `OR` lógico de todos os estados:

```tsx
const loading = overview.isLoading || trends.isLoading || split.isLoading
              || categories.isLoading || pulse.isLoading;

if (loading) {
  return <skeleton-completo-sem-controles />;
}
```

Isso significava que enquanto qualquer query ainda estivesse carregando, **toda a página** — incluindo o header, o seletor de mês, o filtro de pessoa e o botão de exportação — ficava completamente inacessível. O usuário não podia trocar o mês ou o filtro sem esperar o carregamento terminar.

Adicionalmente, os componentes de gráfico (`ExpenseTrendChart`, `CoupleSplitPanel`, etc.) estavam envoltos em `<Suspense>` desnecessários: esses componentes recebem dados via props e nunca emitem `Promise` (não usam `useSuspenseQuery`), portanto as boundaries nunca eram ativadas e existiam apenas como código ornamental.

### Solução

O bloco de controles (header + filtros + exportação) foi extraído para uma variável JSX `controls` e movido para **antes** do check de loading. O skeleton passou a ocupar apenas a área de conteúdo abaixo dos controles:

```tsx
// Controles sempre renderizados — antes do activeLedgerId check
const controls = <div>...header + filtros...</div>;

if (!activeLedgerId) {
  return <div>{notificationBell} + <EmptyState /></div>;
}

return (
  <div>
    {controls}           {/* sempre visível */}
    {exportError ? ... }
    {hasError ? ... }
    {loading ? (
      <div>...skeletons apenas da área de conteúdo...</div>
    ) : (
      <>
        <OverviewCards overview={overview.data} />
        ...gráficos...
      </>
    )}
  </div>
);
```

Os wrappers `<Suspense>` foram removidos, pois não tinham efeito real neste contexto.

### Impacto

- Controles de filtro e exportação ficam **interativos imediatamente** ao entrar na página
- O usuário pode trocar o mês enquanto a primeira carga ainda está em andamento
- Remove ~12 linhas de `Suspense` inoperantes
- A estrutura de loading fica mais clara e honesta sobre o que está esperando

---

## Resumo Geral

| # | Área | Tipo | Arquivo |
|---|------|------|---------|
| 1 | Backend | Remoção de código morto | `TransactionRepository.java` |
| 2a | Backend | Otimização de loop | `DashboardService.java` |
| 2b | Backend | Modernização de sintaxe | `DashboardService.java` |
| 3a | Backend | Eliminação de query redundante | `LedgerService.java` |
| 3b | Backend | Modernização de coleção | `LedgerService.java` |
| 4a | Backend | Eliminação de verificação duplicada | `AskAiService.java` |
| 4b | Backend | Limite de comprimento de input | `AskAiService.java` |
| 5a | Backend | Prevenção de memory leak | `AiUsageGuardService.java` |
| 5b | Backend | Correção de race condition teórica | `AiUsageGuardService.java` |
| 6 | Frontend | UX de carregamento progressivo | `dashboard/page.tsx` |

Todas as mudanças são retrocompatíveis, não alteram contratos de API e não requerem migrations de banco de dados.
