package com.jieni.mqxq.common.scheduled;

import com.jieni.mqxq.domain.dto.FileStatsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件清理定时任务
 * 
 * 定期清理agent生成的临时文件，防止磁盘空间占用过多
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class FileCleanupScheduledTask {

    /**
     * 文件保存根目录
     */
    @Value("${file.cleanup.base-dir:${user.dir}/tmp}")
    private String baseDir;

    /**
     * 文件保留时间（小时），超过此时间的文件将被删除
     * 默认24小时
     */
    @Value("${file.cleanup.retention-hours:24}")
    private int retentionHours;

    /**
     * 是否启用自动清理
     * 默认启用
     */
    @Value("${file.cleanup.enabled:true}")
    private boolean enabled;

    /**
     * 清理文件
     * 每天凌晨3点执行一次
     * Cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "${file.cleanup.cron:0 0 3 * * ?}")
    public void cleanupFiles() {
        if (!enabled) {
            log.debug("文件清理功能已禁用，跳过清理");
            return;
        }

        log.info("========== 开始清理临时文件 ==========");
        log.info("清理目录: {}, 保留时间: {} 小时", baseDir, retentionHours);

        try {
            File baseDirFile = new File(baseDir);
            if (!baseDirFile.exists() || !baseDirFile.isDirectory()) {
                log.info("清理目录不存在，跳过清理: {}", baseDir);
                return;
            }

            long currentTime = System.currentTimeMillis();
            long retentionMillis = retentionHours * 60 * 60 * 1000L;
            int deletedCount = 0;
            long freedSpace = 0;

            // 清理各个子目录
            String[] subDirs = {"file", "word", "download"};
            for (String subDir : subDirs) {
                File dir = new File(baseDirFile, subDir);
                if (dir.exists() && dir.isDirectory()) {
                    long[] result = cleanupDirectory(dir, currentTime, retentionMillis);
                    deletedCount += (int) result[0];
                    freedSpace += result[1];
                }
            }

            log.info("========== 文件清理完成 ==========");
            log.info("删除文件数: {}, 释放空间: {} MB", deletedCount, freedSpace / (1024 * 1024));

        } catch (Exception e) {
            log.error("清理临时文件失败", e);
        }
    }

    /**
     * 清理指定目录中的过期文件
     * 
     * @param dir 要清理的目录
     * @param currentTime 当前时间（毫秒）
     * @param retentionMillis 保留时间（毫秒）
     * @return [删除文件数, 释放空间字节数]
     */
    private long[] cleanupDirectory(File dir, long currentTime, long retentionMillis) {
        int deletedCount = 0;
        long freedSpace = 0;

        File[] files = dir.listFiles();
        if (files == null) {
            return new long[]{0, 0};
        }

        for (File file : files) {
            try {
                if (file.isFile()) {
                    long lastModified = file.lastModified();
                    long age = currentTime - lastModified;

                    if (age > retentionMillis) {
                        long fileSize = file.length();
                        if (file.delete()) {
                            deletedCount++;
                            freedSpace += fileSize;
                            log.debug("删除过期文件: {}, 文件年龄: {} 小时", 
                                    file.getName(), age / (60 * 60 * 1000));
                        } else {
                            log.warn("删除文件失败: {}", file.getAbsolutePath());
                        }
                    }
                } else if (file.isDirectory()) {
                    // 递归清理子目录
                    long[] result = cleanupDirectory(file, currentTime, retentionMillis);
                    deletedCount += (int) result[0];
                    freedSpace += result[1];
                    
                    // 如果目录为空，尝试删除
                    File[] remainingFiles = file.listFiles();
                    if (remainingFiles == null || remainingFiles.length == 0) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                log.warn("处理文件时出错: {}, 错误: {}", file.getAbsolutePath(), e.getMessage());
            }
        }

        return new long[]{deletedCount, freedSpace};
    }

    /**
     * 手动触发清理（可用于测试或API调用）
     */
    public void manualCleanup() {
        log.info("手动触发文件清理");
        cleanupFiles();
    }

    /**
     * 获取目录统计信息（字符串格式，用于工具调用）
     * 
     * @return 统计信息字符串
     */
    public String getDirectoryStats() {
        FileStatsDTO stats = getDirectoryStatsDTO();
        if (stats == null) {
            return "获取统计信息失败";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("文件目录统计:\n");
        sb.append("基础目录: ").append(stats.getBaseDir()).append("\n");
        sb.append("保留时间: ").append(stats.getRetentionHours()).append(" 小时\n\n");
        
        for (FileStatsDTO.DirectoryStats dir : stats.getDirectories()) {
            sb.append(String.format("%s: %d 个文件, %.2f MB\n", 
                    dir.getName(), dir.getFileCount(), dir.getTotalSizeMB()));
        }
        
        return sb.toString();
    }
    
    /**
     * 获取目录统计信息（DTO格式，用于API返回）
     * 
     * @return 统计信息DTO对象
     */
    public FileStatsDTO getDirectoryStatsDTO() {
        try {
            File baseDirFile = new File(baseDir);
            if (!baseDirFile.exists()) {
                return null;
            }

            List<FileStatsDTO.DirectoryStats> directoryStatsList = new ArrayList<>();
            String[] subDirs = {"file", "word", "download"};
            
            for (String subDir : subDirs) {
                File dir = new File(baseDirFile, subDir);
                int fileCount = 0;
                long totalSize = 0;
                
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile()) {
                                fileCount++;
                                totalSize += file.length();
                            }
                        }
                    }
                }
                
                double totalSizeMB = totalSize / (1024.0 * 1024.0);
                directoryStatsList.add(new FileStatsDTO.DirectoryStats(subDir, fileCount, totalSizeMB));
            }

            return new FileStatsDTO(baseDir, retentionHours, directoryStatsList);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return null;
        }
    }
}

