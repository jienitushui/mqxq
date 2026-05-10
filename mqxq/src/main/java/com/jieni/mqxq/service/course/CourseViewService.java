package com.jieni.mqxq.service.course;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.courseview.CourseViewCreateDTO;
import com.jieni.mqxq.domain.dto.courseview.CourseViewQueryDTO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewStatisticsVO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewVO;

import java.util.List;

/**
 * 课程浏览记录服务接口
 * 
 * 提供课程浏览记录的完整业务服务
 * 包括浏览记录管理、统计分析等功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseViewService {

    /**
     * 分页查询课程浏览记录
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageInfo<CourseViewVO> getCourseViewPage(CourseViewQueryDTO queryDTO);

    /**
     * 根据ID获取浏览记录详情
     *
     * @param id 浏览记录ID
     * @return 浏览记录详情
     */
    CourseViewVO getCourseViewById(Integer id);

    /**
     * 记录课程浏览行为
     *
     * @param createDTO 浏览记录创建信息
     * @return 浏览记录详情
     */
    CourseViewVO recordCourseView(CourseViewCreateDTO createDTO);

    /**
     * 删除指定的浏览记录
     *
     * @param id 浏览记录ID
     */
    void removeCourseViewById(Integer id);

    /**
     * 批量删除浏览记录
     *
     * @param ids 浏览记录ID列表
     */
    void removeCourseViewByIds(List<Integer> ids);

    /**
     * 清空指定用户的浏览记录
     *
     * @param userId 用户ID
     */
    void clearUserCourseViews(Integer userId);

    /**
     * 获取平台浏览统计信息（管理员）
     *
     * @return 统计信息
     */
    CourseViewStatisticsVO getPlatformViewStatistics();

    /**
     * 获取教师课程浏览统计信息
     *
     * @param teacherId 教师ID
     * @return 统计信息
     */
    CourseViewStatisticsVO getTeacherCourseViewStatistics(Integer teacherId);

    /**
     * 记录或更新课程浏览行为（幂等）
     * 如果已存在当前用户的浏览记录则仅更新时间和环境信息
     *
     * @param createDTO 浏览记录创建信息
     * @return 浏览记录详情
     */
    CourseViewVO recordOrUpdateCourseView(CourseViewCreateDTO createDTO);

    /**
     * 验证浏览记录是否属于指定用户
     *
     * @param viewId 浏览记录ID
     * @param userId 用户ID
     */
    void validateCourseViewOwnership(Integer viewId, Integer userId);
}
