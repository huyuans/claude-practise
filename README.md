# Bailian Spring Boot Starter

A Spring Boot Starter for Alibaba Cloud Bailian (DashScope) LLM API integration.

## Introduction

Bailian Spring Boot Starter is an auto-configuration module based on Spring Boot 3.x, designed for quick integration with Alibaba Cloud Bailian (DashScope) large language model APIs. It supports synchronous chat, streaming chat, and embedding vector generation.

## Features

- **Synchronous Chat**: Single-turn and multi-turn conversations
- **Streaming Chat**: SSE streaming responses
- **Embedding Vector Generation**: Text vectorization with optional caching
- **Connection Pooling**: Configurable HTTP connection pool for better performance
- **Retry Mechanism**: Automatic retry with exponential backoff
- **Auto-configuration**: Based on Spring Boot auto-configuration mechanism
- **Reactive Programming**: Built on WebFlux

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.huyuans</groupId>
    <artifactId>bailian-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure API Key

In `application.yml` or `application.properties`:

```yaml
bailian:
  api-key: your-dashscope-api-key
  # Optional configurations
  base-url: https://dashscope.aliyuncs.com
  timeout: 60000
  default-model: qwen-turbo
```

Or use environment variable:

```bash
export DASHSCOPE_API_KEY=your-api-key
```

```yaml
bailian:
  api-key: ${DASHSCOPE_API_KEY}
```

### 3. Usage

#### Inject BailianService

```java
@Autowired
private BailianService bailianService;
```

#### Simple Chat

```java
Mono<ChatResponse> response = bailianService.chat("Hello, please introduce yourself");
response.subscribe(result -> {
    System.out.println(result.getChoices().get(0).getMessage().getContent());
});
```

#### Chat with Parameters

```java
ChatRequest request = ChatRequest.builder()
        .model("qwen-max")
        .messages(Collections.singletonList(
                ChatRequest.Message.builder()
                        .role("user")
                        .content("Hello")
                        .build()
        ))
        .temperature(0.8)
        .maxTokens(2000)
        .build();

Mono<ChatResponse> response = bailianService.chat(request);
```

#### Streaming Chat

```java
bailianService.chatStream("Tell me a joke", response -> {
    System.out.print(response.getChoices().get(0).getDelta().getContent());
});
```

#### Embedding Vector Generation

```java
// Single text
Mono<EmbeddingResponse> response = bailianService.embedding("Hello World");

// Multiple texts
Mono<EmbeddingResponse> response = bailianService.embedding(
        Arrays.asList("Text 1", "Text 2", "Text 3")
);
```

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `bailian.api-key` | Alibaba Cloud Bailian API Key | `${DASHSCOPE_API_KEY:}` |
| `bailian.base-url` | API Base URL | `https://dashscope.aliyuncs.com` |
| `bailian.timeout` | Request timeout (ms) | `60000` |
| `bailian.default-model` | Default chat model | `qwen-turbo` |
| `bailian.default-embedding-model` | Default embedding model | `text-embedding-v3` |
| `bailian.retry.enabled` | Enable retry | `true` |
| `bailian.retry.max-attempts` | Max retry attempts | `3` |
| `bailian.connection-pool.enabled` | Enable connection pool | `true` |
| `bailian.connection-pool.max-connections` | Max connections | `100` |
| `bailian.embedding-cache.enabled` | Enable embedding cache | `false` |
| `bailian.embedding-cache.max-size` | Max cache entries | `1000` |
| `bailian.embedding-cache.expire-minutes` | Cache TTL (minutes) | `60` |

## Supported Models

- **Chat Models**: qwen-turbo, qwen-plus, qwen-max, qwen-max-longcontext, etc.
- **Embedding Models**: text-embedding-v2, text-embedding-v3, etc.

For the complete model list, please refer to [Alibaba Cloud Bailian Documentation](https://help.aliyun.com/zh/model-studio/developer-reference/compatibility-of-openapi-with-openai).

## Requirements

- Spring Boot 3.2.0+
- Java 17+
- Spring WebFlux (Reactive Web Client)
- Reactor Netty
- Lombok

## License

MIT License