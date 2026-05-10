package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.chat.ChatDTO;
import com.jieni.mqxq.domain.vo.chat.ChatEventVO;
import com.jieni.mqxq.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;


@Slf4j
@RestController
@RequestMapping("/api/user/chat")
@RequiredArgsConstructor
@CrossOrigin
@SaCheckLogin
@SaCheckRole("用户")
@Tag(name = "用户-聊天", description = "用户端聊天对话接口")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送聊天消息", description = "向AI发送聊天消息，返回流式响应（Server-Sent Events）")
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(
            @Parameter(description = "聊天请求数据", required = true)
            @RequestBody ChatDTO chatDTO) {
        return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId());
    }

    @Operation(summary = "停止聊天", description = "停止指定会话的聊天流式响应")
    @PostMapping("/stop")
    public Result<Void> stop(
            @Parameter(description = "会话ID", required = true, example = "session_123")
            @RequestParam("sessionId") String sessionId) {
        this.chatService.stop(sessionId);
        return Result.success("停止成功");
    }
}
