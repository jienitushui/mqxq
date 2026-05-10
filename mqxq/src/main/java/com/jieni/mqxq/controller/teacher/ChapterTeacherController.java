package com.jieni.mqxq.controller.teacher;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.chapter.ChapterCreateDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterQueryDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterUpdateDTO;
import com.jieni.mqxq.domain.vo.chapter.ChapterStatisticsVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterVO;
import com.jieni.mqxq.exception.MyException;
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
 * 章节管理控制器（教师端）
 * 
 * 提供教师端章节管理功能，包括章节的CRUD操作、查询和统计分析
 * 支持按课程筛选、分页查询，确保教师只能操作自己课程的章节
 * 包含完善的权限校验、级联删除和日志记录机制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/teacher/chapter")
@Tag(name = "教师-章节管理", description = "教师端章节管理接口")
@CrossOrigin
@SaCheckRole("教师")
public class ChapterTeacherController {

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
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师查询章节详情, teacherId: {}, ID: {}", teacherId, id);
        
        // 验证章节是否属于该教师的课程
        if (!chapterService.verifyChapterOwnership(id, teacherId)) {
            throw new MyException("无权查看此章节");
        }
        
        ChapterVO chapterVO = chapterService.getChapterDetail(id);
        return Result.success(chapterVO);
    }

    /**
     * 分页查询章节列表（教师只能查看自己课程的章节）
     */
    @Operation(summary = "分页查询章节", description = "分页获取教师课程的章节列表")
    @GetMapping("/page")
    public Result<PageInfo<ChapterVO>> getChapterPage(@Valid ChapterQueryDTO queryDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师分页查询章节, teacherId: {}, queryDTO: {}", teacherId, queryDTO);
        
        // 如果指定了courseId，先验证课程是否属于该教师
        if (queryDTO.getCourseId() != null) {
            if (!chapterService.verifyCourseOwnership(queryDTO.getCourseId(), teacherId)) {
                throw new MyException("无权查看此课程的章节");
            }
        }
        
        PageInfo<ChapterVO> pageInfo = chapterService.getChapterPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 新增章节（教师只能为自己的课程添加章节）
     */
    @Operation(summary = "新增章节", description = "为教师课程创建新的章节")
    @PostMapping
    public Result<ChapterVO> createChapter(@Valid @RequestBody ChapterCreateDTO createDTO) {
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师创建章节, teacherId: {}, createDTO: {}", teacherId, createDTO);
        
        // 验证课程是否属于该教师
        if (!chapterService.verifyCourseOwnership(createDTO.getCourseId(), teacherId)) {
            throw new MyException("无权为此课程添加章节");
        }
        
        ChapterVO chapterVO = chapterService.createChapter(createDTO, teacherId);
        return Result.success("章节创建成功", chapterVO);
    }

    /**
     * 更新章节（教师只能更新自己课程的章节）
     */
    @Operation(summary = "更新章节", description = "修改教师课程的章节信息")
    @PutMapping("/{id}")
    public Result<ChapterVO> updateChapter(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer id,
            @Valid @RequestBody ChapterUpdateDTO updateDTO) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师更新章节, teacherId: {}, ID: {}, updateDTO: {}", teacherId, id, updateDTO);
        
        // 验证章节是否属于该教师的课程
        if (!chapterService.verifyChapterOwnership(id, teacherId)) {
            throw new MyException("无权修改此章节");
        }
        
        // 如果要修改课程ID，验证新课程是否属于该教师
        if (updateDTO.getCourseId() != null) {
            if (!chapterService.verifyCourseOwnership(updateDTO.getCourseId(), teacherId)) {
                throw new MyException("无权将章节移动到此课程");
            }
        }
        
        ChapterVO chapterVO = chapterService.updateChapter(id, updateDTO, teacherId);
        return Result.success("章节更新成功", chapterVO);
    }

    /**
     * 删除章节（教师只能删除自己课程的章节）
     */
    @Operation(summary = "删除章节", description = "删除教师课程的指定章节")
    @DeleteMapping("/{id}")
    public Result<Void> deleteChapter(
            @Parameter(description = "章节ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "章节ID必须大于0") Integer id) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师删除章节, teacherId: {}, ID: {}", teacherId, id);
        
        // 验证章节是否属于该教师的课程
        if (!chapterService.verifyChapterOwnership(id, teacherId)) {
            throw new MyException("无权删除此章节");
        }
        
        chapterService.deleteChapter(id);
        return Result.success("章节删除成功");
    }

    /**
     * 获取课程的所有章节（教师只能查看自己课程的章节）
     */
    @Operation(summary = "获取课程章节", description = "获取教师指定课程的所有章节")
    @GetMapping("/course/{courseId}")
    public Result<List<ChapterVO>> getChaptersByCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取课程章节, teacherId: {}, courseId: {}", teacherId, courseId);
        
        // 验证课程是否属于该教师
        if (!chapterService.verifyCourseOwnership(courseId, teacherId)) {
            throw new MyException("无权查看此课程的章节");
        }
        
        List<ChapterVO> chapters = chapterService.getCourseChapters(courseId);
        return Result.success(chapters);
    }

    /**
     * 获取章节统计信息（教师只能查看自己课程的统计）
     */
    @Operation(summary = "章节统计", description = "获取教师课程章节相关统计信息")
    @GetMapping("/statistics/{courseId}")
    public Result<ChapterStatisticsVO> getChapterStatistics(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        Integer teacherId = SaUtil.getLoginId();
        log.info("教师获取章节统计信息, teacherId: {}, courseId: {}", teacherId, courseId);
        
        // 验证课程是否属于该教师
        if (!chapterService.verifyCourseOwnership(courseId, teacherId)) {
            throw new MyException("无权查看此课程的统计");
        }
        
        ChapterStatisticsVO statistics = chapterService.getChapterStatistics(courseId);
        return Result.success(statistics);
    }
}
