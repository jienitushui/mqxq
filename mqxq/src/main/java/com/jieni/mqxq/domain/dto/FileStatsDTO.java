package com.jieni.mqxq.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件统计信息DTO
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileStatsDTO {
    /**
     * 基础目录路径
     */
    private String baseDir;
    
    /**
     * 保留时间（小时）
     */
    private Integer retentionHours;
    
    /**
     * 目录统计列表
     */
    private List<DirectoryStats> directories;
    
    /**
     * 目录统计信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirectoryStats {
        /**
         * 目录名称
         */
        private String name;
        
        /**
         * 文件数量
         */
        private Integer fileCount;
        
        /**
         * 总大小（MB）
         */
        private Double totalSizeMB;
    }
}

