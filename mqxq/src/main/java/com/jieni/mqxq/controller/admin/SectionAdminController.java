package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.section.SectionCreateDTO;
import com.jieni.mqxq.domain.dto.section.SectionQueryDTO;
import com.jieni.mqxq.domain.dto.section.SectionStatusUpdateDTO;
import com.jieni.mqxq.domain.dto.section.SectionUpdateDTO;
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
 * 小节管理控制器（管理员端）
 * 
 * 提供管理员端课程小节的全面管理功能，包括小节的CRUD操作、发布管理和统计分析
 * 支持按章节、课程、状态筛选，提供批量操作、发布状态管理和小节统计功能
 * 确保只有管理员角色可以访问，包含完善的日志记录和异常处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/section")
@Tag(name = "管理员-小节管理", description = "管理员端小节管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class SectionAdminController {

    @Resource
    private SectionService sectionService;

    /**
     * 分页查询小节列表
     */
    @Operation(summary = "分页查询小节", description = "分页获取小节列表")
    @GetMapping("/page")
    public Result<PageInfo<SectionVO>> getSectionPage(@Valid SectionQueryDTO queryDTO) {
        log.info("管理员分页查询小节, 查询条件: {}", queryDTO);
        PageInfo<SectionVO> pageInfo = sectionService.getSectionPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 通过ID查询小节详情
     */
    @Operation(summary = "查询小节详情", description = "根据ID获取小节详细信息")
    @GetMapping("/{id}")
    public Result<SectionVO> getSectionById(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id) {
        log.info("管理员查询小节详情, ID: {}", id);
        SectionVO sectionVO = sectionService.getSectionById(id);
        return Result.success(sectionVO);
    }

    /**
     * 新增小节
     */
    @Operation(summary = "新增小节", description = "创建新的小节")
    @PostMapping
    public Result<SectionVO> createSection(@Valid @RequestBody SectionCreateDTO createDTO) {
        log.info("管理员创建小节, 创建数据: {}", createDTO);
        SectionVO sectionVO = sectionService.createSection(createDTO, null);
        return Result.success("小节创建成功", sectionVO);
    }

    /**
     * 更新小节
     */
    @Operation(summary = "更新小节", description = "修改小节信息")
    @PutMapping("/{id}")
    public Result<SectionVO> updateSection(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id,
            @Valid @RequestBody SectionUpdateDTO updateDTO) {
        log.info("管理员更新小节, ID: {}, 更新数据: {}", id, updateDTO);
        SectionVO sectionVO = sectionService.updateSection(id, updateDTO, null);
        return Result.success("小节更新成功", sectionVO);
    }

    /**
     * 发布/取消发布小节
     */
    @Operation(summary = "发布/取消发布小节", description = "修改小节发布状态")
    @PatchMapping("/{id}/status")
    public Result<SectionVO> updateSectionStatus(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id,
            @Valid @RequestBody SectionStatusUpdateDTO statusUpdateDTO) {
        log.info("管理员更新小节状态, ID: {}, 状态: {}", id, statusUpdateDTO.getStatus());
        SectionVO sectionVO = sectionService.updateSectionStatus(id, statusUpdateDTO.getStatus(), null);
        return Result.success("小节状态更新成功", sectionVO);
    }

    /**
     * 删除小节
     */
    @Operation(summary = "删除小节", description = "删除指定小节")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSection(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id) {
        log.info("管理员删除小节, ID: {}", id);
        sectionService.deleteSection(id, null);
        return Result.success("小节删除成功");
    }

    /**
     * 批量删除小节
     */
    @Operation(summary = "批量删除小节", description = "批量删除多个小节")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteSections(@RequestBody List<@Min(value = 1, message = "小节ID必须大于0") Integer> ids) {
        log.info("管理员批量删除小节, IDs: {}", ids);
        sectionService.batchDeleteSections(ids);
        return Result.success(String.format("成功删除%d个小节", ids.size()));
    }

    /**
     * 获取章节的所有小节
     */
    @Operation(summary = "获取章节小节", description = "获取指定章节的所有小节")
    @GetMapping("/chapter/{chapterId}")
    public Result<List<SectionVO>> getSectionsByChapter(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer chapterId) {
        log.info("管理员获取章节小节, chapterId: {}", chapterId);
        List<SectionVO> sections = sectionService.getSectionsByChapterId(chapterId, null, false);
        return Result.success(sections);
    }

    /**
     * 获取课程的所有小节
     */
    @Operation(summary = "获取课程小节", description = "获取指定课程的所有小节")
    @GetMapping("/course/{courseId}")
    public Result<List<SectionVO>> getSectionsByCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        log.info("管理员获取课程小节, courseId: {}", courseId);
        List<SectionVO> sections = sectionService.getSectionsByCourseId(courseId, null, false);
        return Result.success(sections);
    }

    /**
     * 获取小节统计信息
     */
    @Operation(summary = "小节统计", description = "获取小节相关统计信息")
    @GetMapping("/statistics")
    public Result<SectionStatisticsVO> getSectionStatistics() {
        log.info("管理员获取小节统计信息");
        SectionStatisticsVO statistics = sectionService.getSectionStatistics(null, null);
        return Result.success(statistics);
    }
}
