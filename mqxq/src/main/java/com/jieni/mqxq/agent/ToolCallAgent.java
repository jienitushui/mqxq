package com.jieni.mqxq.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jieni.mqxq.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果（要调用那些工具）
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    // 连续无工具调用的次数（用于检测任务是否已完成）
    private int consecutiveNoToolCallCount = 0;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 修复JSON字符串中的转义问题和结构问题
     * 主要处理：
     * 1. 未转义的换行符、制表符等控制字符
     * 2. 嵌套数组结构问题（如 textContents 字段中的嵌套数组）
     * 
     * @param jsonString 可能包含未转义字符或结构问题的JSON字符串
     * @return 修复后的JSON字符串
     */
    private String fixJsonString(String jsonString) {
        if (StrUtil.isBlank(jsonString)) {
            return jsonString;
        }
        
        try {
            // 先尝试解析，如果成功则直接返回
            ObjectMapper mapper = new ObjectMapper();
            mapper.readValue(jsonString, Map.class);
            return jsonString;
        } catch (JsonProcessingException e) {
            // JSON解析失败，尝试修复常见问题
            log.warn("检测到JSON格式问题，尝试修复: {}", e.getMessage());
            
            // 第一步：使用正则表达式快速修复数组中的字符串（处理换行符等）
            String fixedString = fixJsonWithRegex(jsonString);
            
            // 第二步：修复转义问题
            fixedString = fixJsonEscaping(fixedString);
            
            // 第三步：修复结构问题（嵌套数组）
            fixedString = fixJsonStructure(fixedString);
            
            // 再次尝试解析
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.readValue(fixedString, Map.class);
                log.info("JSON修复成功");
                return fixedString;
            } catch (JsonProcessingException e2) {
                log.error("JSON修复失败，返回原始字符串: {}", e2.getMessage());
                // 最后一次尝试：使用更激进的方法
                String lastTry = fixJsonAggressively(jsonString);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.readValue(lastTry, Map.class);
                    log.info("使用激进方法修复JSON成功");
                    return lastTry;
                } catch (JsonProcessingException e3) {
                    log.error("所有JSON修复方法都失败");
                    return jsonString;
                }
            }
        }
    }
    
    /**
     * 使用正则表达式快速修复JSON中的常见问题
     * 特别是数组中的字符串包含换行符的情况
     * 这个方法与fixJsonEscaping类似，但更专注于快速修复
     */
    private String fixJsonWithRegex(String jsonString) {
        // 直接调用fixJsonEscaping，因为它已经能处理换行符
        // 这个方法主要用于预处理，确保在fixJsonEscaping之前处理一些特殊情况
        return jsonString;
    }
    
    /**
     * 更激进的JSON修复方法
     * 当常规方法失败时使用
     * 尝试多次修复转义问题，确保所有换行符都被正确转义
     */
    private String fixJsonAggressively(String jsonString) {
        // 多次应用转义修复，确保所有问题都被解决
        String result = jsonString;
        for (int i = 0; i < 3; i++) {
            String previous = result;
            result = fixJsonEscaping(result);
            if (previous.equals(result)) {
                break; // 没有更多修复需要
            }
        }
        
        // 尝试解析
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readValue(result, Map.class);
            return result;
        } catch (Exception e) {
            log.warn("激进修复方法仍然失败: {}", e.getMessage());
            return jsonString;
        }
    }
    
    /**
     * 修复JSON字符串中的转义问题
     * 主要处理未转义的换行符、制表符等控制字符
     * 增强版：更好地处理数组中的字符串
     */
    private String fixJsonEscaping(String jsonString) {
        StringBuilder fixed = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        int stringStart = -1;
        
        // 逐字符处理，正确识别字符串边界并转义控制字符
        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            
            if (escaped) {
                // 当前字符是转义序列的一部分
                fixed.append(c);
                escaped = false;
            } else if (c == '\\') {
                // 转义字符
                fixed.append(c);
                escaped = true;
            } else if (c == '"') {
                // 检查是否是转义的引号（前面有奇数个反斜杠）
                int backslashCount = 0;
                for (int j = i - 1; j >= 0 && jsonString.charAt(j) == '\\'; j--) {
                    backslashCount++;
                }
                
                if (backslashCount % 2 == 0) {
                    // 这是真正的字符串边界
                    fixed.append(c);
                    inString = !inString;
                    if (inString) {
                        stringStart = fixed.length() - 1;
                    }
                } else {
                    // 这是转义的引号，在字符串内部
                    fixed.append(c);
                }
            } else if (inString) {
                // 在字符串内部，需要转义控制字符
                if (c == '\n') {
                    fixed.append("\\n");
                } else if (c == '\r') {
                    fixed.append("\\r");
                } else if (c == '\t') {
                    fixed.append("\\t");
                } else if (c == '\b') {
                    fixed.append("\\b");
                } else if (c == '\f') {
                    fixed.append("\\f");
                } else if (c == '\u0000') {
                    // NULL字符，必须转义
                    fixed.append("\\u0000");
                } else if (c < 32) {
                    // 其他控制字符，使用Unicode转义
                    fixed.append(String.format("\\u%04x", (int) c));
                } else if (c == '\u2028' || c == '\u2029') {
                    // JSON不允许的Unicode行分隔符，必须转义
                    fixed.append(String.format("\\u%04x", (int) c));
                } else {
                    fixed.append(c);
                }
            } else {
                // 在字符串外部，直接添加
                fixed.append(c);
            }
        }
        
        return fixed.toString();
    }
    
    /**
     * 修复JSON结构问题，特别是嵌套数组问题
     * 例如：{"textContents": ["str1", ["str2"], ["str3"]]} 
     * 修复为：{"textContents": ["str1", "str2", "str3"]}
     */
    private String fixJsonStructure(String jsonString) {
        // 尝试使用Jackson解析为Map，然后递归修复嵌套数组
        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.readValue(jsonString, Map.class);
            
            // 递归修复嵌套数组
            Map<String, Object> fixedMap = fixNestedArrays(map);
            
            // 重新序列化为JSON
            return mapper.writeValueAsString(fixedMap);
        } catch (Exception e) {
            // 如果解析失败，尝试使用正则表达式修复常见的嵌套数组模式
            return fixNestedArraysWithRegex(jsonString);
        }
    }
    
    /**
     * 递归修复Map中的嵌套数组问题
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fixNestedArrays(Map<String, Object> map) {
        Map<String, Object> fixedMap = new java.util.HashMap<>();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof List) {
                List<Object> list = (List<Object>) value;
                List<Object> fixedList = fixNestedList(list);
                fixedMap.put(key, fixedList);
            } else if (value instanceof Map) {
                fixedMap.put(key, fixNestedArrays((Map<String, Object>) value));
            } else {
                fixedMap.put(key, value);
            }
        }
        
        return fixedMap;
    }
    
    /**
     * 递归修复List中的嵌套数组问题
     * 将嵌套的数组展平为单个数组
     */
    @SuppressWarnings("unchecked")
    private List<Object> fixNestedList(List<Object> list) {
        List<Object> fixedList = new java.util.ArrayList<>();
        
        for (Object item : list) {
            if (item instanceof List) {
                // 如果元素本身是List，递归处理并展平
                List<Object> nestedList = fixNestedList((List<Object>) item);
                // 如果嵌套的List只包含一个字符串元素，直接添加该字符串
                if (nestedList.size() == 1 && nestedList.get(0) instanceof String) {
                    fixedList.add(nestedList.get(0));
                } else {
                    // 否则展平所有元素
                    fixedList.addAll(nestedList);
                }
            } else if (item instanceof Map) {
                fixedList.add(fixNestedArrays((Map<String, Object>) item));
            } else {
                fixedList.add(item);
            }
        }
        
        return fixedList;
    }
    
    /**
     * 使用正则表达式修复常见的嵌套数组模式
     * 这是一个后备方案，当JSON无法解析时使用
     * 修复模式：["str1", ["str2"], ["str3"]] -> ["str1", "str2", "str3"]
     */
    private String fixNestedArraysWithRegex(String jsonString) {
        String result = jsonString;
        
        // 步骤1: 修复 ], [" 模式（嵌套数组的结束和开始，后跟字符串）
        // 这个模式出现在数组内部，表示一个嵌套数组结束，另一个字符串数组开始
        // 我们需要将其替换为 , " 来展平数组
        // 例如："], [" -> ", "
        result = result.replaceAll("\\]\\s*,\\s*\\[\"", ", \"");
        
        // 步骤2: 修复数组内部单独的嵌套数组 ["..."]，将其展平为 "..."
        // 匹配模式：, ["..."] 或 ["..."] , 在数组内部
        // 需要处理多行字符串（包含 \n 等）
        String nestedArrayPattern = ",\\s*\\[\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\]";
        result = result.replaceAll(nestedArrayPattern, ", \"$1\"");
        
        // 步骤3: 修复数组开头的嵌套数组 ["..."]
        nestedArrayPattern = "\\[\\s*\\[\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\]";
        result = result.replaceAll(nestedArrayPattern, "[\"$1\"");
        
        // 步骤4: 修复 ], [ 模式（嵌套数组的结束和开始，不带引号，可能是其他类型）
        // 这种情况较少见，但也要处理
        result = result.replaceAll("\\]\\s*,\\s*\\[", ", ");
        
        // 步骤5: 清理可能产生的多余逗号和空格
        result = result.replaceAll(",\\s*,+", ",");  // 多个连续逗号
        result = result.replaceAll("\\[\\s*,+", "[");  // 数组开头后的逗号
        result = result.replaceAll(",+\\s*\\]", "]");  // 数组结尾前的逗号
        result = result.replaceAll(",\\s+", ", ");     // 规范化逗号后的空格
        
        return result;
    }
    
    /**
     * 修复工具调用参数中的JSON格式问题
     * 
     * @param toolCall 原始工具调用
     * @return 修复后的工具调用参数JSON字符串
     */
    private String fixToolCallArguments(AssistantMessage.ToolCall toolCall) {
        String arguments = toolCall.arguments();
        if (StrUtil.isBlank(arguments)) {
            return arguments;
        }
        return fixJsonString(arguments);
    }

    /**
     * 智能判断任务是否已完成
     * 通过分析AI返回的文本内容，判断是否表明任务已完成
     * 
     * @param aiResponse AI返回的文本内容
     * @return true表示任务已完成，false表示任务未完成
     */
    private boolean isTaskCompleted(String aiResponse) {
        if (StrUtil.isBlank(aiResponse)) {
            return false;
        }
        
        String lowerResponse = aiResponse.toLowerCase();
        
        // 任务完成的关键词
        String[] completionKeywords = {
            "任务完成", "已完成", "完成", "任务结束", "已结束", "结束", 
            "finished", "completed", "done", "task completed", "all done",
            "任务已全部完成", "所有任务已完成", "工作完成", "处理完成",
            "执行完成", "操作完成", "已完成所有", "全部完成"
        };
        
        // 任务未完成或需要继续的关键词
        String[] continuationKeywords = {
            "还需要", "接下来", "下一步", "继续", "还需要", "待处理",
            "还需要", "还需要", "还需要", "还需要", "还需要"
        };
        
        // 检查是否包含完成关键词
        boolean hasCompletionKeyword = false;
        for (String keyword : completionKeywords) {
            if (lowerResponse.contains(keyword.toLowerCase())) {
                hasCompletionKeyword = true;
                break;
            }
        }
        
        // 如果包含完成关键词，且不包含继续关键词，则认为任务已完成
        if (hasCompletionKeyword) {
            boolean hasContinuationKeyword = false;
            for (String keyword : continuationKeywords) {
                if (lowerResponse.contains(keyword.toLowerCase())) {
                    hasContinuationKeyword = true;
                    break;
                }
            }
            return !hasContinuationKeyword;
        }
        
        return false;
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 如果是第一步，重置连续无工具调用计数器
        if (getCurrentStep() == 1) {
            consecutiveNoToolCallCount = 0;
        }
        // 1、校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        // 2、限制消息历史长度，避免token消耗过大
        limitMessageHistory();
        
        // 3、调用 AI 大模型，获取工具调用结果
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于等下 Act
            this.toolCallChatResponse = chatResponse;
            // 3、解析工具调用结果，获取要调用的工具
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            
            // 验证并修复工具调用参数中的JSON格式问题
            if (!toolCallList.isEmpty()) {
                for (AssistantMessage.ToolCall toolCall : toolCallList) {
                    String originalArgs = toolCall.arguments();
                    if (StrUtil.isNotBlank(originalArgs)) {
                        // 立即修复并应用
                        fixToolCallArgumentsWithReflection(toolCall);
                        String fixedArgs = toolCall.arguments();
                        if (!originalArgs.equals(fixedArgs)) {
                            log.info("工具 {} 的参数JSON已修复（think阶段）", toolCall.name());
                        }
                    }
                }
            }
            
            // 输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 如果不需要调用工具，检查是否应该自动终止
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                // 增加连续无工具调用计数
                consecutiveNoToolCallCount++;
                // 智能判断任务是否已完成
                if (isTaskCompleted(result)) {
                    log.info("检测到任务已完成，自动终止");
                    setState(AgentState.FINISHED);
                } else if (consecutiveNoToolCallCount >= 3) {
                    // 如果连续3次没有工具调用，且没有明确表示任务完成，也认为任务已完成
                    log.info("连续{}次无工具调用，自动终止", consecutiveNoToolCallCount);
                    setState(AgentState.FINISHED);
                }
                return false;
            } else {
                // 有工具调用时，重置计数器
                consecutiveNoToolCallCount = 0;
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }

    /**
     * 使用反射修复工具调用参数中的JSON
     * 
     * @param toolCall 工具调用对象
     */
    private void fixToolCallArgumentsWithReflection(AssistantMessage.ToolCall toolCall) {
        try {
            String originalArgs = toolCall.arguments();
            if (StrUtil.isBlank(originalArgs)) {
                return;
            }
            
            String fixedArgs = fixJsonString(originalArgs);
            if (!originalArgs.equals(fixedArgs)) {
                // 尝试使用反射修改arguments字段
                // 注意：ToolCall可能是接口，实际实现类可能不同
                Class<?> clazz = toolCall.getClass();
                try {
                    // 尝试常见的字段名
                    String[] possibleFieldNames = {"arguments", "args", "parameters", "params"};
                    boolean fixed = false;
                    
                    for (String fieldName : possibleFieldNames) {
                        try {
                            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(toolCall, fixedArgs);
                            log.info("已通过反射修复工具 {} 的参数JSON (字段: {})", toolCall.name(), fieldName);
                            fixed = true;
                            break;
                        } catch (NoSuchFieldException e) {
                            // 继续尝试下一个字段名
                            continue;
                        }
                    }
                    
                    if (!fixed) {
                        // 如果所有字段名都失败，尝试查找所有字段
                        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
                        for (java.lang.reflect.Field field : fields) {
                            if (field.getType() == String.class) {
                                try {
                                    field.setAccessible(true);
                                    Object currentValue = field.get(toolCall);
                                    if (originalArgs.equals(currentValue)) {
                                        field.set(toolCall, fixedArgs);
                                        log.info("已通过反射修复工具 {} 的参数JSON (字段: {})", toolCall.name(), field.getName());
                                        fixed = true;
                                        break;
                                    }
                                } catch (IllegalAccessException e) {
                                    // 继续尝试下一个字段
                                    continue;
                                }
                            }
                        }
                    }
                    
                    if (!fixed) {
                        log.warn("无法通过反射修改工具 {} 的参数，ToolCall类型: {}", toolCall.name(), clazz.getName());
                    }
                } catch (Exception e) {
                    log.warn("反射修改工具调用参数时出错: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("修复工具调用参数时出错: {}", e.getMessage());
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        
        // 在执行前修复所有工具调用参数中的JSON格式问题
        try {
            AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
            if (assistantMessage != null) {
                List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
                if (toolCallList != null) {
                    for (AssistantMessage.ToolCall toolCall : toolCallList) {
                        fixToolCallArgumentsWithReflection(toolCall);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("预修复工具调用参数时出错，将继续执行: {}", e.getMessage());
        }
        
        try {
            // 调用工具
            Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
            // 压缩工具返回结果，避免大内容占用过多token
            List<Message> compressedHistory = compressToolResponses(toolExecutionResult.conversationHistory());
            // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
            setMessageList(compressedHistory);
            ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
            // 判断是否调用了终止工具
            boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(response -> response.name().equals("doTerminate"));
            if (terminateToolCalled) {
                // 任务结束，更改状态
                setState(AgentState.FINISHED);
            }
            String results = toolResponseMessage.getResponses().stream()
                    .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                    .collect(Collectors.joining("\n"));
            log.info(results);
            return results;
        } catch (Exception e) {
            // 捕获工具调用过程中的异常，特别是JSON解析错误
            log.error(getName() + "执行工具调用时遇到错误：" + e.getMessage(), e);
            
            // 记录工具调用的详细信息，便于调试
            if (toolCallChatResponse != null && toolCallChatResponse.getResult() != null) {
                AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
                if (assistantMessage != null) {
                    List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
                    if (toolCallList != null && !toolCallList.isEmpty()) {
                        String errorDetails = toolCallList.stream()
                                .map(toolCall -> String.format("工具：%s，参数：%s", 
                                        toolCall.name(), 
                                        toolCall.arguments() != null ? 
                                            (toolCall.arguments().length() > 500 ? 
                                                toolCall.arguments().substring(0, 500) + "..." : 
                                                toolCall.arguments()) : "null"))
                                .collect(Collectors.joining("\n"));
                        log.error("工具调用详情：\n" + errorDetails);
                    }
                }
            }
            
            // 返回友好的错误信息
            String errorMessage = "工具调用失败：" + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "，原因：" + e.getCause().getMessage();
            }
            
            // 如果是JSON解析错误，尝试提取并修复JSON
            if (e.getMessage() != null && (e.getMessage().contains("JSON") || 
                    (e.getCause() != null && e.getCause().getMessage() != null && 
                     e.getCause().getMessage().contains("JSON")))) {
                
                // 尝试从工具调用中提取并修复JSON
                if (toolCallChatResponse != null && toolCallChatResponse.getResult() != null) {
                    AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
                    if (assistantMessage != null) {
                        List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
                        if (toolCallList != null && !toolCallList.isEmpty()) {
                            StringBuilder fixedJsonInfo = new StringBuilder();
                            fixedJsonInfo.append("检测到JSON格式错误，已尝试修复。问题工具：\n");
                            
                            for (AssistantMessage.ToolCall toolCall : toolCallList) {
                                String originalArgs = toolCall.arguments();
                                String fixedArgs = fixJsonString(originalArgs);
                                if (!originalArgs.equals(fixedArgs)) {
                                    fixedJsonInfo.append(String.format("工具 %s: 参数已修复\n", toolCall.name()));
                                    log.info("工具 {} 修复后的参数: {}", toolCall.name(), 
                                            fixedArgs.length() > 200 ? fixedArgs.substring(0, 200) + "..." : fixedArgs);
                                }
                            }
                            
                            errorMessage = "工具参数JSON格式错误。\n" + fixedJsonInfo.toString() + 
                                    "\n建议：请重新描述任务，或将复杂内容分步骤处理。";
                        }
                    }
                }
                
                if (errorMessage.equals("工具调用失败：" + e.getMessage())) {
                    errorMessage = "工具参数格式错误，AI返回的参数JSON格式不正确。请尝试重新描述任务，或者将复杂内容分步骤处理。";
                }
            }
            
            // 将错误信息添加到消息上下文
            getMessageList().add(new AssistantMessage(errorMessage));
            return errorMessage;
        }
    }

    /**
     * 压缩工具返回结果，避免大内容占用过多token
     * 由于ToolResponse.responseData是final字段，无法直接修改
     * 我们采用创建新ToolResponseMessage的方式
     * 
     * @param conversationHistory 原始对话历史
     * @return 压缩后的对话历史（包含新的ToolResponseMessage）
     */
    private List<Message> compressToolResponses(List<Message> conversationHistory) {
        if (getMaxToolResponseLength() <= 0) {
            return conversationHistory;
        }
        
        return conversationHistory.stream().map(message -> {
            if (message instanceof ToolResponseMessage) {
                ToolResponseMessage toolResponse = (ToolResponseMessage) message;
                // 检查是否有需要压缩的响应
                boolean needsCompression = toolResponse.getResponses().stream()
                        .anyMatch(response -> {
                            String data = response.responseData();
                            return data != null && data.length() > getMaxToolResponseLength();
                        });
                
                if (!needsCompression) {
                    return message; // 不需要压缩，直接返回
                }
                
                // 创建压缩后的ToolResponse列表
                List<ToolResponseMessage.ToolResponse> compressedResponses = toolResponse.getResponses().stream()
                        .map(response -> {
                            String originalData = response.responseData();
                            if (originalData != null && originalData.length() > getMaxToolResponseLength()) {
                                String compressedData = compressToolResponse(originalData);
                                log.debug("工具 {} 的返回结果已压缩：从 {} 字符减少到 {} 字符", 
                                        response.name(), originalData.length(), compressedData.length());
                                // 创建新的ToolResponse
                                return new ToolResponseMessage.ToolResponse(
                                        response.id(),
                                        response.name(),
                                        compressedData
                                );
                            }
                            return response;
                        })
                        .collect(Collectors.toList());
                
                // 创建新的ToolResponseMessage，包含压缩后的响应
                try {
                    return new ToolResponseMessage(compressedResponses, toolResponse.getMetadata());
                } catch (Exception e) {
                    log.warn("无法创建压缩后的ToolResponseMessage，返回原始消息：{}", e.getMessage());
                    return message;
                }
            }
            return message;
        }).collect(Collectors.toList());
    }
    

    /**
     * 清理资源，重置计数器
     */
    @Override
    protected void cleanup() {
        super.cleanup();
        consecutiveNoToolCallCount = 0;
    }
}
