package com.jieni.mqxq.controller.publicapi;

import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.CourseSubjectSearchDTO;
import com.jieni.mqxq.domain.vo.course.CourseSubjectTreeVO;
import com.jieni.mqxq.domain.vo.course.CourseSubjectVO;
import com.jieni.mqxq.service.course.CourseSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程分类公共接口控制器
 * 
 * 提供无需登录即可访问的课程分类查询功能，包括分类列表查询、树形结构查询、子分类查询、搜索等。
 * 支持多级分类展示、关键词搜索、按时间排序等功能。为课程分类展示和筛选查询提供基础数据支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@RestController
@Validated
@RequestMapping("/api/public/course-subject")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
@Tag(name = "课程分类公共接口", description = "课程分类公共接口")
public class CourseSubjectPublicController {

    private final CourseSubjectService courseSubjectService;

    /**
     * 获取所有课程分类
     * 
     * @return 课程分类列表
     */
    @Operation(summary = "获取所有课程分类", description = "获取所有课程分类列表")
    @GetMapping("/all")
    public Result<List<CourseSubjectVO>> getAllCourseSubjects() {
        log.info("获取所有课程分类");
        List<CourseSubjectVO> list = courseSubjectService.listAllCourseSubjects();
        return Result.success(list);
    }

    /**
     * 根据ID获取课程分类详情
     * 
     * @param id 课程分类ID
     * @return 课程分类详情
     */
    @Operation(summary = "获取课程分类详情", description = "根据ID获取课程分类详细信息")
    @GetMapping("/{id}")
    public Result<CourseSubjectVO> getCourseSubjectById(
            @Parameter(description = "课程分类ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程分类ID必须大于0") Integer id) {
        log.info("获取课程分类详情, ID: {}", id);
        CourseSubjectVO courseSubjectVO = courseSubjectService.getCourseSubjectById(id);
        return Result.success(courseSubjectVO);
    }

    /**
     * 获取子分类列表
     * 
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Operation(summary = "获取子分类", description = "根据父分类ID获取子分类列表")
    @GetMapping("/children/{parentId}")
    public Result<List<CourseSubjectVO>> getChildrenByParentId(
            @Parameter(description = "父分类ID，0表示顶级分类", required = true, in = ParameterIn.PATH, example = "0")
            @PathVariable @Min(value = 0, message = "父分类ID不能小于0") Integer parentId) {
        log.info("获取子分类, 父分类ID: {}", parentId);
        List<CourseSubjectVO> children = courseSubjectService.listChildrenByParentId(parentId);
        return Result.success(children);
    }

    /**
     * 获取树形结构的课程分类
     * 
     * @return 树形结构的课程分类列表
     */
    @Operation(summary = "获取树形分类", description = "获取树形结构的课程分类")
    @GetMapping("/tree")
    public Result<List<CourseSubjectTreeVO>> getCourseSubjectTree() {
        log.info("获取树形结构的课程分类");
        List<CourseSubjectTreeVO> tree = courseSubjectService.getCourseSubjectTree();
        return Result.success(tree);
    }

    /**
     * 搜索课程分类
     * 
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    @Operation(summary = "搜索课程分类", description = "根据名称搜索课程分类")
    @GetMapping("/search")
    public Result<List<CourseSubjectVO>> searchCourseSubjects(
            @Parameter(description = "搜索关键词", required = true, example = "Java")
            @RequestParam String keyword) {
        log.info("搜索课程分类, 关键词: {}", keyword);
        
        // 构建DTO
        CourseSubjectSearchDTO searchDTO = new CourseSubjectSearchDTO();
        searchDTO.setKeyword(keyword);
        
        List<CourseSubjectVO> result = courseSubjectService.searchCourseSubjectsByName(searchDTO.getKeyword());
        return Result.success(result);
    }
}