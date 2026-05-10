package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jieni.mqxq.agent.YuManus;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.common.scheduled.FileCleanupScheduledTask;
import com.jieni.mqxq.domain.dto.FileStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 教师端Manus智能体控制器
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/manus")
@Tag(name = "教师-Manus智能体", description = "教师端Manus超级智能体接口")
@CrossOrigin
@SaCheckRole("教师")
public class ManusTeacherController {

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Value("${server.port:9999}")
    private int serverPort;

    @Resource
    private FileCleanupScheduledTask fileCleanupScheduledTask;

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message 用户消息
     * @return SSE流式响应
     */
    @Operation(summary = "流式调用Manus智能体", description = "流式调用Manus超级智能体进行对话")
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(
            @Parameter(description = "用户消息", required = true, example = "你好")
            @RequestParam String message) {
        log.info("教师调用Manus智能体, message: {}", message);
        YuManus yuManus = new YuManus(allTools, dashscopeChatModel);
        return yuManus.runStream(message);
    }

    /**
     * 下载Agent生成的文件
     * 支持下载word、pdf、file等目录下的文件
     *
     * @param fileType 文件类型：word、pdf、file
     * @param fileName 文件名（包含扩展名）
     * @return 文件下载响应
     */
    @Operation(summary = "下载Agent生成的文件", description = "下载Agent生成的文件，支持word、pdf、file等类型")
    @GetMapping("/download/{fileType}/{fileName}")
    public ResponseEntity<FileSystemResource> downloadFile(
            @Parameter(description = "文件类型：word、pdf、file", required = true, example = "word")
            @PathVariable String fileType,
            @Parameter(description = "文件名（包含扩展名）", required = true, example = "教案.docx")
            @PathVariable String fileName) {
        try {
            // 构建文件路径
            String fileDir = System.getProperty("user.dir") + "/tmp/" + fileType;
            String filePath = fileDir + "/" + fileName;
            
            log.info("下载文件请求: fileType={}, fileName={}, filePath={}", fileType, fileName, filePath);
            
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 创建文件资源
            FileSystemResource resource = new FileSystemResource(file);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            // 对文件名进行URL编码，支持中文文件名
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition");
            
            log.info("文件下载成功: {}", filePath);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("下载文件失败: fileType={}, fileName={}, error={}", fileType, fileName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 手动触发文件清理
     * 清理超过保留时间的临时文件
     *
     * @return 清理结果
     */
    @Operation(summary = "手动触发文件清理", description = "手动触发清理超过保留时间的临时文件")
    @PostMapping("/cleanup")
    public Result<String> triggerCleanup() {
        try {
            fileCleanupScheduledTask.manualCleanup();
            return Result.success("文件清理任务已触发，请查看日志了解详情");
        } catch (Exception e) {
            log.error("触发文件清理失败", e);
            return Result.error("触发文件清理失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件目录统计信息
     * 查看各目录的文件数量和占用空间
     *
     * @return 文件统计信息
     */
    @Operation(summary = "获取文件目录统计信息", description = "查看各目录的文件数量和占用空间")
    @GetMapping("/file-stats")
    public Result<FileStatsDTO> getFileStats() {
        try {
            FileStatsDTO stats = fileCleanupScheduledTask.getDirectoryStatsDTO();
            if (stats == null) {
                return Result.error("获取文件统计信息失败：目录不存在或无法访问");
            }
            return Result.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return Result.error("获取文件统计信息失败: " + e.getMessage());
        }
    }
}

