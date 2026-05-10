package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionCreateDTO;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionQueryDTO;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionUpdateDTO;
import com.jieni.mqxq.domain.entity.CourseHomework;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionDetailVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionVO;
import com.jieni.mqxq.service.homework.CourseHomeworkService;
import com.jieni.mqxq.service.homework.HomeworkSubmissionService;
import com.jieni.mqxq.service.course.MyCourseService;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 用户端作业提交控制器
 * 
 * 提供用户提交和管理作业的完整功能，包括作业提交、更新、撤回、附件上传等。
 * 支持权限验证、截止时间检查、状态管理等功能，确保用户只能管理自己的作业提交。只有用户角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/user/homework-submission")
@Tag(name = "用户-作业提交", description = "用户端作业提交接口")
@CrossOrigin
@SaCheckRole("用户")
public class HomeworkSubmissionUserController {

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;
    
    @Resource
    private CourseHomeworkService courseHomeworkService;

    @Resource
    private MyCourseService myCourseService;

    /**
     * 提交作业
     */
    @Operation(summary = "提交作业", description = "学生提交作业")
    @PostMapping("/submit")
    public Result<String> submitHomework(@Valid @RequestBody HomeworkSubmissionCreateDTO createDTO) {
        Integer userId = SaUtil.getLoginId();
        log.info("学生提交作业, userId: {}, homeworkId: {}", userId, createDTO.getHomeworkId());

        // 查找课程作业
        CourseHomework courseHomework = courseHomeworkService.getById(createDTO.getHomeworkId());

        // 验证作业是否存在且可提交
        courseHomeworkService.checkUserSubmissionPermission(userId, courseHomework.getCourseId());
        
        homeworkSubmissionService.submitHomework(createDTO, userId);

        // 状态流转：0 -> 1，1 -> 2
        tryStartLearning(userId, courseHomework.getCourseId());
        tryCompleteCourse(userId, courseHomework.getCourseId());
        return Result.success("提交成功");
    }

    /**
     * 更新作业提交
     */
    @Operation(summary = "更新提交", description = "学生更新作业提交内容")
    @PutMapping("/{id}")
    public Result<String> updateSubmission(
            @Parameter(description = "提交ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "提交ID必须大于0") Integer id,
            @Valid @RequestBody HomeworkSubmissionUpdateDTO updateDTO) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("学生更新作业提交, userId: {}, submissionId: {}", userId, id);
        
        homeworkSubmissionService.updateHomeworkSubmission(id, updateDTO, userId);
        return Result.success("更新成功");
    }

    /**
     * 上传作业附件
     */
    @Operation(summary = "上传附件", description = "学生上传作业附件")
    @PostMapping("/upload/{homeworkId}")
    public Result<String> uploadAttachment(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Parameter(description = "附件文件") @RequestParam("file") MultipartFile file) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("学生上传作业附件, userId: {}, homeworkId: {}", userId, homeworkId);

        CourseHomework courseHomework = courseHomeworkService.getById(homeworkId);
        
        // 验证权限
        courseHomeworkService.checkUserSubmissionPermission(userId, courseHomework.getCourseId());
        
        String fileUrl = courseHomeworkService.uploadHomeworkFile(userId, homeworkId, file);
        return Result.success(fileUrl);
    }

    /**
     * 获取我的作业提交列表
     */
    @Operation(summary = "我的提交列表", description = "学生查看自己的作业提交列表")
    @GetMapping("/my-submissions")
    public Result<PageInfo<HomeworkSubmissionVO>> getMySubmissions(@Valid HomeworkSubmissionQueryDTO queryDTO) {
        Integer userId = SaUtil.getLoginId();
        log.info("学生查询提交列表, userId: {}, queryDTO: {}", userId, queryDTO);
        
        PageInfo<HomeworkSubmissionVO> pageInfo = homeworkSubmissionService.getStudentSubmissionPage(queryDTO, userId);
        return Result.success(pageInfo);
    }

    /**
     * 获取提交详情
     */
    @Operation(summary = "提交详情", description = "学生查看作业提交详情")
    @GetMapping("/{id}")
    public Result<HomeworkSubmissionDetailVO> getSubmissionDetail(
            @Parameter(description = "提交ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "提交ID必须大于0") Integer id) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("学生查询提交详情, userId: {}, submissionId: {}", userId, id);
        
        HomeworkSubmissionDetailVO detailVO = homeworkSubmissionService.getSubmissionDetail(id);
        
        // 验证权限（只能查看自己的提交）
        if (!detailVO.getStudentId().equals(userId)) {
            return Result.error("无权限查看该提交");
        }
        
        return Result.success(detailVO);
    }

    /**
     * 根据作业ID获取我的提交
     */
    @Operation(summary = "作业提交详情", description = "根据作业ID获取学生的提交详情")
    @GetMapping("/homework/{homeworkId}")
    public Result<HomeworkSubmissionDetailVO> getSubmissionByHomework(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("学生查询作业提交详情, userId: {}, homeworkId: {}", userId, homeworkId);
        
        HomeworkSubmissionDetailVO detailVO = homeworkSubmissionService.getStudentSubmissionByHomework(userId, homeworkId);
        return Result.success(detailVO);
    }

    /**
     * 撤回作业提交
     */
    @Operation(summary = "撤回提交", description = "学生撤回作业提交")
    @DeleteMapping("/{id}")
    public Result<String> withdrawSubmission(
            @Parameter(description = "提交ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "提交ID必须大于0") Integer id) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("学生撤回作业提交, userId: {}, submissionId: {}", userId, id);
        
        homeworkSubmissionService.withdrawHomeworkSubmission(id, userId);
        return Result.success("撤回成功");
    }

    /**
     * 检查作业提交状态
     */
    @Operation(summary = "检查提交状态", description = "检查作业是否已提交及提交状态")
    @GetMapping("/check/{homeworkId}")
    public Result<Map<String, Object>> checkSubmissionStatus(
            @Parameter(description = "作业ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId) {
        
        Integer userId = SaUtil.getLoginId();
        log.info("学生检查提交状态, userId: {}, homeworkId: {}", userId, homeworkId);
        
        boolean hasSubmitted = homeworkSubmissionService.checkStudentSubmitted(userId, homeworkId);
        
        // 获取课程作业信息
        CourseHomework courseHomework = courseHomeworkService.getById(homeworkId);
        boolean canSubmit = courseHomeworkService.checkUserSubmissionPermission(userId, courseHomework.getCourseId());
        
        Map<String, Object> status = new HashMap<>();
        status.put("hasSubmitted", hasSubmitted);
        status.put("canSubmit", canSubmit);
        status.put("homeworkId", homeworkId);
        
        return Result.success(status);
    }

    /**
     * 尝试将学习状态更新为“学习中”
     */
    private void tryStartLearning(Integer userId, Integer courseId) {
        try {
            myCourseService.checkAndStartLearning(userId, courseId);
        } catch (Exception ex) {
            log.warn("更新学习状态到'学习中'失败, userId: {}, courseId: {}, error: {}", userId, courseId, ex.getMessage());
        }
    }

    /**
     * 尝试将学习状态更新为“已完成”
     */
    private void tryCompleteCourse(Integer userId, Integer courseId) {
        try {
            myCourseService.checkAndCompleteCourse(userId, courseId);
        } catch (Exception ex) {
            log.warn("更新学习状态到'已完成'失败, userId: {}, courseId: {}, error: {}", userId, courseId, ex.getMessage());
        }
    }
}
