package com.jieni.mqxq.controller.publicapi;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.PublishedCourseQueryDTO;
import com.jieni.mqxq.domain.vo.course.CourseDetailVO;
import com.jieni.mqxq.domain.vo.course.CourseListVO;
import com.jieni.mqxq.service.course.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共课程控制器
 * 
 * 提供无需登录即可访问的课程相关公共接口，包括课程列表查询、详情查看、热门课程、免费课程等。
 * 支持分页查询、按分类筛选、关键词搜索等功能。为平台的访客用户提供课程浏览服务。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/public/course")
@Tag(name = "公共课程接口", description = "无需登录即可访问的课程接口")
public class CoursePublicController {

    @Resource
    private CourseService courseService;

    /**
     * 分页查询已发布的课程
     *
     * @param title 课程标题关键词
     * @param page 页码（兼容 page 参数）
     * @param size 每页大小（兼容 size 参数）
     * @param queryDTO 查询条件DTO
     * @return 已发布课程列表分页结果
     */
    @Operation(summary = "分页查询已发布课程", description = "公共接口分页查询已发布的课程列表，支持按标题和分类筛选")
    @GetMapping("/page")
    public Result<PageInfo<CourseListVO>> pagePublishedCourses(
            @Parameter(description = "课程标题关键词", example = "Java")
            @RequestParam(required = false) String title,
            @Parameter(description = "页码", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "每页大小", example = "12")
            @RequestParam(required = false) Integer size,
            @Valid PublishedCourseQueryDTO queryDTO) {
        // 手动映射 page 和 size 参数到 DTO
        if (page != null) {
            queryDTO.setPageNum(page);
        }
        if (size != null) {
            queryDTO.setPageSize(size);
        }
        // 如果 title 参数存在，也设置到 DTO
        if (title != null && !title.trim().isEmpty()) {
            queryDTO.setTitle(title);
        }
        log.info("公共接口分页查询已发布课程, queryDTO: {}", queryDTO);
        PageInfo<CourseListVO> pageInfo = courseService.pagePublishedCourses(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID获取已发布课程详情
     *
     * @param id 课程ID
     * @return 课程详细信息
     */
    @Operation(summary = "获取已发布课程详情", description = "公共接口获取已发布课程的详细信息（不记录浏览）")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> getPublishedCourseDetail(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer id) {
        log.info("公共接口获取已发布课程详情, courseId: {}", id);
        // 公共接口不记录浏览，userId传null
        CourseDetailVO courseDetail = courseService.getPublishedCourseDetail(id, null);
        return Result.success(courseDetail);
    }

    /**
     * 获取热门课程
     *
     * @param limit 返回的课程数量
     * @return 热门课程列表
     */
    @Operation(summary = "获取热门课程", description = "公共接口获取浏览量最高的热门课程列表")
    @GetMapping("/hot")
    public Result<List<CourseListVO>> getHotCourses(
            @Parameter(description = "数量限制", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "数量必须大于0") @Max(value = 100, message = "数量不能超过100") Integer limit) {
        log.info("公共接口获取热门课程, limit: {}", limit);
        List<CourseListVO> hotCourses = courseService.listHotCourses(limit);
        return Result.success(hotCourses);
    }

    /**
     * 分页查询免费课程
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 免费课程分页结果
     */
    @Operation(summary = "获取免费课程", description = "公共接口分页查询价格为0的免费课程")
    @GetMapping("/free")
    public Result<PageInfo<CourseListVO>> getFreeCourses(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer pageNum,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") @Max(value = 100, message = "每页大小不能超过100") Integer pageSize) {
        log.info("公共接口获取免费课程, pageNum: {}, pageSize: {}", pageNum, pageSize);
        PageInfo<CourseListVO> pageInfo = courseService.pageFreeCourses(pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 搜索课程
     *
     * @param title 课程标题关键词
     * @param page 页码（兼容 page 参数）
     * @param size 每页大小（兼容 size 参数）
     * @param queryDTO 查询条件DTO
     * @return 搜索结果分页
     */
    @Operation(summary = "搜索课程", description = "公共接口根据关键词搜索已发布的课程")
    @GetMapping("/search")
    public Result<PageInfo<CourseListVO>> searchCourses(
            @Parameter(description = "课程标题关键词", example = "Java")
            @RequestParam(required = false) String title,
            @Parameter(description = "页码", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "每页大小", example = "12")
            @RequestParam(required = false) Integer size,
            @Valid PublishedCourseQueryDTO queryDTO) {
        // 手动映射 page 和 size 参数到 DTO
        if (page != null) {
            queryDTO.setPageNum(page);
        }
        if (size != null) {
            queryDTO.setPageSize(size);
        }
        // 如果 title 参数存在，也设置到 DTO
        if (title != null && !title.trim().isEmpty()) {
            queryDTO.setTitle(title);
        }
        log.info("公共接口搜索课程, queryDTO: {}", queryDTO);
        PageInfo<CourseListVO> pageInfo = courseService.pagePublishedCourses(queryDTO);
        return Result.success(pageInfo);
    }
}
