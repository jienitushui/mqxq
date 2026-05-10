package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.MyCourseCreateDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseQueryDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseUpdateDTO;
import com.jieni.mqxq.domain.vo.course.MyCourseStatisticsVO;
import com.jieni.mqxq.domain.vo.course.MyCourseVO;
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

/**
 * 用户端我的课程控制器
 * 
 * 提供用户管理自己学习课程的完整功能，包括加入课程、退出课程、课程列表查询、学习统计等
 * 支持课程加入状态检查、学习进度管理、权限验证等功能
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/user/my-course")
@Tag(name = "我的课程管理", description = "用户课程学习相关接口")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("用户")
public class MyCourseController {

    @Resource
    private MyCourseService myCourseService;

    /**
     * 加入课程
     */
    @Operation(summary = "加入课程", description = "用户加入课程")
    @PostMapping("/join")
    public Result<MyCourseVO> joinCourse(@Valid @RequestBody MyCourseCreateDTO createDTO) {
        Integer userId = SaUtil.getLoginId();
        MyCourseVO myCourseVO = myCourseService.joinCourse(userId, createDTO);
        return Result.success("成功加入课程", myCourseVO);
    }

    /**
     * 获取我的课程列表
     */
    @Operation(summary = "我的课程列表", description = "分页获取用户的课程列表")
    @GetMapping("/list")
    public Result<PageInfo<MyCourseVO>> getMyCourseList(@Valid MyCourseQueryDTO queryDTO) {
        Integer userId = SaUtil.getLoginId();
        queryDTO.setUserId(userId);
        
        PageInfo<MyCourseVO> pageInfo = myCourseService.getMyCourseListByPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取课程学习详情
     */
    @Operation(summary = "课程学习详情", description = "获取用户在某课程的学习详情")
    @GetMapping("/detail/{courseId}")
    public Result<MyCourseVO> getCourseDetail(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer userId = SaUtil.getLoginId();
        MyCourseVO myCourseVO = myCourseService.getMyCourseByCourseId(userId, courseId);
        return Result.success(myCourseVO);
    }

    /**
     * 更新学习进度
     */
    @Operation(summary = "更新学习进度", description = "更新课程学习进度和状态")
    @PutMapping("/progress")
    public Result<MyCourseVO> updateProgress(@Valid @RequestBody MyCourseUpdateDTO updateDTO) {
        Integer userId = SaUtil.getLoginId();
        MyCourseVO myCourseVO = myCourseService.updateStudyProgress(userId, updateDTO);
        return Result.success("学习进度更新成功", myCourseVO);
    }

    /**
     * 退出课程
     */
    @Operation(summary = "退出课程", description = "用户退出课程")
    @DeleteMapping("/quit/{courseId}")
    public Result<String> quitCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer userId = SaUtil.getLoginId();
        myCourseService.quitCourse(userId, courseId);
        return Result.success("成功退出课程");
    }

    /**
     * 获取学习统计
     */
    @Operation(summary = "学习统计", description = "获取用户学习统计信息")
    @GetMapping("/statistics")
    public Result<MyCourseStatisticsVO> getStudyStatistics() {
        Integer userId = SaUtil.getLoginId();
        MyCourseStatisticsVO statisticsVO = myCourseService.getUserStudyStatistics(userId);
        return Result.success(statisticsVO);
    }

    /**
     * 检查是否已加入课程
     */
    @Operation(summary = "检查课程加入状态", description = "检查用户是否已加入指定课程")
    @GetMapping("/check/{courseId}")
    public Result<Boolean> checkCourseJoined(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer userId = SaUtil.getLoginId();
        boolean hasJoined = myCourseService.hasJoinedCourse(userId, courseId);
        return Result.success(hasJoined);
    }
}
