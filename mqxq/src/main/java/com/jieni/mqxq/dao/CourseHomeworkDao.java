package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.CourseHomework;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 课程作业数据访问层接口
 * 
 * 提供课程作业系统的数据操作方法，包括作业的增删改查、批量操作等
 * 支持作业的多维度查询、截止时间管理、趋势分析以及排行统计
 * 实现作业数据的高效管理和复杂查询支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseHomeworkDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CourseHomework queryById(@Param("id") Integer id);

    /**
     * 根据参数查询作业列表
     *
     * @param params 查询参数
     * @return 对象列表
     */
    List<CourseHomework> queryByParams(@Param("params") Map<String, Object> params);

    /**
     * 管理员查询作业列表（包含课程名称和教师名称）
     *
     * @param params 查询参数
     * @return 对象列表
     */
    List<CourseHomework> queryForAdmin(@Param("params") Map<String, Object> params);

    /**
     * 统计总行数
     *
     * @param params 查询参数
     * @return 总行数
     */
    long countByParams(@Param("params") Map<String, Object> params);

    /**
     * 新增数据
     *
     * @param courseHomework 实例对象
     * @return 影响行数
     */
    int insert(@Param("homework") CourseHomework courseHomework);

    /**
     * 修改数据
     *
     * @param courseHomework 实例对象
     * @return 影响行数
     */
    int update(@Param("homework") CourseHomework courseHomework);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 批量删除数据
     *
     * @param ids 主键列表
     * @return 影响行数
     */
    int batchDelete(@Param("ids") List<Integer> ids);

    /**
     * 批量更新状态
     *
     * @param ids 主键列表
     * @param status 状态值
     * @param updateUser 更新人ID
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("ids") List<Integer> ids, 
                          @Param("status") Integer status, 
                          @Param("updateUser") Integer updateUser);

    /**
     * 查询即将截止的作业
     *
     * @param createUser 创建人ID
     * @param status 状态
     * @param endTimeStart 截止时间起始
     * @param endTimeEnd 截止时间结束
     * @return 对象列表
     */
    List<CourseHomework> queryUpcomingDeadline(@Param("createUser") Integer createUser,
                                               @Param("status") Integer status,
                                               @Param("endTimeStart") LocalDateTime endTimeStart,
                                               @Param("endTimeEnd") LocalDateTime endTimeEnd);

    /**
     * 获取教师作业排行
     *
     * @return 排行数据
     */
    List<Map<String, Object>> getTeacherHomeworkRank();

    /**
     * 获取课程作业排行
     *
     * @return 排行数据
     */
    List<Map<String, Object>> getCourseHomeworkRank();

    /**
     * 获取作业趋势分析
     *
     * @param startDate 开始日期
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getHomeworkTrendAnalysis(@Param("startDate") LocalDateTime startDate);

    /**
     * 获取作业统计信息
     *
     * @return 统计数据
     */
    Map<String, Object> getHomeworkStatistics();
}

