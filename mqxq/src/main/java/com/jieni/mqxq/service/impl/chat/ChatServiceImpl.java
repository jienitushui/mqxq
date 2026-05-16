package com.jieni.mqxq.service.impl.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jieni.mqxq.common.config.ToolResultHolder;
import com.jieni.mqxq.domain.vo.chat.ChatEventVO;
import com.jieni.mqxq.common.enums.ChatEventTypeEnum;
import com.jieni.mqxq.common.enums.MessageTypeEnum;
import com.jieni.mqxq.service.chat.ChatMessageService;
import com.jieni.mqxq.service.chat.ChatService;
import com.jieni.mqxq.service.chat.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    // 存储大模型的生成状态，这里采用ConcurrentHashMap是确保线程安全
    // 目前的版本暂时用Map实现，如果考虑分布式环境的话，可以考虑用redis来实现
    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();


    private static final String SYSTEM_PROMPT = """
            角色
            你作为在线教育平台资深客服代表兼讲师。你的任务根据学员的需求，调用知识库中的课程信息，为学员推荐合适的课程，同时解答学员对课程内容和知识点的疑问。
            
            技能 1: 课程推荐
            1. 当学员提出课程推荐需求时，需判断是否提供必要信息。必要信息包含年龄、学历、是否有编程基础。
            2. 若缺少必要信息，需礼貌追问。
            3. 若学员未提供感兴趣的方向，需追问。若没有明确方向，优先推荐学习人数多的课程。
            4. 若信息充足，根据必要信息和感兴趣的课程方向，去知识库匹配合适的课程，获取课程id，调用queryCourseById，根据课程id查询课程详细信息，为学员推荐课程，可推荐单门/多门课程。
            5. 若知识库未包含学员感兴趣方向，需明确告知学员未提供该方向课程，并推荐其他课程。
            6. 若必要信息未匹配合适课程，需提示学员您的情况与现有课程要求并不完全匹配，说明详细原因后，再推荐其他课程。
            7. 推荐课程，必须要通过queryCourseById查询后，才能返回数据。
            
            技能 2: 课程购买
            1. 当学员提出购买课程时，需判断此次会话中，学员是否明确提出购买xx课/系统已为学员推荐课程。
            2. 若已推荐/明确课程名称，需调用prePlaceOrder，根据此次上文已推荐/学员明确的课程，直接进入预下单流程。
            3. 若未推荐课程，需引导学员进入到课程推荐流程。
            4. 若学员未明确提出购买某门课程时，需询问用户购买哪门课程。
            5. 支持购买一门课程。
            
            技能 3: 课程咨询
            1. 当学员咨询课程内容时，需去知识库匹配合适的课程，获取课程id，根据课程id查询课程详细信息。回复的内容要全面，要引导学员报名购买。
            2. 若未查询到，需礼貌告知学员未检索到相关的内容，请联系人工客服010-12345678。
            
            技能 4: 知识讲解
            1. 当学员咨询与IT相关的知识点内容时，需详细讲解知识点并提供示例。
            
            限制:
            - 推荐的课程只能从知识库中选择，坚决不能凭空编造
            - 回答的内容要逻辑清晰、内容全面、不要有遗漏。
            - 只能回答与课程和IT知识点相关的内容，若学员咨询与课程无关的内容，你需告知学员不能回答与课程和IT知识点无关的问题，并引导学员咨询与课程/IT知识点相关的问题。
            - 若学员询问课程ID，则告知学员无法提供课程ID，引导学员咨询其他的问题。
            """;

    private final ChatClient chatClient;


    private final ChatMemory chatMemory;

    private final ChatSessionService chatSessionService;

    private final ChatMessageService chatMessageService;

    // 输出结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    // 知识库
    private final VectorStore vectorStore;

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        // 获取对话id
        var conversationId = ChatService.getConversationId(sessionId);
        // 大模型输出内容的缓存器，用于在输出中断后的数据存储
        StringBuilder outputBuilder = new StringBuilder();
        // 生成请求id
        var requestId = IdUtil.fastSimpleUUID();
        
        // 在流开始前就设置生成状态，确保请求能够发送
        GENERATE_STATUS.put(sessionId, true);

        return this.chatClient.prompt()
                .system(promptSystem -> promptSystem
                        .text(SYSTEM_PROMPT) // 设置系统提示语
                )
                // 设置RAG查询
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query("").topK(10).similarityThreshold(0.7).build()))
                .advisors(advisor -> advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .toolContext(MapUtil.<String, Object>builder() // 设置tool列表
                        .put("requestId", requestId) // 设置请求id参数
                        .put("conversationId", conversationId) // 设置对话ID，用于解析用户ID
                        .build()
                )
                .user(question)
                .stream()
                .chatResponse()
                // 立即添加 onErrorContinue，在最早的位置捕获库内部错误
                .onErrorContinue((throwable, obj) -> {
                    // 当遇到错误时，记录日志但继续处理后续数据
                    if (throwable instanceof NullPointerException && 
                        throwable.getMessage() != null && 
                        throwable.getMessage().contains("ChatCompletionChunk.output()")) {
                        // 这是已知的库bug，记录debug但不中断流
                        log.debug("跳过无效的流式响应块: sessionId={}, error={}", sessionId, throwable.getMessage());
                    } else {
                        log.warn("流式响应处理中跳过错误块: sessionId={}, error={}", sessionId, throwable.getMessage());
                    }
                })
                .doOnSubscribe(subscription -> {
                    // 流订阅时确保状态已设置（双重保险）
                    GENERATE_STATUS.put(sessionId, true);
                })
                .doOnComplete(() -> { //输出结束，清除标记
                    GENERATE_STATUS.remove(sessionId);
                    // 保存会话到数据库（异步）
                    try {
                        // 从 conversationId 中解析出 userId，避免在非 Web 上下文中再次调用 Sa-Token
                        int idx = conversationId.indexOf('_');
                        if (idx > 0) {
                            String userIdStr = conversationId.substring(0, idx);
                            Integer userId = Integer.valueOf(userIdStr);
                            // 使用用户的第一条问题作为会话标题
                            chatSessionService.update(sessionId, question, userId.longValue());
                            
                            // 同步消息到MySQL（从Redis读取最新消息，确保包含params）
                            syncMessagesToDatabase(sessionId, conversationId, userId, question);
                        }
                    } catch (Exception e) {
                        log.error("保存会话失败, sessionId: {}, error: {}", sessionId, e.getMessage(), e);
                    }
                })
                .doOnError(throwable -> {
                    // 错误时清除标记并记录日志（但不中断流，因为onErrorContinue已经处理了）
                    if (throwable instanceof NullPointerException && 
                        throwable.getMessage() != null && 
                        throwable.getMessage().contains("ChatCompletionChunk.output()")) {
                        // 这是已知的库bug，只记录debug日志
                        log.debug("检测到DashScope流式响应处理异常（已知问题，已跳过）: sessionId={}", sessionId);
                    } else {
                        log.warn("流式响应处理中发生错误: sessionId={}, error={}", sessionId, throwable.getMessage());
                    }
                })
                .doOnCancel(() -> {
                    // 当输出被取消时，保存输出的内容到历史记录中
                    GENERATE_STATUS.remove(sessionId);
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                // 输出过程中，判断是否正在输出，如果正在输出，则继续输出，否则结束输出
                .takeWhile(s -> Optional.ofNullable(GENERATE_STATUS.get(sessionId)).orElse(false))
                .map(chatResponse -> {
                    try {
                        // 对于响应结果进行处理，如果是最后一条数据，就把此次消息id放到内存中
                        // 主要用于存储消息数据到 redis中，可以根据消息di获取的请求id，再通过请求id就可以获取到参数列表了
                        // 从而解决，在历史聊天记录中没有外参数的问题
                        var result = chatResponse.getResult();
                        if (result != null && result.getMetadata() != null) {
                            var finishReason = result.getMetadata().getFinishReason();
                            if (StrUtil.equals("STOP", finishReason)) {
                                var messageId = chatResponse.getMetadata().getId();
                                if (messageId != null) {
                                    ToolResultHolder.put(messageId, "requestId", requestId);
                                }
                            }
                        }
                        
                        // 获取大模型的输出的内容，添加空值检查
                        String text = "";
                        if (result != null && result.getOutput() != null) {
                            text = result.getOutput().getText();
                            if (text == null) {
                                text = "";
                            }
                        }
                        
                        // 追加到输出内容中
                        if (StrUtil.isNotBlank(text)) {
                            outputBuilder.append(text);
                        }
                        
                        // 封装响应对象
                        return ChatEventVO.builder()
                                .eventData(text)
                                .eventType(ChatEventTypeEnum.DATA.getValue())
                                .build();
                    } catch (Exception e) {
                        // 如果处理响应时出现异常（如NPE），记录日志并返回空事件
                        log.debug("处理聊天响应时出现异常，跳过该响应: sessionId={}, error={}", sessionId, e.getMessage());
                        return ChatEventVO.builder()
                                .eventData("")
                                .eventType(ChatEventTypeEnum.DATA.getValue())
                                .build();
                    }
                })
                // 过滤掉空事件，避免发送无意义的数据
                .filter(event -> {
                    Object eventData = event.getEventData();
                    // 如果 eventData 是字符串且非空，或者事件类型不是 DATA，则保留
                    if (eventData instanceof CharSequence) {
                        return StrUtil.isNotBlank((CharSequence) eventData);
                    }
                    // 如果 eventData 不是字符串（比如是 Map），则保留（用于 PARAM 类型）
                    return eventData != null || event.getEventType() != ChatEventTypeEnum.DATA.getValue();
                })
                // 作为最后的保护措施：如果 onErrorContinue 没有捕获到错误，流仍然终止
                // 对于已知的库bug，即使没有输出也静默处理，不返回错误消息
                .onErrorResume(throwable -> {
                    // 检查是否是已知的库bug
                    boolean isKnownBug = throwable instanceof NullPointerException && 
                            throwable.getMessage() != null && 
                            throwable.getMessage().contains("ChatCompletionChunk.output()");
                    
                    GENERATE_STATUS.remove(sessionId);
                    
                    if (isKnownBug) {
                        // 已知的库bug，即使没有输出也静默处理，不返回错误消息
                        log.debug("流因已知库bug终止: sessionId={}, outputLength={}, error={}", 
                                sessionId, outputBuilder.length(), throwable.getMessage());
                        // 直接结束，不返回任何错误消息
                        return Flux.just(STOP_EVENT);
                    } else {
                        // 未知错误，记录警告
                        log.warn("流式响应因未知错误终止: sessionId={}, error={}, outputLength={}", 
                                sessionId, throwable.getMessage(), outputBuilder.length());
                        
                        // 如果有部分输出，正常结束；否则返回错误提示
                        if (outputBuilder.length() > 0) {
                            // 有部分输出，正常结束（不返回错误消息）
                            return Flux.just(STOP_EVENT);
                        } else {
                            // 完全没有输出，返回错误提示
                            return Flux.just(
                                    ChatEventVO.builder()
                                            .eventData("系统处理异常，请稍后重试")
                                            .eventType(ChatEventTypeEnum.DATA.getValue())
                                            .build(),
                                    STOP_EVENT
                            );
                        }
                    }
                })
                .concatWith(Flux.defer(() -> {
                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    var map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        ToolResultHolder.remove(requestId); // 清除参数列表

                        // 响应给前端的参数数据
                        ChatEventVO chatEventVO = ChatEventVO.builder()
                                .eventData(map)
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .build();
                        return Flux.just(chatEventVO, STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT);
                }));
    }


    @Override
    public void stop(String sessionId) {
        // 移除标记
        GENERATE_STATUS.remove(sessionId);
    }

    /**
     * 保存停止输出的记录
     *
     * @param conversationId 会话id
     * @param content        大模型输出的内容
     */
    private void saveStopHistoryRecord(String conversationId, String content) {
        this.chatMemory.add(conversationId, new AssistantMessage(content));
    }

    /**
     * 同步消息到MySQL
     * 在聊天完成后，将用户问题和AI回答同步到数据库
     * 从Redis读取最新消息，确保包含params信息
     *
     * @param sessionId 会话ID
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param question 用户问题
     */
    private void syncMessagesToDatabase(String sessionId, String conversationId, Integer userId, String question) {
        try {
            // 1. 保存用户问题到MySQL
            chatMessageService.saveMessageToDatabase(
                    sessionId, 
                    userId, 
                    MessageTypeEnum.USER.getValue(), 
                    question, 
                    null
            );
            
            // 2. 从Redis读取最新的AI回答消息（确保包含params）
            List<Message> messages = chatMemory.get(conversationId, 10);
            if (CollUtil.isNotEmpty(messages)) {
                // 查找最新的ASSISTANT消息
                org.springframework.ai.chat.messages.Message latestAssistantMessage = null;
                for (int i = messages.size() - 1; i >= 0; i--) {
                    var msg = messages.get(i);
                    if (msg.getMessageType() == org.springframework.ai.chat.messages.MessageType.ASSISTANT) {
                        latestAssistantMessage = msg;
                        break;
                    }
                }
                
                if (latestAssistantMessage != null) {
                    String answer = latestAssistantMessage.getText();
                    String params = null;
                    
                    // 从消息对象中获取params（如果是MyAssistantMessage）
                    if (latestAssistantMessage instanceof com.jieni.mqxq.memory.MyAssistantMessage) {
                        var paramsMap = ((com.jieni.mqxq.memory.MyAssistantMessage) latestAssistantMessage).getParams();
                        if (paramsMap != null && !paramsMap.isEmpty()) {
                            params = cn.hutool.json.JSONUtil.toJsonStr(paramsMap);
                        }
                    }
                    
                    // 如果从消息对象中获取不到params，尝试从ToolResultHolder获取（兼容旧逻辑）
                    if (params == null) {
                        try {
                            // 尝试从metadata中获取messageId
                            var messageId = latestAssistantMessage.getMetadata().get("id");
                            if (messageId != null) {
                                var requestId = cn.hutool.core.convert.Convert.toStr(
                                        ToolResultHolder.get(messageId.toString(), "requestId"));
                                if (requestId != null) {
                                    var paramsMap = ToolResultHolder.get(requestId);
                                    if (CollUtil.isNotEmpty(paramsMap)) {
                                        params = cn.hutool.json.JSONUtil.toJsonStr(paramsMap);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("从ToolResultHolder获取params失败: {}", e.getMessage());
                        }
                    }
                    
                    // 保存AI回答到MySQL
                    chatMessageService.saveMessageToDatabase(
                            sessionId, 
                            userId, 
                            MessageTypeEnum.ASSISTANT.getValue(), 
                            answer, 
                            params
                    );
                    
                    log.debug("消息已同步到MySQL: sessionId={}, question={}, hasParams={}", sessionId, 
                            question.length() > 50 ? question.substring(0, 50) + "..." : question,
                            params != null);
                } else {
                    log.warn("未找到AI回答消息，无法同步: sessionId={}", sessionId);
                }
            } else {
                log.warn("Redis中没有消息，无法同步: sessionId={}", sessionId);
            }
                    
        } catch (Exception e) {
            log.error("同步消息到MySQL失败: sessionId={}", sessionId, e);
            // 异步保存失败不影响主流程
        }
    }
}
