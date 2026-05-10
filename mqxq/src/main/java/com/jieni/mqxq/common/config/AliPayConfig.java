package com.jieni.mqxq.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置类
 * 
 * 用于配置支付宝支付相关的参数，包括应用ID、应用私钥、支付宝公钥等
 * 通过@ConfigurationProperties注解自动绑定配置文件中的属性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 应用私钥
     */
    private String appPrivateKey;
    
    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;
    
    /**
     * 异步通知地址
     */
    private String notifyUrl;
}