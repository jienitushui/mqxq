package com.jieni.mqxq.service.course;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.course.MyCourseCreateDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseQueryDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseUpdateDTO;
import com.jieni.mqxq.domain.vo.course.MyCourseStatisticsVO;
import com.jieni.mqxq.domain.vo.course.MyCourseVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 我的课程服务接口
 * 
 * 提供用户课程关系管理的完整业务接口，包括课程加入、学习状态管理、学习统计等功能
 * 支持多角色（学员、教师、管理员）的课程数据查询和管理服务
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface MyCourseService {

    // ========== 基础CRUD方法 ==========

    /**
     * 根据ID查询我的课程详情
     *
     * @param id 主键ID
     * @return 课程详情VO
     */
    MyCourseVO getMyCourseById(Integer id);

    /**
     * 根据用户ID和课程ID查询我的课程详情
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 课程详情VO
     */
    MyCourseVO getMyCourseByCourseId(Integer userId, Integer courseId);

    /**
     * 用户加入课程
     *
     * @param userId    用户ID
     * @param createDTO 创建参数
     * @return 课程VO
     */
    MyCourseVO joinCourse(Integer userId, MyCourseCreateDTO createDTO);

    /**
     * 更新学习状态
     *
     * @param userId    用户ID
     * @param updateDTO 更新参数
     * @return 课程VO
     */
    MyCourseVO updateStudyProgress(Integer userId, MyCourseUpdateDTO updateDTO);

    /**
     * 用户退出课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     */
    void quitCourse(Integer userId, Integer courseId);

    /**
     * 根据ID删除课程记录
     *
     * @param id 主键ID
     */
    void deleteMyCourseById(Integer id);

    /**
     * 批量删除课程记录
     *
     * @param ids ID列表
     * @return 删除成功的数量
     */
    int batchDeleteMyCourses(List<Integer> ids);

    // ========== 查询方法 ==========

    /**
     * 检查用户是否已加入课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否已加入
     */
    boolean hasJoinedCourse(Integer userId, Integer courseId);

    /**
     * 分页查询我的课程列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageInfo<MyCourseVO> getMyCourseListByPage(MyCourseQueryDTO queryDTO);

    // ========== 统计方法 ==========

    /**
     * 获取用户学习统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    MyCourseStatisticsVO getUserStudyStatistics(Integer userId);

    /**
     * 获取课程学习统计
     *
     * @param courseId 课程ID
     * @return 统计信息
     */
    MyCourseStatisticsVO getCourseStudyStatistics(Integer courseId);

    /**
     * 获取教师课程统计
     *
     * @param teacherId 教师ID
     * @return 统计信息
     */
    MyCourseStatisticsVO getTeacherCoursesStatistics(Integer teacherId);

    /**
     * 获取平台总体学习统计
     *
     * @return 统计信息
     */
    MyCourseStatisticsVO getOverallStudyStatistics();

    // ========== 教师端方法 ==========

    /**
     * 检查教师课程权限
     *
     * @param teacherId 教师ID
     * @param courseId  课程ID
     * @return 是否有权限
     */
    boolean checkTeacherCoursePermission(Integer teacherId, Integer courseId);

    /**
     * 教师批量移除学员
     *
     * @param myCourseIds 我的课程ID列表
     * @param teacherId   教师ID
     * @return 移除成功的数量
     */
    int batchRemoveStudentsByTeacher(List<Integer> myCourseIds, Integer teacherId);

    /**
     * 导出课程学员信息
     *
     * @param courseId 课程ID
     * @param response HTTP响应
     */
    void exportCourseStudents(Integer courseId, HttpServletResponse response);

    // ========== 学习状态自动流转方法 ==========

    /**
     * 检查并更新学习状态为"学习中"
     * 当用户首次学习行为时调用（观看课程、提交作业等）
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否更新了状态
     */
    boolean checkAndStartLearning(Integer userId, Integer courseId);

    /**
     * 检查并更新学习状态为"已完成"
     * 根据学习进度自动判断是否完成
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否更新了状态
     */
    boolean checkAndCompleteCourse(Integer userId, Integer courseId);

    /**
     * 判断课程是否学习完成
     * 根据观看记录、作业完成情况等综合判断
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否完成
     */
    boolean isCourseLearningCompleted(Integer userId, Integer courseId);

    /**
     * 重置课程学习状态为"学习中"
     * 当课程内容更新（新增章节、小节、作业）时，将已完成的用户状态重置为学习中
     *
     * @param courseId 课程ID
     * @param reason   重置原因（用于日志记录）
     * @return 重置的用户数量
     */
    int resetCompletedStatusToLearning(Integer courseId, String reason);
}
