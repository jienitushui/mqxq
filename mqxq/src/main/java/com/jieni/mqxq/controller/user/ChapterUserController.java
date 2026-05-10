package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.chapter.ChapterQueryDTO;
import com.jieni.mqxq.domain.vo.chapter.ChapterVO;
import com.jieni.mqxq.service.course.ChapterService;
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
 * 用户端章节查看控制器
 * 
 * 提供用户查看课程章节信息的完整功能，支持章节详情查询、分页浏览、课程章节列表等。
 * 用户只能查看已发布状态的章节内容，确保内容发布控制和权限安全。只有用户角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/user/chapter")
@Tag(name = "用户-章节查看", description = "用户端章节查看接口")
@CrossOrigin
@SaCheckRole("用户")
public class ChapterUserController {

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
        
        log.info("用户查询章节详情, ID: {}", id);
        ChapterVO chapterVO = chapterService.getChapterDetail(id);
        return Result.success(chapterVO);
    }

    /**
     * 分页查询课程章节列表
     */
    @Operation(summary = "分页查询课程章节", description = "分页获取指定课程的章节列表")
    @GetMapping("/page")
    public Result<PageInfo<ChapterVO>> getCourseChapterPage(@Valid ChapterQueryDTO queryDTO) {
        log.info("用户分页查询课程章节, queryDTO: {}", queryDTO);
        
        PageInfo<ChapterVO> pageInfo = chapterService.getChapterPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取课程的所有章节（不分页）
     */
    @Operation(summary = "获取课程所有章节", description = "获取指定课程的所有章节列表")
    @GetMapping("/course/{courseId}")
    public Result<List<ChapterVO>> getAllChaptersByCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        
        log.info("用户获取课程所有章节, courseId: {}", courseId);
        List<ChapterVO> chapters = chapterService.getCourseChapters(courseId);
        return Result.success(chapters);
    }
}
