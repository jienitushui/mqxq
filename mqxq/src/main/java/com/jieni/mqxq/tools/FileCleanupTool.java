package com.jieni.mqxq.tools;

import com.jieni.mqxq.common.scheduled.FileCleanupScheduledTask;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 文件清理工具
 * 允许agent手动触发文件清理或查看文件统计信息
 */
@Component
public class FileCleanupTool {

    private final FileCleanupScheduledTask fileCleanupTask;

    public FileCleanupTool(FileCleanupScheduledTask fileCleanupTask) {
        this.fileCleanupTask = fileCleanupTask;
    }

    @Tool(description = "Get file directory statistics")
    public String getFileStats() {
        return fileCleanupTask.getDirectoryStats();
    }

    @Tool(description = "Manually trigger file cleanup")
    public String cleanupFiles() {
        fileCleanupTask.manualCleanup();
        return "文件清理任务已触发，请查看日志了解详情";
    }
}

