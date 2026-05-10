package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.CreateCourseSubjectDTO;
import com.jieni.mqxq.domain.dto.course.UpdateCourseSubjectDTO;
import com.jieni.mqxq.domain.vo.course.CourseSubjectTreeVO;
import com.jieni.mqxq.domain.vo.course.CourseSubjectVO;
import com.jieni.mqxq.service.course.CourseSubjectService;
import com.jieni.mqxq.util.SaUtil;
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
 * 课程分类管理控制器
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Tag(name = "课程分类管理", description = "管理员课程分类管理接口")
@RestController
@Validated
@RequestMapping("/api/admin/course-subject")
@SaCheckRole("管理员")
@RequiredArgsConstructor
@Slf4j
public class CourseSubjectController {

    private final CourseSubjectService courseSubjectService;

    /**
     * 获取所有课程分类
     */
    @Operation(summary = "获取所有课程分类", description = "获取所有课程分类列表")
    @GetMapping("/all")
    public Result<List<CourseSubjectVO>> getAllCourseSubjects() {
        log.info("获取所有课程分类");
        List<CourseSubjectVO> list = courseSubjectService.listAllCourseSubjects();
        return Result.success(list);
    }

    /**
     * 分页查询课程分类
     */
    @Operation(summary = "分页查询课程分类", description = "分页查询课程分类列表")
    @GetMapping("/page")
    public Result<PageInfo<CourseSubjectVO>> getCourseSubjectPage(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @Parameter(description = "分类名称") 
            @RequestParam(required = false) String name,
            @Parameter(description = "父分类ID") 
            @RequestParam(required = false) @Min(value = 0, message = "父分类ID不能小于0") Integer parentId) {
        
        log.info("分页查询课程分类, page: {}, size: {}, name: {}, parentId: {}", page, size, name, parentId);
        PageInfo<CourseSubjectVO> pageInfo = courseSubjectService.pageCourseSubjects(name, parentId, page, size);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID获取课程分类详情
     */
    @Operation(summary = "获取课程分类详情", description = "根据ID获取课程分类详细信息")
    @GetMapping("/{id}")
    public Result<CourseSubjectVO> getCourseSubjectById(
            @Parameter(description = "课程分类ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        log.info("获取课程分类详情, ID: {}", id);
        CourseSubjectVO vo = courseSubjectService.getCourseSubjectById(id);
        return Result.success(vo);
    }

    /**
     * 创建课程分类
     */
    @Operation(summary = "创建课程分类", description = "创建新的课程分类")
    @PostMapping
    public Result<CourseSubjectVO> createCourseSubject(
            @Valid @RequestBody CreateCourseSubjectDTO createDTO) {
        
        log.info("创建课程分类: {}", createDTO);
        Integer currentUserId = SaUtil.getLoginId();
        CourseSubjectVO vo = courseSubjectService.createCourseSubject(createDTO, currentUserId);
        return Result.success(vo);
    }

    /**
     * 更新课程分类
     */
    @Operation(summary = "更新课程分类", description = "更新课程分类信息")
    @PutMapping("/{id}")
    public Result<CourseSubjectVO> updateCourseSubject(
            @Parameter(description = "课程分类ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id,
            @Valid @RequestBody UpdateCourseSubjectDTO updateDTO) {
        
        log.info("更新课程分类, ID: {}, 信息: {}", id, updateDTO);
        Integer currentUserId = SaUtil.getLoginId();
        CourseSubjectVO vo = courseSubjectService.updateCourseSubject(id, updateDTO, currentUserId);
        return Result.success(vo);
    }

    /**
     * 删除课程分类
     */
    @Operation(summary = "删除课程分类", description = "删除课程分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourseSubject(
            @Parameter(description = "课程分类ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        log.info("删除课程分类, ID: {}", id);
        courseSubjectService.deleteCourseSubject(id);
        return Result.success();
    }

    /**
     * 获取子分类列表
     */
    @Operation(summary = "获取子分类", description = "根据父分类ID获取子分类列表")
    @GetMapping("/children/{parentId}")
    public Result<List<CourseSubjectVO>> getChildrenByParentId(
            @Parameter(description = "父分类ID", required = true, in = ParameterIn.PATH, example = "0")
            @PathVariable @Min(value = 0, message = "父分类ID不能小于0") Integer parentId) {
        
        log.info("获取子分类, 父分类ID: {}", parentId);
        List<CourseSubjectVO> children = courseSubjectService.listChildrenByParentId(parentId);
        return Result.success(children);
    }

    /**
     * 获取树形结构的课程分类
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
     */
    @Operation(summary = "搜索课程分类", description = "根据名称搜索课程分类")
    @GetMapping("/search")
    public Result<List<CourseSubjectVO>> searchCourseSubjects(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword) {
        
        log.info("搜索课程分类, 关键词: {}", keyword);
        List<CourseSubjectVO> result = courseSubjectService.searchCourseSubjectsByName(keyword);
        return Result.success(result);
    }
}
