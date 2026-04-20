# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
mvn clean install -DskipTests

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=MyTestIT

# Package without tests
mvn package -DskipTests
```

Java 11, Maven. Test files matching `**/*Test.java` and `**/*IT.java` are picked up by Surefire.

## Architecture Overview

This is a **reusable library** for Cucumber-based integration tests. Consumer projects add it as a dependency, configure an `IntegrationTestRuntime`, and then use the pre-built step definitions in their `.feature` files.

### Core wiri

`IntegrationTestRuntime` is a static singleton facade. Before running any scenarios, the consuming project must call:

```java
IntegrationTestRuntime.configure(
    RuntimeConfig.builder()
        .httpTestClient(new RestAssuredHttpTestClient("http://base-url"))
        .queueClient(new SqsQueueClient(sqsClient))
        .keyValueStoreClient(new RedisKeyValueStoreClient(jedis))
        .build()
);
```

`RuntimeConfig` enforces that all three clients are provided at build time (throws `IllegalStateException` otherwise). `IntegrationTestRuntime` then exposes `httpClient()`, `queueClient()`, and `keyValueStoreClient()` statically to the step classes.

**Note:** There is a bug in `IntegrationTestRuntime.configure()` — the `config` field is not actually set (self-assignment). Fix: `IntegrationTestRuntime.config = config;`.

### Step definitions (Cucumber PT-BR)

All steps use Brazilian Portuguese annotations (`@Dado`, `@Quando`, `@Entao`, `@E`):

| Class | Responsibility |
|---|---|
| `HttpSteps` | Build HTTP request spec, send, assert status/body |
| `MessagingSteps` | Send to SQS, wait, read from SQS or Redis string key |
| `RedisHashSteps` | Read from Redis hash field and assert |
| `JsonConfigSteps` | Configure JSON comparison mode (strict/lenient, ignored fields, custom comparator) |

Each step class holds its own `ScenarioContext` instance — this only works correctly because Cucumber creates a new instance per scenario. **Do not share state across step classes via static fields.**

### JSON comparison

`JsonComparatorRegistry` holds named `JsonComparator` implementations. The built-in `"default"` comparator uses JSONAssert. Custom comparators can be registered before test execution:

```java
JsonComparatorRegistry.register("myComparator", new MyCustomComparator());
```

JSON payloads (request bodies and expected responses) are loaded from the classpath under a `payloads/` directory via `ClasspathJsonResourceLoader`.

### Interfaces for extension

| Interface | Default implementation |
|---|---|
| `HttpTestClient` | `RestAssuredHttpTestClient` |
| `QueueClient` | `SqsQueueClient` (AWS SDK v2) |
| `KeyValueStoreClient` | `RedisKeyValueStoreClient` (Jedis) |
| `JsonComparator` | `DefaultJsonComparator` (JSONAssert) |
| `JsonResourceLoader` | `ClasspathJsonResourceLoader` |
