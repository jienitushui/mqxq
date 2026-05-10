package com.jieni.mqxq.mq;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.jieni.mqxq.domain.dto.auth.SendCodeDto;
import com.jieni.mqxq.util.EmailUtils;

/**
 * 邮箱消息交换类
 * 
 * 提供邮箱相关的消息队列操作，包括邮箱验证码发送、通知邮件等功能
 * 支持RabbitMQ消息监听和异步处理，提高系统性能和用户体验
 * 实现邮件发送的可靠性保证和失败重试机制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */

@Component
@Slf4j
public class EmailExchange {
    @Resource
    EmailUtils emailUtils;
    /**
     * @Description 发送验证码
     * @Param sendCodeDto 验证码消息
     **/
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "mqxq.email"),
            exchange = @Exchange(name = "mqxq.email"),
            key = {"sendCode"}))
    public void listenSendCode(SendCodeDto sendCodeDto)  {
        try {
            log.info("收到发送验证码的消息：receiver={}, code={}, subject={}", 
                    sendCodeDto.getReceiver(), sendCodeDto.getCode(), sendCodeDto.getSubject());
            
            // 异步发送邮件
            emailUtils.sendSimpleMail(
                    sendCodeDto.getReceiver(), 
                    sendCodeDto.getSubject(), 
                    sendCodeDto.getContent()
            );
            
            log.info("验证码邮件发送成功：receiver={}", sendCodeDto.getReceiver());
        } catch (Exception e) {
            log.error("发送验证码邮件失败：receiver={}", sendCodeDto.getReceiver(), e);
            // 注意：这里可以添加重试机制或死信队列处理
            throw e; // 抛出异常以便RabbitMQ进行重试
        }
    }

}
