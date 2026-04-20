# taac-kit

Biblioteca Java para testes de integração baseados em Cucumber (BDD), focada em APIs HTTP e filas SQS. O desenvolvedor declara apenas o input e o output esperado — a lib cuida da conexão, execução e asserção.

---

## Instalação

```xml
<dependency>
    <groupId>com.jocivaldias</groupId>
    <artifactId>taac-kit</artifactId>
    <version>1.0.0</version>
</dependency>
```

Dependências **opcionais** — adicione apenas o que for usar:

```xml
<!-- Se usar RestAssuredHttpTestClient -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>

<!-- Se usar SqsQueueClient -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sqs</artifactId>
    <version>2.25.69</version>
    <scope>test</scope>
</dependency>
```

---

## Configuração

Crie uma classe de setup do Cucumber e configure o `IntegrationTestRuntime` antes dos cenários:

```java
@BeforeAll
static void setup() {
    IntegrationTestRuntime.configure(
        RuntimeConfig.builder()
            .httpTestClient(new RestAssuredHttpTestClient("http://localhost:8080"))
            .queueClient(new SqsQueueClient(sqsClient))
            .build()
    );
}
```

`RuntimeConfig` aceita `httpTestClient` e `queueClient` como opcionais — configure apenas o que o seu projeto usa.

### SqsQueueClient com timeout customizado

```java
new SqsQueueClient(sqsClient, 10) // espera até 10s por mensagem (padrão: 5s)
```

---

## Estrutura de arquivos

```
src/test/
├── java/
│   └── com/suaempresa/seuservico/
│       └── RunCucumberIT.java        # runner do Cucumber
└── resources/
    ├── features/
    │   └── criar-usuario.feature     # cenários em Gherkin
    └── payloads/
        ├── criar-usuario.json        # envelope de requisição HTTP
        ├── criar-usuario-esperado.json
        ├── evento-entrada.json       # envelope de mensagem SQS
        └── evento-saida-esperado.json
```

---

## Steps disponíveis

### HTTP

```gherkin
# Define o endpoint e o método HTTP
Dado o endpoint "/users" com o verbo "POST"

# Envia a requisição carregando o envelope payloads/criar-usuario.json
Quando envia a requisicao "criar-usuario"

# Envia sem corpo (útil para GET e DELETE sem body)
Quando envia a requisicao sem corpo

# Valida o status HTTP (aceita o número diretamente, sem aspas)
Entao devera retornar status 201

# Valida o corpo da resposta contra payloads/criar-usuario-esperado.json
E a resposta deve conter "criar-usuario-esperado"
```

### Mensageria (SQS)

```gherkin
# Envia mensagem para a fila — carrega payloads/evento-entrada.json
Quando envia mensagem na fila "nome-da-fila" com "evento-entrada"

# Aguarda processamento (suporta: 500ms, 5s, 2m, PT10S)
E aguarda "3s"

# Lê da fila de saída e valida contra payloads/evento-saida-esperado.json
Entao a fila "fila-de-saida" deve conter "evento-saida-esperado"
```

### Configuração de comparação JSON

```gherkin
# Comparação estrita (rejeita campos extras na resposta)
Dado que as comparacoes de JSON sejam strict

# Comparação leniente (campos extras na resposta são tolerados) — padrão
Dado que as comparacoes de JSON sejam lenient

# Ignora campos específicos na comparação (útil para id, timestamp, etc.)
E ignorando os campos de JSON:
  """
  id
  createdAt
  updatedAt
  """

# Usa um comparador customizado registrado via JsonComparatorRegistry
E usando o comparador de JSON "meu-comparador"
```

---

## Formato dos arquivos de payload

### Envelope de requisição HTTP (`payloads/criar-usuario.json`)

```json
{
  "headers": {
    "Authorization": "Bearer token123",
    "X-Correlation-Id": "test-abc"
  },
  "queryParams": {
    "version": "v2"
  },
  "body": {
    "name": "João Silva",
    "email": "joao@exemplo.com"
  }
}
```

Todos os campos são opcionais. Para um POST simples sem headers, você pode omitir `headers` e `queryParams` e usar só `body`. Para um GET sem body, basta omitir o `body` e usar `queryParams`.

> **Retrocompatibilidade:** se o arquivo não contiver as chaves `body`, `headers` ou `queryParams` no nível raiz, o conteúdo inteiro é tratado como o body da requisição.

### Resposta esperada (`payloads/criar-usuario-esperado.json`)

```json
{
  "id": "qualquer-valor",
  "name": "João Silva",
  "email": "joao@exemplo.com"
}
```

Use o step `ignorando os campos de JSON` para campos dinâmicos como `id` e `createdAt`.

### Envelope de mensagem SQS (`payloads/evento-entrada.json`)

```json
{
  "attributes": {
    "eventType": {
      "type": "String",
      "value": "USER_CREATED"
    }
  },
  "body": {
    "userId": "123",
    "name": "João Silva"
  }
}
```

`attributes` é opcional. Se o arquivo não contiver `body`, o conteúdo inteiro é enviado como body da mensagem.

---

## Exemplos completos

### Teste de API REST

```gherkin
Feature: Gerenciamento de usuários

  Scenario: Criar usuário com sucesso
    Dado o endpoint "/users" com o verbo "POST"
    Quando envia a requisicao "criar-usuario"
    Entao devera retornar status 201
    E ignorando os campos de JSON:
      """
      id
      createdAt
      """
    E a resposta deve conter "criar-usuario-esperado"

  Scenario: Buscar usuário existente
    Dado o endpoint "/users/42" com o verbo "GET"
    Quando envia a requisicao sem corpo
    Entao devera retornar status 200
    E a resposta deve conter "buscar-usuario-esperado"

  Scenario: Listar usuários com filtro
    Dado o endpoint "/users" com o verbo "GET"
    Quando envia a requisicao "listar-usuarios-filtro"
    Entao devera retornar status 200
    E a resposta deve conter "listar-usuarios-esperado"
```

### Teste de fluxo assíncrono (HTTP → SQS)

```gherkin
Feature: Processamento de pedidos

  Scenario: Criar pedido dispara evento na fila de processamento
    Dado o endpoint "/orders" com o verbo "POST"
    Quando envia a requisicao "criar-pedido"
    Entao devera retornar status 202
    E aguarda "2s"
    E a fila "pedidos-processamento" deve conter "pedido-processamento-esperado"
```

### Teste de fluxo SQS → SQS

```gherkin
Feature: Enriquecimento de eventos

  Scenario: Evento de entrada dispara evento enriquecido na saida
    Quando envia mensagem na fila "eventos-entrada" com "evento-raw"
    E aguarda "3s"
    Entao a fila "eventos-enriquecidos" deve conter "evento-enriquecido-esperado"
```

---

## Extensões

### Implementar seu próprio HttpTestClient

```java
public class OkHttpTestClient implements HttpTestClient {
    @Override
    public Response execute(HttpRequestSpec spec) {
        // sua implementação
    }
}
```

### Registrar um comparador JSON customizado

```java
JsonComparatorRegistry.register("schema-only", new JsonSchemaComparator());
```

Depois use no feature file:
```gherkin
E usando o comparador de JSON "schema-only"
```

---

## Skill para geração de testes

O repositório inclui uma skill em `taac.md`.

```
/taac
```

A skill recebe a descrição do cenário (endpoint, input, output esperado) e gera automaticamente o `.feature` e os arquivos JSON correspondentes.
