package com.jieni.mqxq.service.course;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.course.*;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.vo.course.CourseDetailVO;
import com.jieni.mqxq.domain.vo.course.CourseListVO;
import com.jieni.mqxq.domain.vo.course.CourseStatisticsVO;
import com.jieni.mqxq.domain.vo.course.SimpleCourseVO;

import java.util.List;

/**
 * 课程服务接口
 * 
 * 提供课程管理的完整业务功能，包括课程的CRUD操作、分页查询、
 * 发布管理、权限校验等功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseService {

    // ==================== 管理员端方法 ====================

    /**
     * 管理员分页查询所有课程
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    PageInfo<CourseListVO> pageCoursesForAdmin(CoursePageQueryDTO queryDTO);

    /**
     * 管理员创建课程
     *
     * @param adminId 管理员ID
     * @param createDTO 创建DTO
     * @return 课程ID
     */
    Integer createCourseByAdmin(Integer adminId, CreateCourseDTO createDTO);

    /**
     * 管理员更新课程
     *
     * @param courseId 课程ID
     * @param adminId 管理员ID
     * @param updateDTO 更新DTO
     */
    void updateCourseByAdmin(Integer courseId, Integer adminId, UpdateCourseDTO updateDTO);

    /**
     * 管理员发布课程
     *
     * @param courseId 课程ID
     * @param adminId 管理员ID
     */
    void publishCourseByAdmin(Integer courseId, Integer adminId);

    /**
     * 管理员取消发布课程
     *
     * @param courseId 课程ID
     * @param adminId 管理员ID
     */
    void unpublishCourseByAdmin(Integer courseId, Integer adminId);

    /**
     * 管理员删除课程
     *
     * @param courseId 课程ID
     * @param adminId 管理员ID
     */
    void deleteCourseByAdmin(Integer courseId, Integer adminId);

    /**
     * 管理员获取课程统计信息
     *
     * @return 统计信息
     */
    CourseStatisticsVO getCourseStatistics();

    // ==================== 用户端方法 ====================

    /**
     * 用户分页查询已发布课程
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    PageInfo<CourseListVO> pagePublishedCourses(PublishedCourseQueryDTO queryDTO);

    /**
     * 用户获取已发布课程详情
     *
     * @param courseId 课程ID
     * @param userId 用户ID（可为null，用于记录浏览）
     * @return 课程详情VO
     */
    CourseDetailVO getPublishedCourseDetail(Integer courseId, Integer userId);

    /**
     * 获取热门课程列表
     *
     * @param limit 数量限制
     * @return 课程列表
     */
    List<CourseListVO> listHotCourses(Integer limit);

    /**
     * 分页查询免费课程
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<CourseListVO> pageFreeCourses(Integer pageNum, Integer pageSize);

    // ==================== 教师端方法 ====================

    /**
     * 根据ID获取课程详情
     *
     * @param id 课程ID
     * @return 课程详情VO
     */
    CourseDetailVO getCourseDetailById(Integer id);

    /**
     * 教师分页查询自己的课程
     *
     * @param teacherId 教师ID
     * @param queryDTO 查询条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<CourseDetailVO> pageTeacherCourses(Integer teacherId, CourseQueryDTO queryDTO, Integer pageNum, Integer pageSize);

    /**
     * 获取教师的简化课程列表
     *
     * @param teacherId 教师ID
     * @return 简化课程列表
     */
    List<SimpleCourseVO> listSimpleCoursesByTeacher(Integer teacherId);

    /**
     * 教师创建课程
     *
     * @param teacherId 教师ID
     * @param createDTO 创建DTO
     * @return 课程详情VO
     */
    CourseDetailVO createCourse(Integer teacherId, CreateCourseDTO createDTO);

    /**
     * 教师更新课程
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param updateDTO 更新DTO
     * @return 课程详情VO
     */
    CourseDetailVO updateCourse(Integer courseId, Integer teacherId, UpdateCourseDTO updateDTO);

    /**
     * 教师发布课程
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     */
    void publishCourse(Integer courseId, Integer teacherId);

    /**
     * 教师取消发布课程
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     */
    void unpublishCourse(Integer courseId, Integer teacherId);

    /**
     * 教师删除课程
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     */
    void deleteCourse(Integer courseId, Integer teacherId);

    // ==================== 内部辅助方法 ====================

    /**
     * 根据ID查询课程（内部使用，返回Entity）
     *
     * @param id 课程ID
     * @return 课程实体
     */
    Course queryById(Integer id);

    /**
     * 根据条件查询课程列表（内部使用，返回Entity列表）
     *
     * @param condition 查询条件
     * @return 课程实体列表
     */
    List<Course> queryAll(Course condition);

    /**
     * 更新课程（内部使用，接收Entity）
     *
     * @param course 课程实体
     * @return 更新后的课程实体
     */
    Course update(Course course);

    /**
     * 验证课程是否属于指定教师
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 是否属于该教师
     */
    boolean isCourseOwnedByTeacher(Integer courseId, Integer teacherId);

    /**
     * 检查课程是否可以发布
     *
     * @param courseId 课程ID
     * @return 是否可以发布
     */
    boolean canPublishCourse(Integer courseId);

    /**
     * 记录课程浏览
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     */
    void recordCourseView(Integer courseId, Integer userId);
}
