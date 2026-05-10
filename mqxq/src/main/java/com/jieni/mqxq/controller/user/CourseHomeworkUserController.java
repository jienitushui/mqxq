package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.homework.CourseHomeworkSubmitDTO;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkSubmissionVO;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkVO;
import com.jieni.mqxq.service.homework.CourseHomeworkService;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端课程作业控制器
 * 
 * 提供用户查看课程作业、提交作业和管理作业状态的功能
 * 用户只能查看已发布的作业，支持作业截止时间验证和权限控制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/user/homework")
@Tag(name = "用户-作业管理", description = "用户端作业相关接口")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("用户")
public class CourseHomeworkUserController {

    @Resource
    private CourseHomeworkService courseHomeworkService;

    @Operation(summary = "获取课程作业列表", description = "用户查看课程的已发布作业列表")
    @GetMapping("/course/{courseId}")
    public Result<PageInfo<CourseHomeworkVO>> getCourseHomeworkList(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}查看课程{}作业列表, page: {}, size: {}", userId, courseId, page, size);
        
        PageInfo<CourseHomeworkVO> pageInfo = courseHomeworkService.getPublishedHomeworkByCourse(courseId, page, size);
        return Result.success(pageInfo);
    }

    @Operation(summary = "获取作业详情", description = "用户查看作业详细信息")
    @GetMapping("/{id}")
    public Result<CourseHomeworkVO> getHomeworkDetail(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer id) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}查看作业{}详情", userId, id);
        
        CourseHomeworkVO homework = courseHomeworkService.getHomeworkById(id);
        return Result.success(homework);
    }

    @Operation(summary = "获取我的作业列表", description = "用户查看自己的作业列表")
    @GetMapping("/my-list")
    public Result<PageInfo<CourseHomeworkVO>> getMyHomeworkList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @Parameter(description = "作业状态：0-未发布，1-已发布")
            @RequestParam(required = false) Integer status) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}查看我的作业列表, page: {}, size: {}, status: {}", userId, page, size, status);
        
        PageInfo<CourseHomeworkVO> pageInfo = courseHomeworkService.getMyHomeworkList(userId, page, size, status);
        return Result.success(pageInfo);
    }

    @Operation(summary = "提交作业", description = "用户提交作业答案")
    @PostMapping("/submit")
    public Result<String> submitHomework(@Valid @RequestBody CourseHomeworkSubmitDTO submitDTO) {
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}提交作业{}", userId, submitDTO.getHomeworkId());
        
        courseHomeworkService.submitHomework(submitDTO, userId);
        return Result.success("作业提交成功");
    }

    @Operation(summary = "更新作业提交", description = "用户更新已提交的作业")
    @PutMapping("/submit")
    public Result<String> updateSubmission(@Valid @RequestBody CourseHomeworkSubmitDTO submitDTO) {
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}更新作业{}提交", userId, submitDTO.getHomeworkId());
        
        courseHomeworkService.updateHomeworkSubmission(submitDTO, userId);
        return Result.success("作业更新成功");
    }

    @Operation(summary = "撤回作业提交", description = "用户撤回已提交的作业")
    @DeleteMapping("/submit/{homeworkId}")
    public Result<String> withdrawSubmission(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}撤回作业{}提交", userId, homeworkId);
        
        courseHomeworkService.withdrawHomeworkSubmission(homeworkId, userId);
        return Result.success("作业撤回成功");
    }

    @Operation(summary = "上传作业附件", description = "用户上传作业附件文件")
    @PostMapping("/upload/{homeworkId}")
    public Result<String> uploadAttachment(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Parameter(description = "文件", required = true)
            @RequestParam("file") MultipartFile file) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}上传作业{}附件", userId, homeworkId);
        
        String fileUrl = courseHomeworkService.uploadHomeworkAttachment(homeworkId, userId, file);
        return Result.success("文件上传成功", fileUrl);
    }

    @Operation(summary = "获取我的提交记录", description = "用户查看自己的作业提交记录")
    @GetMapping("/submissions")
    public Result<PageInfo<CourseHomeworkSubmissionVO>> getMySubmissions(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @Parameter(description = "课程ID")
            @RequestParam(required = false) Integer courseId) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}查看提交记录, page: {}, size: {}, courseId: {}", userId, page, size, courseId);
        
        PageInfo<CourseHomeworkSubmissionVO> pageInfo = courseHomeworkService.getUserSubmissions(userId, page, size, courseId);
        return Result.success(pageInfo);
    }

    @Operation(summary = "获取作业提交详情", description = "用户查看自己的作业提交详情")
    @GetMapping("/submission/{homeworkId}")
    public Result<Object> getSubmissionDetail(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("用户{}查看作业{}提交详情", userId, homeworkId);
        
        Object detail = courseHomeworkService.getUserSubmissionDetail(homeworkId, userId);
        return Result.success(detail);
    }
}
