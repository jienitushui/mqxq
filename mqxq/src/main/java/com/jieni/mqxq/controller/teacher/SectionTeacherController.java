package com.jieni.mqxq.controller.teacher;

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
 * 小节管理控制器（教师端）
 * 
 * 提供教师端课程小节的全面管理功能，包括小节的CRUD操作、发布管理和统计分析
 * 支持按章节、课程、状态筛选，确保教师只能管理自己课程的小节
 * 包含完善的权限校验、级联关系验证和统计分析功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/section")
@Tag(name = "教师-小节管理", description = "教师端小节管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class SectionTeacherController {

    @Resource
    private SectionService sectionService;

    /**
     * 分页查询小节列表（教师只能查看自己课程的小节）
     */
    @Operation(summary = "分页查询小节", description = "分页获取教师课程的小节列表")
    @GetMapping("/page")
    public Result<PageInfo<SectionVO>> getSectionPage(@Valid SectionQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师分页查询小节, teacherId: {}, 查询条件: {}", teacherId, queryDTO);
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
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师查询小节详情, teacherId: {}, ID: {}", teacherId, id);
        
        // 权限验证在Service层完成
        SectionVO sectionVO = sectionService.getSectionById(id);
        
        // 验证小节是否属于该教师的课程
        if (!sectionService.verifySectionOwnership(id, teacherId)) {
            return Result.error("无权查看此小节");
        }
        
        return Result.success(sectionVO);
    }

    /**
     * 新增小节（教师只能为自己的课程添加小节）
     */
    @Operation(summary = "新增小节", description = "为教师课程创建新的小节")
    @PostMapping
    public Result<SectionVO> createSection(@Valid @RequestBody SectionCreateDTO createDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师创建小节, teacherId: {}, 创建数据: {}", teacherId, createDTO);
        SectionVO sectionVO = sectionService.createSection(createDTO, teacherId);
        return Result.success("小节创建成功", sectionVO);
    }

    /**
     * 更新小节（教师只能更新自己课程的小节）
     */
    @Operation(summary = "更新小节", description = "修改教师课程的小节信息")
    @PutMapping("/{id}")
    public Result<SectionVO> updateSection(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id,
            @Valid @RequestBody SectionUpdateDTO updateDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师更新小节, teacherId: {}, ID: {}, 更新数据: {}", teacherId, id, updateDTO);
        SectionVO sectionVO = sectionService.updateSection(id, updateDTO, teacherId);
        return Result.success("小节更新成功", sectionVO);
    }

    /**
     * 发布/取消发布小节（教师只能操作自己课程的小节）
     */
    @Operation(summary = "发布/取消发布小节", description = "修改教师课程的小节发布状态")
    @PutMapping("/{id}/status")
    public Result<SectionVO> updateSectionStatus(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id,
            @Valid @RequestBody SectionStatusUpdateDTO statusUpdateDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师更新小节状态, teacherId: {}, ID: {}, 状态: {}", teacherId, id, statusUpdateDTO.getStatus());
        SectionVO sectionVO = sectionService.updateSectionStatus(id, statusUpdateDTO.getStatus(), teacherId);
        return Result.success("小节状态更新成功", sectionVO);
    }

    /**
     * 删除小节（教师只能删除自己课程的小节）
     */
    @Operation(summary = "删除小节", description = "删除教师课程的指定小节")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSection(
            @Parameter(description = "小节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "小节ID必须大于0") Integer id) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师删除小节, teacherId: {}, ID: {}", teacherId, id);
        sectionService.deleteSection(id, teacherId);
        return Result.success("小节删除成功");
    }

    /**
     * 获取章节的所有小节（教师只能查看自己课程的小节）
     */
    @Operation(summary = "获取章节小节", description = "获取教师章节的所有小节")
    @GetMapping("/chapter/{chapterId}")
    public Result<List<SectionVO>> getSectionsByChapter(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer chapterId) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取章节小节, teacherId: {}, chapterId: {}", teacherId, chapterId);
        List<SectionVO> sections = sectionService.getSectionsByChapterId(chapterId, teacherId, false);
        return Result.success(sections);
    }

    /**
     * 获取课程的所有小节（教师只能查看自己课程的小节）
     */
    @Operation(summary = "获取课程小节", description = "获取教师课程的所有小节")
    @GetMapping("/course/{courseId}")
    public Result<List<SectionVO>> getSectionsByCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程小节, teacherId: {}, courseId: {}", teacherId, courseId);
        List<SectionVO> sections = sectionService.getSectionsByCourseId(courseId, teacherId, false);
        return Result.success(sections);
    }

    /**
     * 获取课程小节统计（教师只能查看自己课程的统计）
     */
    @Operation(summary = "课程小节统计", description = "获取教师课程小节相关统计信息")
    @GetMapping("/statistics/{courseId}")
    public Result<SectionStatisticsVO> getSectionStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程小节统计, teacherId: {}, courseId: {}", teacherId, courseId);
        SectionStatisticsVO statistics = sectionService.getSectionStatistics(courseId, teacherId);
        return Result.success(statistics);
    }
}
