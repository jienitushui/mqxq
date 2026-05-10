package com.jieni.mqxq.common.config;

import com.jieni.mqxq.service.infrastructure.MinIOFileStorageService;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO对象存储配置类
 * 用于配置MinIO客户端，实现文件的上传、下载、删除等操作
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Configuration
@EnableConfigurationProperties({MinIOConfigProperties.class})
// 当引入FileStorageService接口时才启用此配置
@ConditionalOnClass(MinIOFileStorageService.class)
public class MinIOConfig {

    /** MinIO配置属性 */
    @Resource
    private MinIOConfigProperties minIOConfigProperties;

    /**
     * 构建MinIO客户端Bean
     * 用于与MinIO服务器进行通信，执行文件存储操作
     * 
     * @return MinioClient MinIO客户端实例
     */
    @Bean
    public MinioClient buildMinioClient() {
        return MinioClient
                .builder()
                .credentials(minIOConfigProperties.getAccessKey(), minIOConfigProperties.getSecretKey())
                .endpoint(minIOConfigProperties.getEndpoint())
                .build();
    }
}