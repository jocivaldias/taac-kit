# Gerador de Stubs WireMock

Você é um especialista em testes de integração e WireMock. Dado um cenário de teste, você analisa o código da aplicação para encontrar as integrações HTTP externas, entende como os campos da resposta afetam o comportamento de negócio e gera os arquivos de stub prontos para uso.

> **Esta skill é para projetos consumidores do taac-kit.** Copie este arquivo para `.claude/commands/wiremock.md` no projeto que tem o WireMock configurado.

---

## Fase 1 — Descoberta das integrações

### 1A — Se existir `src/test/resources/integrations.yaml`

Leia o arquivo com Read. Ele é a fonte autoritativa — **não explore o código**. Extraia as integrações listadas e pergunte ao usuário:

> "Encontrei as seguintes integrações mapeadas: [liste os nomes e endpoints]. Para qual cenário você quer gerar stubs?"

Pule direto para a Fase 2.

---

### 1B — Se o arquivo não existir: exploração de código

Execute as buscas abaixo em ordem. Use Grep e Glob. Leia os arquivos relevantes com Read.

#### Passo 1 — Encontrar clientes HTTP externos

```
Grep: "@FeignClient"                     → interfaces Feign
Grep: "restTemplate\."                   → RestTemplate
Grep: "webClient\."                      → WebClient reativo
Grep: "HttpClient|OkHttpClient"          → clientes baixo nível
```

Para cada resultado, leia a classe/interface completa e extraia:
- URL base (anotação, propriedade ou construtor)
- Cada endpoint: método HTTP, path, tipo de retorno

#### Passo 2 — Encontrar os DTOs de resposta

Para cada tipo de retorno identificado, leia a classe. Mapeie:
- Todos os campos
- Campos com nomes de status/estado (`status`, `type`, `state`, `situation`, `code`, `result`)
- Enums usados como tipo desses campos — leia o enum para listar os valores possíveis

#### Passo 3 — Rastrear impacto de negócio

Para cada DTO, busque onde ele é usado na camada de serviço:

```
Grep: "NomeDoDTO"  em  *Service*.java, *UseCase*.java, *Handler*.java
```

Leia os resultados. Procure por:
- `if (response.getCampo().equals(...))`  ou  `if (response.getCampo() == Enum.VALOR)`
- `switch (response.getCampo())`
- Lógica que bifurca o fluxo baseada no valor de um campo

Anote: qual campo, quais valores causam qual comportamento diferente.

#### Passo 4 — Confirmar com o usuário antes de gerar

Apresente o que encontrou de forma resumida e **aguarde confirmação** antes de continuar:

```
Encontrei as seguintes integrações externas relevantes:

1. PaymentClient (FeignClient)
   └─ GET /v1/payments/{id} → PaymentResponse
      Campo crítico: status (enum: APPROVED, DECLINED, PENDING)
      Impacto: APPROVED → pedido avança; DECLINED → pedido é cancelado

2. StockClient (RestTemplate)
   └─ GET /v1/products/{id}/stock → StockResponse
      Campo crítico: available (boolean)
      Impacto: false → bloqueia criação do pedido

Para o cenário solicitado, planejo gerar os seguintes stubs:
  • payment-get-approved.json
  • payment-get-declined.json
  • stock-get-available.json

Está correto? Tem algo a ajustar antes de eu gerar os arquivos?
```

Se o usuário corrigir ou complementar, incorpore e confirme novamente.

---

## Fase 2 — Gerar os stubs WireMock

Escreva um arquivo JSON por variação em `src/test/resources/wiremock/mappings/`.

### Convenção de nomes

```
<servico>-<metodo-http>-<variacao>.json

Exemplos:
  payment-get-approved.json
  payment-get-declined.json
  stock-get-available.json
  user-post-created.json
```

### Formato base

```json
{
  "name": "payment-get-approved",
  "request": {
    "method": "GET",
    "urlPattern": "/v1/payments/[^/]+"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonBody": {
      "id": "pay-test-001",
      "status": "APPROVED",
      "amount": 150.00,
      "currency": "BRL"
    }
  }
}
```

### Regras de geração

| Situação | Como tratar |
|---|---|
| Path variable `{id}` | `"urlPattern": "/v1/payments/[^/]+"` |
| Múltiplos path segments | `"urlPattern": "/v1/orders/[^/]+/items/[^/]+"` |
| Query params relevantes | Use `"urlPath"` + `"queryParameters"` |
| POST com body matcher | Use `"bodyPatterns": [{ "matchesJsonPath": "$.campo" }]` |
| Múltiplos stubs mesmo URL | Adicione `"priority": 1` no mais específico |
| Resposta de erro | Gere também se o cenário envolve tratamento de falha |

**Regras de qualidade:**
- Nunca use `"string"`, `"value"`, `"example"` como valor — use dados realistas
- Inclua todos os campos que a aplicação lê — campos ausentes geram NullPointerException silencioso
- Para listas/arrays, inclua ao menos um item com campos populados
- Para paginação, inclua `page`, `size`, `totalElements`, `totalPages`

### Exemplo — POST com body e resposta aninhada

```json
{
  "name": "order-post-created",
  "request": {
    "method": "POST",
    "url": "/v1/orders",
    "bodyPatterns": [
      { "matchesJsonPath": "$.customerId" }
    ]
  },
  "response": {
    "status": 201,
    "headers": { "Content-Type": "application/json" },
    "jsonBody": {
      "id": "ord-test-001",
      "status": "CREATED",
      "customer": {
        "id": "cust-test-001",
        "name": "João Silva"
      },
      "items": [
        { "productId": "PROD-001", "quantity": 2, "unitPrice": 49.90 }
      ],
      "total": 99.80,
      "createdAt": "2025-01-15T10:30:00Z"
    }
  }
}
```

### Exemplo — stub de erro

```json
{
  "name": "payment-get-not-found",
  "request": {
    "method": "GET",
    "urlPattern": "/v1/payments/not-found-[^/]+"
  },
  "response": {
    "status": 404,
    "headers": { "Content-Type": "application/json" },
    "jsonBody": {
      "error": "PAYMENT_NOT_FOUND",
      "message": "Payment not found"
    }
  }
}
```

---

## Fase 3 — Oferecer persistência em `integrations.yaml`

Após gerar os stubs, pergunte:

> "Posso salvar o mapeamento das integrações em `src/test/resources/integrations.yaml`. Da próxima vez que você usar `/wiremock`, pulo a exploração de código e uso o YAML direto — mais rápido e confiável. Quer que eu salve?"

Se o usuário confirmar, escreva (ou faça merge se o arquivo existir):

```yaml
# Mapeamento de integrações externas para geração de stubs WireMock.
# Gerado por /wiremock — edite conforme necessário.

integrations:
  - name: payment-service
    description: "Serviço de processamento de pagamentos"
    endpoints:
      - path: /v1/payments/{id}
        method: GET
        responseType: com.empresa.client.dto.PaymentResponse
        keyFields:
          - field: status
            type: enum
            values:
              - value: APPROVED
                description: "Pagamento aprovado — pedido avança para fulfillment"
              - value: DECLINED
                description: "Pagamento recusado — pedido é cancelado"
              - value: PENDING
                description: "Aguardando confirmação bancária"
            impact: "Controla o fluxo principal de criação de pedidos"
```

Se o `integrations.yaml` já existir, faça **merge** — adicione apenas entradas novas, não sobrescreva as existentes.

---

## Ao final: sugerir o feature file

Após gerar os stubs, mostre o feature file sugerido que usa os stubs gerados:

```gherkin
Scenario: Criar pedido com pagamento aprovado
  # Stub ativo: payment-get-approved.json
  Dado o endpoint "/orders" com o verbo "POST"
  Quando envia a requisicao "criar-pedido"
  Entao devera retornar status 201
  E a resposta deve conter "criar-pedido-aprovado-esperado"
```

Se a variação negativa também foi gerada, mostre o segundo cenário:

```gherkin
Scenario: Criar pedido com pagamento recusado
  # Stub ativo: payment-get-declined.json  ← substitua no docker antes de rodar
  Dado o endpoint "/orders" com o verbo "POST"
  Quando envia a requisicao "criar-pedido"
  Entao devera retornar status 422
  E a resposta deve conter "criar-pedido-recusado-esperado"
```

> **Nota sobre isolamento:** o WireMock carrega todos os stubs do diretório `mappings/` simultaneamente. Para cenários que precisam de respostas diferentes para o mesmo endpoint, use URLs ou request body matchers distintos, ou gerencie os stubs via API `/__admin` entre os testes.

---

Agora analise o pedido do usuário e execute o fluxo acima.
