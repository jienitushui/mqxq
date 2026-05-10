package com.jieni.mqxq.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 用于配置Web相关的拦截器、路径映射等功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ThreadPoolTaskExecutor mvcAsyncTaskExecutor;

    public WebConfig(@Qualifier("threadPoolTaskExecutor") ThreadPoolTaskExecutor mvcAsyncTaskExecutor) {
        this.mvcAsyncTaskExecutor = mvcAsyncTaskExecutor;
    }

    /**
     * 注册拦截器
     * 配置Sa-Token登录校验拦截器，对需要认证的接口进行统一拦截
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")  // 拦截所有路径
                // 排除不需要登录校验的路径
                .excludePathPatterns("/swagger-ui/**")          // Swagger UI界面
                .excludePathPatterns("/doc.html/**")            // Knife4j文档界面
                .excludePathPatterns("/swagger-ui/doc.html/**") // Swagger文档页面
                .excludePathPatterns("/swagger-ui/index.html/**") // Swagger首页
                .excludePathPatterns("/swagger-ui.html/**")     // Swagger UI HTML
                .excludePathPatterns("/v3/api-docs/**")         // OpenAPI 3.0文档
                .excludePathPatterns("/webjars/**")             // 静态资源
                .excludePathPatterns("/api/auth/**")            // 认证相关接口（登录、注册等）
                .excludePathPatterns("/api/public/**")          // 公共接口（无需认证）
                .excludePathPatterns("/api/alipay/**")          // 支付宝回调接口
                .excludePathPatterns("/error");                 // 错误页面
    }

    /**
     * Spring MVC 异步请求线程池配置
     *
     * 避免使用默认的 SimpleAsyncTaskExecutor（高并发下不适合生产）。
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcAsyncTaskExecutor);
        // 如有需要可调整（单位毫秒）：configurer.setDefaultTimeout(60_000L);
    }
}

