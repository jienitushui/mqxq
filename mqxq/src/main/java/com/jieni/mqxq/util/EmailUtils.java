package com.jieni.mqxq.util;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.jieni.mqxq.exception.MyException;

/**
 * 邮件工具类
 * 
 * 提供邮件发送的完整功能，包括简单邮件发送、验证码生成、消息队列集成等。
 * 支持配置化的发件人设置、错误处理和日志记录。为用户注册、密码找回等业务提供邮件服务支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class EmailUtils {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送简单邮件
     *
     * @param to      收件人，不能为空
     * @param subject 主题，不能为空
     * @param content 内容，不能为空
     * @ 当参数为空或发送失败时抛出
     */
    public void sendSimpleMail(String to, String subject, String content)  {
        // 参数验证
        if (to == null || to.trim().isEmpty()) {
            throw new MyException("收件人不能为空");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new MyException("邮件主题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new MyException("邮件内容不能为空");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("发送邮件成功: to={}, subject={}", to, subject);
            
        } catch (Exception e) {
            log.error("发送邮件失败: to={}, subject={}", to, subject, e);
            throw new MyException("发送邮件失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机验证码
     *
     * @return 6位数字验证码
     */
    public String randCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * 发送验证码到消息队列
     *
     * @param sendCodeDto 发送验证码DTO
     */
    public void sendCodeToMq(Object sendCodeDto) {
        // 这里可以集成RabbitMQ或其他消息队列
        // 暂时直接发送邮件
        log.info("发送验证码到消息队列: {}", sendCodeDto);
    }
}