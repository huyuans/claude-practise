# Bailian Spring Boot Starter

阿里云百炼模型 API 的 Spring Boot Starter，提供便捷的集成能力。

## 项目介绍

Bailian Spring Boot Starter 是一个基于 Spring Boot 3.x 的自动配置模块，用于快速集成阿里云百炼（DashScope）大模型 API。支持同步聊天、流式聊天和 Embedding 向量生成等功能。

## 功能特性

- **同步聊天**：支持单轮和多轮对话
- **流式聊天**：支持 SSE 流式响应
- **Embedding 向量生成**：支持文本向量化
- **自动配置**：基于 Spring Boot 自动配置机制
- **响应式编程**：基于 WebFlux 响应式编程

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.huyuans</groupId>
    <artifactId>bailian-spring-boot-starter</artifactId>
<version>1.0.0</version>
</dependency>
```

### 2. 配置 API Key

在 `application.yml` 或 `application.properties` 中配置：

```yaml
bailian:
  api-key: your-dashscope-api-key
  # 可选配置
  base-url: https://dashscope.aliyuncs.com
  timeout: 60000
  default-model: qwen-turbo
```

或使用环境变量：

```bash
export DASHSCOPE_API_KEY=your-api-key
```

```yaml
bailian:
  api-key: ${DASHSCOPE_API_KEY}
```

### 3. 使用服务

#### 注入 BailianService

```java
@Autowired
private BailianService bailianService;
```

#### 简单聊天

```java
Mono<ChatResponse> response = bailianService.chat("你好，请介绍一下自己");
response.subscribe(result -> {
    System.out.println(result.getChoices().get(0).getMessage().getContent());
});
```

#### 带参数的聊天

```java
ChatRequest request = ChatRequest.builder()
        .model("qwen-max")
        .messages(Collections.singletonList(
                ChatRequest.Message.builder()
                        .role("user")
                        .content("你好")
                        .build()
        ))
        .temperature(0.8)
        .maxTokens(2000)
        .build();

Mono<ChatResponse> response = bailianService.chat(request);
```

#### 流式聊天

```java
bailianService.chatStream("给我讲个笑话", response -> {
    System.out.print(response.getChoices().get(0).getDelta().getContent());
});
```

#### Embedding 向量生成

```java
// 单条文本
Mono<EmbeddingResponse> response = bailianService.embedding("Hello World");

// 多条文本
Mono<EmbeddingResponse> response = bailianService.embedding(
        Arrays.asList("文本1", "文本2", "文本3")
);
```

## 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `bailian.api-key` | 阿里云百炼 API Key | `${DASHSCOPE_API_KEY:}` |
| `bailian.base-url` | API 基础URL | `https://dashscope.aliyuncs.com` |
| `bailian.timeout` | 请求超时时间（毫秒） | `60000` |
| `bailian.default-model` | 默认模型 | `qwen-turbo` |

## 支持的模型

- **聊天模型**：qwen-turbo, qwen-plus, qwen-max, qwen-max-longcontext 等
- **Embedding模型**：text-embedding-v2, text-embedding-v3 等

具体模型列表请参考 [阿里云百炼文档](https://help.aliyun.com/zh/model-studio/developer-reference/compatibility-of-openapi-with-openai)。

## 依赖说明

- Spring Boot 3.2.0+
- Java 17+
- Spring WebFlux (响应式Web客户端)
- Reactor Netty
- Lombok

## 许可证

MIT License
