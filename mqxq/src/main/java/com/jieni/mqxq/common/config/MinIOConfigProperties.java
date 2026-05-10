package com.jieni.mqxq.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * MinIO配置属性类
 * 用于绑定application.yml中minio前缀的配置信息
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀minio
public class MinIOConfigProperties implements Serializable {
    
    /** 访问密钥ID */
    private String accessKey;
    
    /** 秘密访问密钥 */
    private String secretKey;
    
    /** 存储桶名称 */
    private String bucket;
    
    /** MinIO服务端点地址 */
    private String endpoint;
    
    /** 文件读取访问路径 */
    private String readPath;
}
