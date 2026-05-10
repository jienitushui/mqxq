package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.section.SectionQueryDTO;
import com.jieni.mqxq.domain.vo.section.SectionStatisticsVO;
import com.jieni.mqxq.domain.vo.section.SectionVO;
import com.jieni.mqxq.service.course.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端课程小节查看控制器
 * 
 * 提供用户查看课程小节内容的完整功能，包括小节详情查询、分页浏览、统计信息查看等。
 * 用户只能查看已发布状态的小节内容，支持章节和课程维度的小节查询。只有用户角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/user/section")
@Tag(name = "用户-小节查看", description = "用户端小节查看接口")
@CrossOrigin
@SaCheckRole("用户")
public class SectionUserController {

    @Resource
    private SectionService sectionService;

    /**
     * 通过ID查询小节详情（只返回已发布的小节）
     */
    @Operation(summary = "查询小节详情", description = "根据ID获取小节详细信息")
    @GetMapping("/{id}")
    public Result<SectionVO> getSectionById(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id) {
        log.info("用户查询小节详情, ID: {}", id);
        
        SectionVO sectionVO = sectionService.getSectionById(id);
        
        // 只返回已发布的小节
        if (sectionVO.getStatus() == null || sectionVO.getStatus() != 1) {
            return Result.error("小节未发布");
        }
        
        return Result.success(sectionVO);
    }

    /**
     * 分页查询章节小节列表（只返回已发布的小节）
     */
    @Operation(summary = "分页查询章节小节", description = "分页获取指定章节的已发布小节列表")
    @GetMapping("/chapter/{chapterId}")
    public Result<PageInfo<SectionVO>> getChapterSections(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer chapterId,
            @Valid SectionQueryDTO queryDTO) {
        log.info("用户分页查询章节小节, chapterId: {}, 查询条件: {}", chapterId, queryDTO);
        
        // 设置章节ID和状态
        queryDTO.setChapterId(chapterId);
        queryDTO.setStatus(1); // 只查询已发布的小节
        
        PageInfo<SectionVO> pageInfo = sectionService.getSectionPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 分页查询课程小节列表（只返回已发布的小节）
     */
    @Operation(summary = "分页查询课程小节", description = "分页获取指定课程的已发布小节列表")
    @GetMapping("/course/{courseId}")
    public Result<PageInfo<SectionVO>> getCourseSections(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            @Valid SectionQueryDTO queryDTO) {
        log.info("用户分页查询课程小节, courseId: {}, 查询条件: {}", courseId, queryDTO);
        
        // 设置课程ID和状态
        queryDTO.setCourseId(courseId);
        queryDTO.setStatus(1); // 只查询已发布的小节
        
        PageInfo<SectionVO> pageInfo = sectionService.getSectionPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取章节的所有已发布小节（不分页）
     */
    @Operation(summary = "获取章节已发布小节", description = "获取指定章节的所有已发布小节")
    @GetMapping("/chapter/{chapterId}/all")
    public Result<List<SectionVO>> getAllPublishedSectionsByChapter(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer chapterId) {
        log.info("用户获取章节已发布小节, chapterId: {}", chapterId);
        List<SectionVO> sections = sectionService.getSectionsByChapterId(chapterId, null, true);
        return Result.success(sections);
    }

    /**
     * 获取课程的所有已发布小节（不分页）
     */
    @Operation(summary = "获取课程已发布小节", description = "获取指定课程的所有已发布小节列表")
    @GetMapping("/course/{courseId}/all")
    public Result<List<SectionVO>> getAllPublishedSectionsByCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        log.info("用户获取课程已发布小节, courseId: {}", courseId);
        List<SectionVO> sections = sectionService.getSectionsByCourseId(courseId, null, true);
        return Result.success(sections);
    }

    /**
     * 获取课程小节统计（只统计已发布的小节）
     */
    @Operation(summary = "课程小节统计", description = "获取课程已发布小节的相关统计信息")
    @GetMapping("/course/{courseId}/statistics")
    public Result<SectionStatisticsVO> getSectionStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        log.info("用户获取课程小节统计, courseId: {}", courseId);
        SectionStatisticsVO statistics = sectionService.getSectionStatistics(courseId, null);
        return Result.success(statistics);
    }
}
