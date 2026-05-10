package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.chapter.ChapterCreateDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterQueryDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterUpdateDTO;
import com.jieni.mqxq.domain.vo.chapter.ChapterStatisticsVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterVO;
import com.jieni.mqxq.service.course.ChapterService;
import com.jieni.mqxq.util.SaUtil;
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
 * 管理员端章节管理控制器
 * 
 * 提供章节的完整生命周期管理，包括创建、查询、更新、删除等功能。
 * 支持分页查询、条件筛选、批量操作和统计功能。只有管理员角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/chapter")
@Tag(name = "管理员-章节管理", description = "管理员端章节管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class ChapterAdminController {

    @Resource
    private ChapterService chapterService;

    /**
     * 通过ID查询章节详情
     */
    @Operation(summary = "查询章节详情", description = "根据ID获取章节详细信息")
    @GetMapping("/{id}")
    public Result<ChapterVO> getChapterById(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer id) {
        
        log.info("管理员查询章节详情, ID: {}", id);
        ChapterVO chapterVO = chapterService.getChapterDetail(id);
        return Result.success(chapterVO);
    }

    /**
     * 分页查询章节列表
     */
    @Operation(summary = "分页查询章节", description = "分页获取章节列表")
    @GetMapping("/page")
    public Result<PageInfo<ChapterVO>> getChapterPage(@Valid ChapterQueryDTO queryDTO) {
        log.info("管理员分页查询章节, queryDTO: {}", queryDTO);
        PageInfo<ChapterVO> pageInfo = chapterService.getChapterPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 新增章节
     */
    @Operation(summary = "新增章节", description = "创建新的章节")
    @PostMapping
    public Result<ChapterVO> createChapter(@Valid @RequestBody ChapterCreateDTO createDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员创建章节, adminId: {}, createDTO: {}", adminId, createDTO);
        
        ChapterVO chapterVO = chapterService.createChapter(createDTO, adminId);
        return Result.success("章节创建成功", chapterVO);
    }

    /**
     * 更新章节
     */
    @Operation(summary = "更新章节", description = "修改章节信息")
    @PutMapping("/{id}")
    public Result<ChapterVO> updateChapter(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer id,
            @Valid @RequestBody ChapterUpdateDTO updateDTO) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员更新章节, adminId: {}, ID: {}, updateDTO: {}", adminId, id, updateDTO);
        
        ChapterVO chapterVO = chapterService.updateChapter(id, updateDTO, adminId);
        return Result.success("章节更新成功", chapterVO);
    }

    /**
     * 删除章节
     */
    @Operation(summary = "删除章节", description = "删除指定章节")
    @DeleteMapping("/{id}")
    public Result<Void> deleteChapter(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer id) {
        
        log.info("管理员删除章节, ID: {}", id);
        chapterService.deleteChapter(id);
        return Result.success("章节删除成功");
    }

    /**
     * 获取章节统计信息
     */
    @Operation(summary = "章节统计", description = "获取章节相关统计信息")
    @GetMapping("/statistics")
    public Result<ChapterStatisticsVO> getChapterStatistics(
            @Parameter(description = "课程ID（可选，不传则统计全部）", example = "1")
            @RequestParam(required = false) @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        log.info("管理员获取章节统计信息, courseId: {}", courseId);
        ChapterStatisticsVO statistics = chapterService.getChapterStatistics(courseId);
        return Result.success(statistics);
    }

    /**
     * 批量删除章节（级联删除关联小节）
     */
    @Operation(summary = "批量删除章节", description = "批量删除多个章节及其关联小节")
    @DeleteMapping("/batch")
    public Result<String> batchDeleteChapters(@RequestBody List<Integer> ids) {
        log.info("管理员批量删除章节, IDs: {}", ids);
        
        Integer successCount = chapterService.batchDeleteChapters(ids);
        return Result.success(String.format("成功删除%d个章节及其关联小节", successCount));
    }

    /**
     * 获取课程的所有章节
     */
    @Operation(summary = "获取课程章节", description = "获取指定课程的所有章节")
    @GetMapping("/course/{courseId}")
    public Result<List<ChapterVO>> getChaptersByCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        log.info("管理员获取课程章节, courseId: {}", courseId);
        List<ChapterVO> chapters = chapterService.getCourseChapters(courseId);
        return Result.success(chapters);
    }
}
