package com.jieni.mqxq;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 码趣星球主应用启动类
 * 这是一个基于Spring Boot的编程课程在线教育平台
 * 
 * 功能特性：
 * - 课程管理和学习
 * - 用户认证和授权
 * - 在线支付功能
 * - 作业提交和批改
 * - 文件存储和管理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication // Spring Boot应用主配置注解
@MapperScan("com.jieni.mqxq.dao") // 扫描MyBatis Mapper接口
@EnableScheduling // 启用定时任务支持
@EnableCaching // 启用缓存功能
@EnableTransactionManagement // 启用事务管理
public class MqxqApplication {
    
    /**
     * 应用程序入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MqxqApplication.class, args);
    }

}
