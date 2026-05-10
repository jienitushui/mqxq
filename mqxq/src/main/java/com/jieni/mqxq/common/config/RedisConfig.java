package com.jieni.mqxq.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 
 * <p>主要功能：
 * <ul>
 *   <li>配置Redis连接模板</li>
 *   <li>设置键值序列化方式</li>
 *   <li>优化Redis性能和兼容性</li>
 * </ul>
 * 
 * <p>序列化策略：
 * <ul>
 *   <li>Key: String序列化 - 保证可读性，便于调试</li>
 *   <li>Value: JSON序列化 - 支持复杂对象，保持类型信息</li>
 * </ul>
 * 
 * @author jieni
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 
     * <p>自定义序列化配置，解决以下问题：
     * <ul>
     *   <li>默认JDK序列化产生乱码</li>
     *   <li>不同类型数据的序列化兼容性</li>
     *   <li>Redis客户端工具查看数据的便利性</li>
     * </ul>
     * 
     * @param connectionFactory Redis连接工厂，Spring自动注入
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);
        
        // 配置Key序列化器 - 使用String序列化
        // 优点：生成的key在Redis客户端中可读性好，便于调试和运维
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 配置Value序列化器 - 使用JSON序列化
        // 优点：支持复杂对象，保留类型信息，跨语言兼容性好
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        // 初始化参数和序列化设置
        template.afterPropertiesSet();
        
        return template;
    }
}