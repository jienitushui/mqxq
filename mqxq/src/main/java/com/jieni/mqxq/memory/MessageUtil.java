package com.jieni.mqxq.memory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.jieni.mqxq.common.config.ToolResultHolder;
import org.springframework.ai.chat.messages.*;

import java.util.Map;

/**
 * 消息转换工具类，提供消息对象与JSON字符串之间的转换功能，主要用于Redis存储格式转换
 */
public class MessageUtil {

    /**
     * 将Message对象转换为Redis存储格式的JSON字符串
     *
     * @param message 需要转换的原始消息对象
     * @return 符合Redis存储规范的JSON字符串
     */
    public static String toJson(Message message) {
        var redisMessage = BeanUtil.toBean(message, RedisMessage.class);
        // 设置消息内容
        redisMessage.setTextContent(message.getText());
        if (message instanceof AssistantMessage assistantMessage) {
            redisMessage.setToolCalls(assistantMessage.getToolCalls());

            // 优先使用消息本身的params（如果是MyAssistantMessage）
            Map<String, Object> params = null;
            if (message instanceof MyAssistantMessage myAssistantMessage) {
                params = myAssistantMessage.getParams();
                // 即使params为空Map，也要保存（空Map和null是不同的）
                if (params != null) {
                    redisMessage.setParams(params);
                }
            }
            
            // 如果消息本身没有params，尝试从ToolResultHolder获取
            if (params == null) {
                // 通过 messageId 获取 requestId，再通过 requestId 获取参数列表，如果有，就存储起来
                // 最后，删除 messageId 对应的数据
                var messageId = Convert.toStr(assistantMessage.getMetadata().get("id"));
                var requestId = Convert.toStr(ToolResultHolder.get(messageId, "requestId"));
                params = ToolResultHolder.get(requestId);
                ToolResultHolder.remove(messageId);
                
                // 设置params到RedisMessage
                if (ObjectUtil.isNotEmpty(params)) {
                    redisMessage.setParams(params);
                }
            }
        }

        if (message instanceof ToolResponseMessage toolResponseMessage) {
            redisMessage.setToolResponses(toolResponseMessage.getResponses());
        }

        return JSONUtil.toJsonStr(redisMessage);
    }

    /**
     * 将Redis存储的JSON字符串反序列化为对应的Message对象
     *
     * @param json Redis存储的JSON格式消息数据
     * @return 对应类型的Message对象
     * @throws RuntimeException 当无法识别的消息类型时抛出异常
     */
    public static Message toMessage(String json) {
        var redisMessage = JSONUtil.toBean(json, RedisMessage.class);
        var messageType = MessageType.valueOf(redisMessage.getMessageType());
        switch (messageType) {
            case SYSTEM -> {
                return new SystemMessage(redisMessage.getTextContent());
            }
            case USER -> {
                return new UserMessage(redisMessage.getTextContent(), redisMessage.getMedia(), redisMessage.getMetadata());
            }
            case ASSISTANT -> {
                return new MyAssistantMessage(redisMessage.getTextContent(), redisMessage.getProperties(),
                        redisMessage.getToolCalls(), redisMessage.getParams());
            }
            case TOOL -> {
                return new ToolResponseMessage(redisMessage.getToolResponses(), redisMessage.getMetadata());
            }
        }

        throw new RuntimeException("Message data conversion failed.");
    }

}
