package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.vo.chat.MessageVO;
import com.jieni.mqxq.domain.vo.chat.SessionListItemVO;
import com.jieni.mqxq.domain.vo.chat.SessionVO;
import com.jieni.mqxq.service.chat.ChatSessionService;
import com.jieni.mqxq.util.SaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/user/session")
@RequiredArgsConstructor
@CrossOrigin
@SaCheckLogin
@SaCheckRole("用户")
@Tag(name = "用户-会话", description = "用户端对话会话管理接口")
public class SessionController {

    private final ChatSessionService chatSessionService;

    /**
     * 新建会话
     */
    @Operation(summary = "新建会话", description = "创建新的对话会话，可指定示例问题数量")
    @PostMapping
    public Result<SessionVO> createSession(
            @Parameter(description = "示例问题数量", example = "3")
            @RequestParam(value = "n", defaultValue = "3") Integer num) {
        return Result.success(this.chatSessionService.createSession(num));
    }

    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    @Operation(summary = "获取热门会话示例", description = "获取热门会话示例列表，用于快速开始对话")
    @GetMapping("/hot")
    public Result<List<SessionVO.Example>> hotExamples(
            @Parameter(description = "返回数量", example = "3")
            @RequestParam(value = "n", defaultValue = "3") Integer num) {
        return Result.success(this.chatSessionService.hotExamples(num));
    }

    /**
     * 查询单个历史对话详情
     *
     * @return 对话记录列表
     */
    @Operation(summary = "查询会话详情", description = "根据会话ID查询该会话的所有历史消息记录")
    @GetMapping("/{sessionId}")
    public Result<List<MessageVO>> queryBySessionId(
            @Parameter(description = "会话ID", required = true, in = ParameterIn.PATH, example = "session_123")
            @PathVariable("sessionId") String sessionId) {
        return Result.success(this.chatSessionService.queryBySessionId(sessionId));
    }

    /**
     * 查询当前用户的会话列表
     *
     * @return 会话列表
     */
    @Operation(summary = "查询会话列表", description = "获取当前用户的所有会话列表")
    @GetMapping("/list")
    public Result<List<SessionListItemVO>> listSessions() {
        Integer userId = SaUtil.getLoginId();
        return Result.success(this.chatSessionService.listByUserId(userId));
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 删除结果
     */
    @Operation(summary = "删除会话", description = "删除指定的会话及其所有消息记录")
    @DeleteMapping("/{sessionId}")
    public Result<Void> deleteSession(
            @Parameter(description = "会话ID", required = true, in = ParameterIn.PATH, example = "session_123")
            @PathVariable("sessionId") String sessionId) {
        Integer userId = SaUtil.getLoginId();
        this.chatSessionService.deleteSession(sessionId, userId);
        return Result.success();
    }

}