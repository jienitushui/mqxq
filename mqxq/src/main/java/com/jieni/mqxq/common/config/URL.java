package com.jieni.mqxq.common.config;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件上传路径配置类
 * 用于配置系统中各种文件的存储路径，并自动创建相应目录
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Component
@ConfigurationProperties(prefix = "url")
@Data
public class URL {
    
    /** 通用图片存储路径 */
    private String img;
    
    /** 用户头像存储路径 */
    private String headImg;
    
    /** 课程封面图片存储路径 */
    private String courseImg;

    /**
     * 初始化方法，在Bean创建后自动执行
     * 用于确保所有配置的目录都存在，如不存在则自动创建
     */
    @PostConstruct
    public void init() {
        // 确保所有目录都存在
        createDirectoryIfNotExists(img);
        createDirectoryIfNotExists(headImg);
        createDirectoryIfNotExists(courseImg);
    }

    /**
     * 创建目录（如果不存在）
     * 用于确保指定路径的目录存在，如果不存在则自动创建
     * 
     * @param path 目录路径
     */
    private void createDirectoryIfNotExists(String path) {
        if (path != null && !path.trim().isEmpty()) {
            File dir = new File(path);
            if (!dir.exists()) {
                // 创建目录及其父目录
                dir.mkdirs();
            }
        }
    }
}
