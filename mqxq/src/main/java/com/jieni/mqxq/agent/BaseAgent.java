package com.jieni.mqxq.agent;

import cn.hutool.core.util.StrUtil;
import com.jieni.mqxq.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();
    
    // 消息历史最大长度（用于限制token消耗）
    // 设置为0表示不限制，设置为正数表示只保留最近的N条消息
    private int maxMessageHistory = 0;
    
    // 工具返回结果最大长度（用于限制token消耗）
    // 设置为0表示不限制，设置为正数表示超过该长度的结果会被截断
    private int maxToolResponseLength = 0;

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1、基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 2、执行，更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            // 如果 maxSteps <= 0，表示无限制执行，只检查状态
            int stepNumber = 0;
            while (state != AgentState.FINISHED && (maxSteps <= 0 || stepNumber < maxSteps)) {
                stepNumber++;
                currentStep = stepNumber;
                if (maxSteps > 0) {
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                } else {
                    log.info("Executing step {} (unlimited)", stepNumber);
                }
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制（仅在有限制时检查）
            if (maxSteps > 0 && currentStep >= maxSteps && state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 3、清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时
        // 用于跟踪SSE连接是否仍然有效
        final boolean[] sseConnectionActive = {true};
        
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
            // 1、基础校验
            try {
                if (this.state != AgentState.IDLE) {
                    if (sseConnectionActive[0]) {
                        sseEmitter.send("错误：无法从状态运行代理：" + this.state);
                        sseEmitter.complete();
                    }
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    if (sseConnectionActive[0]) {
                        sseEmitter.send("错误：不能使用空提示词运行代理");
                        sseEmitter.complete();
                    }
                    return;
                }
            } catch (Exception e) {
                if (sseConnectionActive[0]) {
                    sseEmitter.completeWithError(e);
                }
            }
            // 2、执行，更改状态
            this.state = AgentState.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                // 执行循环
                // 如果 maxSteps <= 0，表示无限制执行，只检查状态
                int stepNumber = 0;
                while (state != AgentState.FINISHED && sseConnectionActive[0] && (maxSteps <= 0 || stepNumber < maxSteps)) {
                    stepNumber++;
                    currentStep = stepNumber;
                    if (maxSteps > 0) {
                        log.info("Executing step {}/{}", stepNumber, maxSteps);
                    } else {
                        log.info("Executing step {} (unlimited)", stepNumber);
                    }
                    // 单步执行
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    // 输出当前每一步的结果到 SSE（检查连接状态）
                    if (sseConnectionActive[0]) {
                        try {
                            sseEmitter.send(result);
                        } catch (IllegalStateException e) {
                            // SSE连接已关闭，停止执行
                            log.warn("SSE connection closed, stopping agent execution");
                            sseConnectionActive[0] = false;
                            break;
                        } catch (IOException e) {
                            log.warn("Error sending SSE data, stopping agent execution", e);
                            sseConnectionActive[0] = false;
                            break;
                        }
                    } else {
                        // 连接已关闭，停止执行
                        break;
                    }
                }
                // 检查是否超出步骤限制（仅在有限制时检查）
                if (sseConnectionActive[0] && maxSteps > 0 && currentStep >= maxSteps && state != AgentState.FINISHED) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    try {
                        sseEmitter.send("执行结束：达到最大步骤（" + maxSteps + "）");
                        sseEmitter.complete();
                    } catch (IllegalStateException | IOException e) {
                        log.warn("SSE connection already closed when trying to send completion message");
                    }
                } else if (sseConnectionActive[0]) {
                    // 正常完成
                    try {
                        sseEmitter.complete();
                    } catch (IllegalStateException e) {
                        log.warn("SSE connection already closed when trying to complete");
                    }
                }
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent", e);
                if (sseConnectionActive[0]) {
                    try {
                        sseEmitter.send("执行错误：" + e.getMessage());
                        sseEmitter.complete();
                    } catch (IllegalStateException | IOException ex) {
                        log.warn("SSE connection already closed when trying to send error message");
                    }
                }
            } finally {
                // 3、清理资源
                this.cleanup();
            }
        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            sseConnectionActive[0] = false;
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
            // 尝试取消后台任务（如果还在运行）
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            sseConnectionActive[0] = false;
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        // 设置错误回调
        sseEmitter.onError((ex) -> {
            sseConnectionActive[0] = false;
            this.state = AgentState.ERROR;
            this.cleanup();
            log.error("SSE connection error", ex);
            // 尝试取消后台任务（如果还在运行）
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
        return sseEmitter;
    }

    /**
     * 定义单个步骤
     *
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
    
    /**
     * 限制消息历史长度，避免token消耗过大
     * 保留用户初始消息和最近的对话，同时保持消息的配对关系
     * 重要：确保 ToolResponseMessage 总是与其前面的 AssistantMessage 配对
     */
    protected void limitMessageHistory() {
        if (maxMessageHistory <= 0 || messageList.size() <= maxMessageHistory) {
            return;
        }
        
        int originalSize = messageList.size();
        
        // 保留第一条用户消息（初始任务描述）
        Message firstUserMessage = null;
        int firstUserMessageIndex = -1;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i) instanceof UserMessage) {
                firstUserMessage = messageList.get(i);
                firstUserMessageIndex = i;
                break;
            }
        }
        
        // 计算需要保留的消息数量
        int keepCount = maxMessageHistory;
        if (firstUserMessage != null && firstUserMessageIndex >= 0) {
            // 为第一条用户消息留出空间
            keepCount = Math.max(1, maxMessageHistory - 1);
        }
        
        // 从后往前取最近的N条消息
        int startIndex = messageList.size() - keepCount;
        if (startIndex < 0) {
            startIndex = 0;
        }
        
        // 检查起始位置的消息类型，确保消息配对关系正确
        // 如果起始位置是 ToolResponseMessage，需要向前查找对应的 AssistantMessage
        if (startIndex < messageList.size()) {
            Message startMessage = messageList.get(startIndex);
            if (startMessage instanceof ToolResponseMessage) {
                // 向前查找对应的 AssistantMessage（包含 tool_calls）
                int assistantIndex = -1;
                for (int i = startIndex - 1; i >= 0; i--) {
                    Message msg = messageList.get(i);
                    if (msg instanceof AssistantMessage) {
                        AssistantMessage assistantMsg = (AssistantMessage) msg;
                        // 检查是否包含 tool_calls
                        if (assistantMsg.getToolCalls() != null && !assistantMsg.getToolCalls().isEmpty()) {
                            assistantIndex = i;
                            break;
                        }
                    }
                    // 如果遇到其他类型的消息，停止查找
                    if (!(msg instanceof ToolResponseMessage)) {
                        break;
                    }
                }
                // 如果找到了对应的 AssistantMessage，调整起始位置
                if (assistantIndex >= 0) {
                    startIndex = assistantIndex;
                } else {
                    // 如果没找到，跳过这个 ToolResponseMessage（因为它没有对应的 AssistantMessage）
                    startIndex++;
                }
            }
        }
        
        List<Message> limitedList = new ArrayList<>();
        // 如果有第一条用户消息，且它不在保留范围内，先添加它
        if (firstUserMessage != null && firstUserMessageIndex >= 0 && firstUserMessageIndex < startIndex) {
            limitedList.add(firstUserMessage);
        }
        // 添加最近的消息（跳过已经在limitedList中的第一条用户消息）
        int subListStart = startIndex;
        if (firstUserMessage != null && firstUserMessageIndex >= 0 && firstUserMessageIndex >= startIndex) {
            // 如果第一条用户消息在保留范围内，从它之后开始
            subListStart = Math.max(startIndex, firstUserMessageIndex + 1);
        }
        limitedList.addAll(messageList.subList(subListStart, messageList.size()));
        
        // 最后验证：确保消息序列的完整性
        // 1. 检查第一条消息是否是 ToolResponseMessage（不应该出现）
        while (!limitedList.isEmpty() && limitedList.get(0) instanceof ToolResponseMessage) {
            log.warn("检测到消息历史开头是 ToolResponseMessage，这可能导致错误。跳过该消息。");
            limitedList.remove(0);
        }
        
        // 2. 验证消息序列：确保每个 ToolResponseMessage 前面都有对应的 AssistantMessage
        for (int i = 0; i < limitedList.size(); i++) {
            Message msg = limitedList.get(i);
            if (msg instanceof ToolResponseMessage) {
                // 检查前一条消息是否是包含 tool_calls 的 AssistantMessage
                boolean isValid = false;
                if (i > 0) {
                    Message prevMsg = limitedList.get(i - 1);
                    if (prevMsg instanceof AssistantMessage) {
                        AssistantMessage assistantMsg = (AssistantMessage) prevMsg;
                        if (assistantMsg.getToolCalls() != null && !assistantMsg.getToolCalls().isEmpty()) {
                            isValid = true;
                        }
                    }
                }
                if (!isValid) {
                    log.warn("检测到孤立的 ToolResponseMessage（位置 {}），删除该消息以避免错误。", i);
                    limitedList.remove(i);
                    i--; // 调整索引
                }
            }
        }
        
        messageList = limitedList;
        log.debug("消息历史已限制：从 {} 条减少到 {} 条", originalSize, limitedList.size());
    }
    
    /**
     * 设置消息历史最大长度
     * @param maxHistory 最大历史消息数，0表示不限制
     */
    public void setMaxMessageHistory(int maxHistory) {
        this.maxMessageHistory = maxHistory;
    }
    
    /**
     * 获取消息历史最大长度
     */
    public int getMaxMessageHistory() {
        return maxMessageHistory;
    }
    
    /**
     * 压缩工具返回结果，避免大内容占用过多token
     * 
     * @param responseData 工具返回的原始数据
     * @return 压缩后的数据
     */
    protected String compressToolResponse(String responseData) {
        if (maxToolResponseLength <= 0 || responseData == null) {
            return responseData;
        }
        
        if (responseData.length() <= maxToolResponseLength) {
            return responseData;
        }
        
        // 保留开头和结尾，中间用省略号
        int headLength = maxToolResponseLength / 2;
        int tailLength = maxToolResponseLength - headLength - 100; // 为省略信息留出空间
        
        if (tailLength < 0) {
            tailLength = 0;
        }
        
        String head = responseData.substring(0, headLength);
        String tail = tailLength > 0 ? responseData.substring(responseData.length() - tailLength) : "";
        String compressed = head + 
                "\n\n... (内容已压缩，原始长度: " + responseData.length() + " 字符，已截断) ...\n\n" + 
                tail;
        
        log.debug("工具返回结果已压缩：从 {} 字符减少到 {} 字符", responseData.length(), compressed.length());
        return compressed;
    }
    
    /**
     * 设置工具返回结果最大长度
     * @param maxLength 最大长度（字符数），0表示不限制
     */
    public void setMaxToolResponseLength(int maxLength) {
        this.maxToolResponseLength = maxLength;
    }
    
    /**
     * 获取工具返回结果最大长度
     */
    public int getMaxToolResponseLength() {
        return maxToolResponseLength;
    }
}
