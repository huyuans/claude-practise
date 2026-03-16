package com.huyuans.bailian.autoconfigure;

import com.huyuans.bailian.cache.EmbeddingCache;
import com.huyuans.bailian.client.BailianClient;
import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.metrics.BailianMetricsRecorder;
import com.huyuans.bailian.service.BailianService;
import com.huyuans.bailian.session.ConversationManager;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 百炼模型自动配置类
 * <p>
 * Spring Boot 自动配置类，负责初始化百炼 SDK 所需的所有核心组件。
 * 当配置了 bailian.api-key 时自动激活，采用条件装配确保组件可被用户自定义覆盖。
 * <p>
 * 自动装配的组件包括：
 * <ul>
 *   <li>{@link BailianClient} - API 调用客户端，负责 HTTP 通信</li>
 *   <li>{@link EmbeddingCache} - 向量缓存，避免重复计算</li>
 *   <li>{@link BailianMetricsRecorder} - 指标记录器，集成 Micrometer</li>
 *   <li>{@link BailianService} - 业务服务层，提供高级封装</li>
 *   <li>{@link ConversationManager} - 会话管理器（可选，需显式启用）</li>
 * </ul>
 * <p>
 * 配置示例：
 * <pre>
 * bailian:
 *   api-key: ${DASHSCOPE_API_KEY}
 *   conversation:
 *     enabled: true  # 启用会话管理
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 * @see BailianProperties 配置属性类
 * @see ConditionalOnMissingBean 允许用户自定义覆盖
 */
@AutoConfiguration
@EnableConfigurationProperties(BailianProperties.class)
@ConditionalOnProperty(prefix = "bailian", name = "api-key")
public class BailianAutoConfiguration {

    /**
     * 创建百炼 API 客户端
     * <p>
     * 负责与阿里云百炼服务的 HTTP 通信，支持同步/流式调用
     *
     * @param properties 百炼配置属性
     * @return BailianClient 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BailianClient bailianClient(BailianProperties properties) {
        return new BailianClient(properties);
    }

    /**
     * 创建 Embedding 缓存
     * <p>
     * 内存缓存实现，用于缓存文本向量结果，减少 API 调用
     *
     * @param properties 百炼配置属性
     * @return EmbeddingCache 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public EmbeddingCache embeddingCache(BailianProperties properties) {
        return new EmbeddingCache(properties.getEmbeddingCache());
    }

    /**
     * 创建指标记录器
     * <p>
     * 集成 Micrometer，记录 API 调用次数、延迟、token 使用量等指标
     *
     * @param meterRegistry Micrometer 注册表（可选，未配置时指标功能不启用）
     * @return BailianMetricsRecorder 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BailianMetricsRecorder bailianMetricsRecorder(
            @Autowired(required = false) MeterRegistry meterRegistry) {
        return new BailianMetricsRecorder(meterRegistry);
    }

    /**
     * 创建百炼服务层
     * <p>
     * 高级封装类，提供便捷的聊天和 Embedding 方法，自动处理缓存和指标
     *
     * @param bailianClient    API 客户端
     * @param properties       配置属性
     * @param embeddingCache   向量缓存
     * @param metricsRecorder  指标记录器
     * @return BailianService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BailianService bailianService(BailianClient bailianClient, 
                                          BailianProperties properties,
                                          EmbeddingCache embeddingCache,
                                          BailianMetricsRecorder metricsRecorder) {
        return new BailianService(bailianClient, properties, embeddingCache, metricsRecorder);
    }

    /**
     * 创建会话管理器（可选）
     * <p>
     * 管理多轮对话会话，支持会话过期清理和上下文长度限制
     * 需配置 bailian.conversation.enabled=true 才会激活
     *
     * @param bailianService 百炼服务
     * @param properties     配置属性
     * @return ConversationManager 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "bailian.conversation", name = "enabled", havingValue = "true", matchIfMissing = false)
    public ConversationManager conversationManager(BailianService bailianService, 
                                                    BailianProperties properties) {
        BailianProperties.Conversation conversation = properties.getConversation();
        return new ConversationManager(
                bailianService,
                conversation.getExpireMinutes(),
                conversation.getMaxTokensPerSession()
        );
    }
}