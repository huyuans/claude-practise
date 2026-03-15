# Bailian Spring Boot Starter

Spring Boot 3.x 自动配置模块，快速集成阿里云百炼（DashScope）大模型 API。

## Features

- 同步/流式对话（SSE）
- 文本向量化（Embedding）
- 连接池 + 自动重试
- 向量缓存（可选）

## Quick Start

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.huyuans</groupId>
    <artifactId>bailian-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置

```yaml
bailian:
  api-key: ${DASHSCOPE_API_KEY}
  default-model: qwen-turbo
```

### 3. 使用

```java
@Autowired
private BailianService bailianService;

// 对话
bailianService.chat("你好").subscribe(r -> 
    System.out.println(r.getChoices().get(0).getMessage().getContent()));

// 流式对话
bailianService.chatStream("讲个笑话", r -> 
    System.out.print(r.getChoices().get(0).getDelta().getContent()));

// 向量化
bailianService.embedding("Hello").subscribe(r -> 
    System.out.println(r.getData().get(0).getEmbedding()));
```

## Configuration

| Property | Default |
|----------|---------|
| `bailian.api-key` | `${DASHSCOPE_API_KEY:}` |
| `bailian.base-url` | `https://dashscope.aliyuncs.com` |
| `bailian.timeout` | `60000` |
| `bailian.default-model` | `qwen-turbo` |
| `bailian.retry.max-attempts` | `3` |
| `bailian.connection-pool.max-connections` | `100` |

## Requirements

- Spring Boot 3.2.0+
- Java 17+

## License

MIT