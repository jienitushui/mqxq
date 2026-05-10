package com.jieni.mqxq.common.config;


import com.jieni.mqxq.memory.RedisChatMemory;
import com.jieni.mqxq.tools.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringAIConfig {


    /**
     * 构造一个ChatClient对象，放到Spring容器中
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 Advisor messageChatMemoryAdvisor,
                                 Advisor safeGuardAdvisor,
                                 CourseTools courseTools
    ) {
        return chatClientBuilder
                .defaultAdvisors(messageChatMemoryAdvisor, safeGuardAdvisor) // 默认的增强器
                .defaultTools(courseTools) //添加默认工具
                .build();
    }


    @Bean
    public ChatMemory chatMemory() {
        return new RedisChatMemory();
    }

    /**
     * 基于Redis的会话记忆，聊天记忆整合到system message中实现多轮对话
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return new MessageChatMemoryAdvisor(chatMemory);
    }

    @Bean
    public Advisor safeGuardAdvisor() {
        // 敏感词列表（示例数据，建议实际使用时从配置文件或数据库读取）
        List<String> sensitiveWords = List.of("血腥","暴力","伤害","色情","政治","宗教");
        // 创建安全防护Advisor，参数依次为：敏感词库、违规提示语、advisor处理优先级，数字越小越优先
        return new SafeGuardAdvisor(
                sensitiveWords,
                "敏感词提示：请勿输入敏感词！",
                Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER
        );
    }

}
