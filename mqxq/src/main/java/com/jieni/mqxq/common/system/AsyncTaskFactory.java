package com.jieni.mqxq.common.system;

import cn.hutool.core.date.DateUtil;
import com.jieni.mqxq.dao.LogsDao;
import com.jieni.mqxq.domain.entity.Logs;
import com.jieni.mqxq.util.IpUtils;
import com.jieni.mqxq.util.SpringUtils;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步任务工厂类
 * 管理所有多线程任务的执行，提供异步处理各种后台任务的功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public class AsyncTaskFactory {

    /** 线程池执行器，用于执行异步任务 */
    private static final ThreadPoolTaskExecutor EXECUTOR = SpringUtils.getBean("threadPoolTaskExecutor");

    /**
     * 异步记录操作日志
     * 在独立线程中记录用户操作日志，避免影响主业务流程性能
     * 包含模块、操作、操作人ID、IP地址、操作时间等信息
     * 
     * @param module 功能模块名称
     * @param operate 操作类型描述
     * @param userId 操作用户ID
     */
    public static void recordLog(String module, String operate, Integer userId) {
        // 获取当前请求的IP地址（注意：异步任务中可能无法获取HttpServletRequest）
        String ip = IpUtils.getIpAddr(); 
        
        // 提交异步任务到线程池执行
        EXECUTOR.execute(() -> {
            try {
                // 创建日志对象并设置相关信息
                Logs logs = Logs.builder()
                        .module(module)      // 功能模块
                        .operate(operate)    // 操作描述
                        .userId(userId)      // 操作用户ID
                        .ip(ip)              // 操作IP地址
                        .time(DateUtil.now()) // 操作时间
                        .build();
                
                // 获取日志DAO并插入数据库
                LogsDao logsDao = SpringUtils.getBean(LogsDao.class);
                logsDao.insert(logs);
            } catch (Exception e) {
                // 记录日志操作失败时不影响主业务，只记录错误信息
                // 注意：在异步线程中无法使用@Slf4j，使用标准日志记录
                org.slf4j.LoggerFactory.getLogger(AsyncTaskFactory.class)
                    .error("异步记录日志失败: {}", e.getMessage());
            }
        });
    }
}
