package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.courseview.CourseViewBatchDeleteDTO;
import com.jieni.mqxq.domain.dto.courseview.CourseViewQueryDTO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewVO;
import com.jieni.mqxq.service.course.CourseViewService;
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
 * 用户端课程浏览记录控制器
 * 
 * 提供用户管理自己课程浏览记录的完整功能，包括浏览记录查询、详情查看、删除和清空等。
 * 支持分页查询、批量删除、权限验证等功能，确保用户只能管理自己的浏览记录。只有用户角色可以访问。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/user/course-view")
@Tag(name = "用户课程浏览管理", description = "用户端课程浏览记录管理接口")
@CrossOrigin
@SaCheckRole("用户")
public class CourseViewUserController {

    @Resource
    private CourseViewService courseViewService;

    /**
     * 分页查询用户的浏览记录
     */
    @Operation(summary = "查询用户浏览记录", description = "用户分页查询自己的课程浏览记录")
    @GetMapping("/page")
    public Result<PageInfo<CourseViewVO>> getUserCourseViewPage(@Valid CourseViewQueryDTO queryDTO) {
        Integer userId = SaUtil.getLoginId();
        queryDTO.setUserId(userId);
        PageInfo<CourseViewVO> pageInfo = courseViewService.getCourseViewPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取用户浏览记录详情
     */
    @Operation(summary = "获取浏览记录详情", description = "用户获取指定浏览记录的详细信息")
    @GetMapping("/{id}")
    public Result<CourseViewVO> getUserCourseViewById(
            @Parameter(description = "浏览记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        Integer userId = SaUtil.getLoginId();
        courseViewService.validateCourseViewOwnership(id, userId);
        CourseViewVO courseViewVO = courseViewService.getCourseViewById(id);
        return Result.success(courseViewVO);
    }

    /**
     * 删除浏览记录
     */
    @Operation(summary = "删除浏览记录", description = "用户删除指定的浏览记录")
    @DeleteMapping("/{id}")
    public Result<String> deleteUserCourseView(
            @Parameter(description = "浏览记录ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        Integer userId = SaUtil.getLoginId();
        courseViewService.validateCourseViewOwnership(id, userId);
        courseViewService.removeCourseViewById(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除浏览记录
     */
    @Operation(summary = "批量删除浏览记录", description = "用户批量删除自己的浏览记录")
    @DeleteMapping("/batch")
    public Result<String> batchDeleteUserCourseViews(@Valid @RequestBody CourseViewBatchDeleteDTO deleteDTO) {
        Integer userId = SaUtil.getLoginId();
        // 验证所有记录是否属于当前用户
        for (Integer id : deleteDTO.getIds()) {
            courseViewService.validateCourseViewOwnership(id, userId);
        }
        courseViewService.removeCourseViewByIds(deleteDTO.getIds());
        return Result.success("批量删除成功");
    }

    /**
     * 清空用户的课程浏览记录
     */
    @Operation(summary = "清空浏览记录", description = "清空当前用户的所有课程浏览记录")
    @DeleteMapping("/clear")
    public Result<String> clearUserCourseViews() {
        Integer userId = SaUtil.getLoginId();
        courseViewService.clearUserCourseViews(userId);
        return Result.success("清空成功");
    }
}