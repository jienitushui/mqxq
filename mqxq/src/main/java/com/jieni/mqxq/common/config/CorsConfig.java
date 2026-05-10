package com.jieni.mqxq.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 配置Spring Boot应用的CORS（跨源资源共享）策略
 * 允许前端应用从不同域名访问后端API接口
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * 设置跨域访问策略，允许所有源、头部和请求方法
     * 
     * @return CorsFilter CORS过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        corsConfiguration.addAllowedOrigin("*"); // 设置允许访问的源地址，*表示允许所有域名
        corsConfiguration.addAllowedHeader("*"); // 设置允许的请求头，*表示允许所有请求头
        corsConfiguration.addAllowedMethod("*"); // 设置允许的请求方法，*表示允许所有HTTP方法
        
        // // 生产环境建议配置
        // corsConfiguration.addAllowedOrigin("https://yourdomain.com");
        // corsConfiguration.addAllowedOrigin("https://www.yourdomain.com");
        // // 或使用配置文件外化
        // corsConfiguration.addAllowedOriginPattern("${cors.allowed-origins}");
        
        source.registerCorsConfiguration("/**", corsConfiguration); // 对所有接口路径配置跨域设置
        
        return new CorsFilter(source);
    }
}