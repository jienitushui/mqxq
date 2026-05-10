package com.jieni.mqxq.agent;

import com.jieni.mqxq.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class YuManus extends ToolCallAgent {

    public YuManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("yuManus");
        // 优化后的系统提示词：更简洁，减少token消耗
        // 系统提示词只在第一次调用时发送，所以可以包含详细说明
        String SYSTEM_PROMPT = """
                You are YuManus, an AI assistant that solves user tasks using available tools.
                
                Rules:
                1. Call doTerminate when ALL tasks are complete or cannot proceed.
                2. Tool parameters must be valid JSON. Escape special chars (\\n, \\", \\\\).
                3. Break complex tasks into steps. Explain results after each tool call.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        // 优化后的下一步提示词：大幅精简，因为每次think()都会发送
        // 从 ~300 tokens 减少到 ~30 tokens，每次调用节省 ~270 tokens
        String NEXT_STEP_PROMPT = """
                Select appropriate tools. Break complex tasks into steps. Call doTerminate when done.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        // 设置为0表示无限制执行，直到任务完成或调用doTerminate
        // 如果需要限制，可以设置为一个较大的值，如50或100
//        this.setMaxSteps(0);
        this.setMaxSteps(13);
        // 限制消息历史长度，避免token消耗过大
        // 保留最近的15条消息（包括用户消息、AI回复、工具调用结果）
        // 从20减少到15，进一步减少token消耗，同时保持必要的上下文
        this.setMaxMessageHistory(15);
        
        // 限制工具返回结果的最大长度，避免大内容占用过多token
        // 设置为2000字符，超过此长度的工具返回结果会被压缩（保留开头和结尾）
        // 这样可以防止readFile读取大文件、searchWeb返回大量结果等情况导致的token激增
        this.setMaxToolResponseLength(2000);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
