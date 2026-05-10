package com.jieni.mqxq.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jieni.mqxq.util.RabbitMqHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * RabbitMQ消息队列配置类
 * 用于配置消息转换器和RabbitMQ助手工具
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Configuration
public class RabbitConfig {
    
    /**
     * 创建自定义的ConnectionFactory，确保使用正确的配置
     */
    @Bean
    @Primary
    public ConnectionFactory connectionFactory(
            @Value("${spring.rabbitmq.host}") String host,
            @Value("${spring.rabbitmq.port}") int port,
            @Value("${spring.rabbitmq.username}") String username,
            @Value("${spring.rabbitmq.password}") String password,
            @Value("${spring.rabbitmq.virtual-host:/}") String virtualHost) {
        
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        // 确保虚拟主机正确设置（/ 需要特殊处理）
        if ("/".equals(virtualHost) || virtualHost == null || virtualHost.isEmpty()) {
            factory.setVirtualHost("/");
        } else {
            factory.setVirtualHost(virtualHost);
        }
        
        // 设置连接参数
        factory.getRabbitConnectionFactory().setRequestedHeartbeat(60);
        factory.getRabbitConnectionFactory().setConnectionTimeout(30000);
        // 禁用自动恢复，避免在连接失败时产生混乱
        factory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(false);
        factory.setChannelCacheSize(25);
        factory.setChannelCheckoutTimeout(1000);
        
        // 添加连接监听器，用于诊断连接问题
        factory.addConnectionListener(new org.springframework.amqp.rabbit.connection.ConnectionListener() {
            @Override
            public void onCreate(org.springframework.amqp.rabbit.connection.Connection connection) {
                log.info("✅ RabbitMQ连接已成功创建: {}", connection);
            }
            
            @Override
            public void onClose(org.springframework.amqp.rabbit.connection.Connection connection) {
                log.warn("⚠️ RabbitMQ连接已关闭: {}", connection);
            }
        });
        
        log.info("RabbitMQ连接工厂已创建：{}:{}，虚拟主机：'{}'，用户名：{}", host, port, factory.getVirtualHost(), username);
        log.info("提示：如果连接失败，请检查：");
        log.info("  1. RabbitMQ服务器是否正常运行");
        log.info("  2. 用户名和密码是否正确");
        log.info("  3. 用户是否有访问虚拟主机 '/' 的权限");
        log.info("  4. 服务器端日志是否有拒绝连接的详细信息");
        
        return factory;
    }
    
    /**
     * 配置JSON消息转换器
     * 用于将Java对象与JSON消息进行相互转换
     * 
     * @param mapper JSON对象映射器
     * @return MessageConverter 消息转换器
     */
    @Bean
    public MessageConverter messageConverter(ObjectMapper mapper){
        // 1.定义消息转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter(mapper);
        // 2.配置自动创建消息id，用于识别不同消息
        jackson2JsonMessageConverter.setCreateMessageIds(true);
        return jackson2JsonMessageConverter;
    }

    /**
     * 配置RabbitMQ助手工具
     * 用于简化消息发送和接收操作
     * 
     * @param rabbitTemplate RabbitMQ模板
     * @return RabbitMqHelper RabbitMQ助手工具实例
     */
    @Bean
    public RabbitMqHelper rabbitMqHelper(RabbitTemplate rabbitTemplate){
        return new RabbitMqHelper(rabbitTemplate);
    }
}
