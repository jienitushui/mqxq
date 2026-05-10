package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.homework.CourseHomeworkBatchOperationDTO;
import com.jieni.mqxq.domain.dto.homework.CourseHomeworkQueryDTO;
import com.jieni.mqxq.domain.dto.homework.CourseHomeworkUpdateDTO;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkVO;
import com.jieni.mqxq.service.homework.CourseHomeworkService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课程作业管理控制器（管理员端）
 * 
 * 提供管理员端课程作业的全面管理功能，包括作业查询、审核、发布/取消发布和删除等操作
 * 支持按课程、教师、状态筛选，提供批量操作、统计分析和数据导出等高级功能
 * 确保只有管理员角色可以访问，包含完善的日志记录和异常处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/homework")
@Tag(name = "管理员-作业管理", description = "管理员端作业管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class CourseHomeworkAdminController {

    @Resource
    private CourseHomeworkService courseHomeworkService;

    @Operation(summary = "分页查询所有作业", description = "管理员分页查询所有作业")
    @GetMapping("/list")
    public Result<PageInfo<CourseHomeworkVO>> getAllHomeworkList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @Parameter(description = "课程ID")
            @RequestParam(required = false) Integer courseId,
            @Parameter(description = "教师ID")
            @RequestParam(required = false) Integer teacherId,
            @Parameter(description = "作业状态：0-未发布，1-已发布")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "作业标题")
            @RequestParam(required = false) String title,
            @Parameter(description = "课程名称")
            @RequestParam(required = false) String courseName,
            @Parameter(description = "教师姓名")
            @RequestParam(required = false) String teacherName) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}查询所有作业, page: {}, size: {}", adminId, page, size);
        
        CourseHomeworkQueryDTO queryDTO = new CourseHomeworkQueryDTO();
        queryDTO.setPageNum(page);
        queryDTO.setPageSize(size);
        queryDTO.setCourseId(courseId);
        queryDTO.setCreateUser(teacherId);
        queryDTO.setStatus(status);
        queryDTO.setTitle(title);
        queryDTO.setCourseName(courseName);
        queryDTO.setTeacherName(teacherName);
        
        PageInfo<CourseHomeworkVO> pageInfo = courseHomeworkService.getHomeworkPage(queryDTO);
        return Result.success(pageInfo);
    }

    @Operation(summary = "获取作业详情", description = "管理员查看作业详情")
    @GetMapping("/{id}")
    public Result<CourseHomeworkVO> getHomeworkDetail(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}查看作业{}详情", adminId, id);
        
        CourseHomeworkVO homework = courseHomeworkService.getHomeworkById(id);
        return Result.success(homework);
    }

    @Operation(summary = "强制发布作业", description = "管理员强制发布作业")
    @PutMapping("/{id}/force-publish")
    public Result<CourseHomeworkVO> forcePublishHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}强制发布作业{}", adminId, id);
        
        CourseHomeworkUpdateDTO updateDTO = new CourseHomeworkUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setStatus(1);
        
        CourseHomeworkVO homework = courseHomeworkService.updateHomework(updateDTO, adminId);
        return Result.success("作业强制发布成功", homework);
    }

    @Operation(summary = "强制取消发布", description = "管理员强制取消发布作业")
    @PutMapping("/{id}/force-unpublish")
    public Result<CourseHomeworkVO> forceUnpublishHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}强制取消发布作业{}", adminId, id);
        
        CourseHomeworkUpdateDTO updateDTO = new CourseHomeworkUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setStatus(0);
        
        CourseHomeworkVO homework = courseHomeworkService.updateHomework(updateDTO, adminId);
        return Result.success("作业取消发布成功", homework);
    }

    @Operation(summary = "删除作业", description = "管理员删除作业")
    @DeleteMapping("/{id}")
    public Result<String> deleteHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}删除作业{}", adminId, id);
        
        courseHomeworkService.deleteHomework(id, adminId);
        return Result.success("作业删除成功");
    }

    @Operation(summary = "批量发布作业", description = "管理员批量发布作业")
    @PutMapping("/batch-publish")
    public Result<String> batchPublishHomework(@Valid @RequestBody CourseHomeworkBatchOperationDTO batchDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}批量发布作业, 作业数量: {}", adminId, batchDTO.getHomeworkIds().size());
        
        int successCount = courseHomeworkService.batchPublishHomework(batchDTO, adminId);
        return Result.success("成功发布 " + successCount + " 个作业");
    }

    @Operation(summary = "批量取消发布", description = "管理员批量取消发布作业")
    @PutMapping("/batch-unpublish")
    public Result<String> batchUnpublishHomework(@Valid @RequestBody CourseHomeworkBatchOperationDTO batchDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}批量取消发布作业, 作业数量: {}", adminId, batchDTO.getHomeworkIds().size());
        
        int successCount = courseHomeworkService.batchUnpublishHomework(batchDTO);
        return Result.success("成功取消发布 " + successCount + " 个作业");
    }

    @Operation(summary = "批量删除作业", description = "管理员批量删除作业")
    @DeleteMapping("/batch-delete")
    public Result<String> batchDeleteHomework(@Valid @RequestBody CourseHomeworkBatchOperationDTO batchDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}批量删除作业, 作业数量: {}", adminId, batchDTO.getHomeworkIds().size());
        
        int successCount = courseHomeworkService.batchDeleteHomework(batchDTO, adminId);
        return Result.success("成功删除 " + successCount + " 个作业");
    }

    @Operation(summary = "获取作业统计信息", description = "管理员获取作业统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getHomeworkStatistics() {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}获取作业统计信息", adminId);
        
        Map<String, Object> statistics = courseHomeworkService.getAdminHomeworkStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取教师作业排行", description = "获取教师作业数量排行")
    @GetMapping("/teacher-rank")
    public Result<List<Map<String, Object>>> getTeacherHomeworkRank() {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}获取教师作业排行", adminId);
        
        List<Map<String, Object>> rank = courseHomeworkService.getTeacherHomeworkRank();
        return Result.success(rank);
    }

    @Operation(summary = "获取课程作业排行", description = "获取课程作业数量排行")
    @GetMapping("/course-rank")
    public Result<List<Map<String, Object>>> getCourseHomeworkRank() {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}获取课程作业排行", adminId);
        
        List<Map<String, Object>> rank = courseHomeworkService.getCourseHomeworkRank();
        return Result.success(rank);
    }

    @Operation(summary = "获取作业趋势分析", description = "获取作业创建和发布的趋势分析")
    @GetMapping("/trend-analysis")
    public Result<List<Map<String, Object>>> getHomeworkTrendAnalysis(
            @Parameter(description = "天数", example = "30")
            @RequestParam(defaultValue = "30") @Min(value = 1, message = "天数必须大于0") Integer days) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}获取作业趋势分析, days: {}", adminId, days);
        
        List<Map<String, Object>> trend = courseHomeworkService.getHomeworkTrendAnalysis(days);
        return Result.success(trend);
    }

    @Operation(summary = "导出作业数据", description = "管理员导出作业数据")
    @GetMapping("/export")
    public void exportHomeworkData(
            @Parameter(description = "课程ID")
            @RequestParam(required = false) Integer courseId,
            @Parameter(description = "教师ID")
            @RequestParam(required = false) Integer teacherId,
            @Parameter(description = "作业状态：0-未发布，1-已发布")
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员{}导出作业数据, courseId: {}, teacherId: {}, status: {}", 
                adminId, courseId, teacherId, status);
        
        CourseHomeworkQueryDTO queryDTO = new CourseHomeworkQueryDTO();
        queryDTO.setCourseId(courseId);
        queryDTO.setCreateUser(teacherId);
        queryDTO.setStatus(status);
        
        courseHomeworkService.exportHomeworkData(queryDTO, response);
    }
}
