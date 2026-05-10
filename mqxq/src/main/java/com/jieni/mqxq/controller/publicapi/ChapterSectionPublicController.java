package com.jieni.mqxq.controller.publicapi;

import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.vo.chapter.ChapterStatisticsVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterStructureVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterVO;
import com.jieni.mqxq.service.course.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节小节公共接口控制器
 * 
 * 提供无需登录即可访问的课程章节和小节信息查询功能，包括课程结构查询、章节小节查询、统计信息等。
 * 支持课程学习路径查询、章节数量统计、小节时长统计等功能。为课程展示和学习导航提供基础数据支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/public/chapter-section")
@Tag(name = "公共-章节小节", description = "公开的课程章节和小节查询接口")
@CrossOrigin
public class ChapterSectionPublicController {

    @Resource
    private ChapterService chapterService;

    /**
     * 获取课程完整的章节结构
     */
    @Operation(summary = "获取课程章节结构", description = "获取课程的完整章节和小节结构")
    @GetMapping("/course/{courseId}/structure")
    public Result<List<ChapterStructureVO>> getCourseStructure(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        log.info("获取课程章节结构, courseId: {}", courseId);
        List<ChapterStructureVO> structure = chapterService.getCourseStructure(courseId);
        return Result.success(structure);
    }

    /**
     * 获取课程章节列表
     */
    @Operation(summary = "获取课程章节列表", description = "获取指定课程的所有章节")
    @GetMapping("/course/{courseId}/chapters")
    public Result<List<ChapterVO>> getCourseChapters(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        log.info("获取课程章节列表, courseId: {}", courseId);
        List<ChapterVO> chapters = chapterService.getCourseChapters(courseId);
        return Result.success(chapters);
    }

    /**
     * 获取课程小节统计
     */
    @Operation(summary = "获取课程小节统计", description = "获取指定课程的小节统计信息")
    @GetMapping("/course/{courseId}/statistics")
    public Result<ChapterStatisticsVO> getCourseSectionStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        log.info("获取课程小节统计, courseId: {}", courseId);
        ChapterStatisticsVO statistics = chapterService.getChapterStatistics(courseId);
        return Result.success(statistics);
    }
}
