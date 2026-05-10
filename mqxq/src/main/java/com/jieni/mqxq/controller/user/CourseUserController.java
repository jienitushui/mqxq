package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.course.PublishedCourseQueryDTO;
import com.jieni.mqxq.domain.dto.courseview.CourseViewQueryDTO;
import com.jieni.mqxq.domain.vo.course.CourseDetailVO;
import com.jieni.mqxq.domain.vo.course.CourseListVO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewVO;
import com.jieni.mqxq.domain.vo.order.PurchaseStatusVO;
import com.jieni.mqxq.service.course.CourseService;
import com.jieni.mqxq.service.course.CourseViewService;
import com.jieni.mqxq.service.order.CoursePurchaseService;
import com.jieni.mqxq.util.SaUtil;
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
 * 用户端课程管理控制器
 * 
 * 提供用户查看和浏览课程的完整功能，包括课程列表查询、详情查看、浏览记录、购买状态检查等。
 * 支持分页查询、热门课程、免费课程、学习权限校验等功能。只有用户角色可以访问。
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/user/course")
@Tag(name = "用户课程管理", description = "用户端课程管理接口")
@CrossOrigin
@SaCheckRole("用户")
@Validated
public class CourseUserController {

    @Resource
    private CourseService courseService;

    @Resource
    private CourseViewService courseViewService;

    @Resource
    private CoursePurchaseService coursePurchaseService;

    /**
     * 分页查询已发布的课程
     *
     * @param queryDTO 查询条件DTO
     * @return 已发布课程列表分页结果
     */
    @Operation(summary = "分页查询已发布课程", description = "用户分页查询已发布的课程列表，支持按标题和分类筛选")
    @GetMapping("/page")
    public Result<PageInfo<CourseListVO>> pagePublishedCourses(@Valid PublishedCourseQueryDTO queryDTO) {
        log.info("用户分页查询已发布课程, queryDTO: {}", queryDTO);
        PageInfo<CourseListVO> pageInfo = courseService.pagePublishedCourses(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID获取已发布课程详情
     *
     * @param id 课程ID
     * @return 课程详细信息
     */
    @Operation(summary = "获取已发布课程详情", description = "用户根据ID获取已发布课程的详细信息，自动记录浏览历史")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> getPublishedCourseDetail(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer id) {
        Integer userId = SaUtil.getLoginId();
        log.info("用户获取已发布课程详情, userId: {}, courseId: {}", userId, id);
        CourseDetailVO courseDetail = courseService.getPublishedCourseDetail(id, userId);
        return Result.success(courseDetail);
    }

    /**
     * 获取热门课程
     *
     * @param limit 返回的课程数量
     * @return 热门课程列表
     */
    @Operation(summary = "获取热门课程", description = "获取浏览量最高的热门课程列表")
    @GetMapping("/hot")
    public Result<List<CourseListVO>> getHotCourses(
            @Parameter(description = "数量限制", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "数量必须大于0") @Max(value = 100, message = "数量不能超过100") Integer limit) {
        log.info("用户获取热门课程, limit: {}", limit);
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
    @Operation(summary = "获取免费课程", description = "分页查询价格为0的免费课程")
    @GetMapping("/free")
    public Result<PageInfo<CourseListVO>> getFreeCourses(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer pageNum,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") @Max(value = 100, message = "每页大小不能超过100") Integer pageSize) {
        log.info("用户获取免费课程, pageNum: {}, pageSize: {}", pageNum, pageSize);
        PageInfo<CourseListVO> pageInfo = courseService.pageFreeCourses(pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取用户的课程浏览记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 浏览记录分页结果
     */
    @Operation(summary = "获取浏览记录", description = "获取当前用户的课程浏览历史记录")
    @GetMapping("/view-history")
    @SaCheckLogin
    public Result<PageInfo<CourseViewVO>> getUserViewHistory(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer pageNum,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") @Max(value = 100, message = "每页大小不能超过100") Integer pageSize) {
        Integer userId = SaUtil.getLoginId();
        log.info("用户获取浏览记录, userId: {}, pageNum: {}, pageSize: {}", userId, pageNum, pageSize);
        
        CourseViewQueryDTO queryDTO = new CourseViewQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        
        PageInfo<CourseViewVO> pageInfo = courseViewService.getCourseViewPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 检查课程购买状态
     *
     * @param courseId 课程ID
     * @return 购买状态信息
     */
    @Operation(summary = "检查课程购买状态", description = "检查用户是否已购买指定课程，返回详细购买状态")
    @GetMapping("/purchase-status/{courseId}")
    @SaCheckLogin
    public Result<PurchaseStatusVO> checkPurchaseStatus(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer userId = SaUtil.getLoginId();
        log.info("检查课程购买状态, userId: {}, courseId: {}", userId, courseId);
        PurchaseStatusVO response = coursePurchaseService.checkPurchaseStatus(userId, courseId);
        return Result.success(response);
    }

    /**
     * 检查是否可以学习课程
     *
     * @param courseId 课程ID
     * @return 是否可以学习
     */
    @Operation(summary = "检查学习权限", description = "检查用户是否可以学习指定课程（免费或已购买）")
    @GetMapping("/can-study/{courseId}")
    @SaCheckLogin
    public Result<Boolean> canStudyCourse(
            @Parameter(description = "课程ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "课程ID必须大于0") Integer courseId) {
        Integer userId = SaUtil.getLoginId();
        log.info("检查课程学习权限, userId: {}, courseId: {}", userId, courseId);
        boolean canStudy = coursePurchaseService.canStudyCourse(userId, courseId);
        return Result.success(canStudy);
    }
}
