package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.CourseComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 课程评论数据访问层接口
 * 
 * 提供课程评论系统的数据操作方法，包括评论的增删改查、批量操作等。
 * 支持评论的多维度查询、评分统计、趋势分析以及排行统计。
 * 实现评论数据的高效管理和复杂查询支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseCommentDao {

    // ========== 基础CRUD方法 ==========

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CourseComment queryById(@Param("id") Integer id);

    /**
     * 查询指定行数据
     * 
     * @param courseComment 查询条件
     * @return 对象列表
     */
    List<CourseComment> queryAllByLimit(CourseComment courseComment);

    /**
     * 统计总行数
     *
     * @param courseComment 查询条件
     * @return 总行数
     */
    long count(CourseComment courseComment);

    /**
     * 新增数据
     *
     * @param courseComment 实例对象
     * @return 影响行数
     */
    int insert(CourseComment courseComment);

    /**
     * 批量新增数据
     *
     * @param entities 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<CourseComment> entities);

    /**
     * 批量新增或按主键更新数据
     *
     * @param entities 实例对象列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<CourseComment> entities);

    /**
     * 修改数据
     *
     * @param courseComment 实例对象
     * @return 影响行数
     */
    int update(CourseComment courseComment);

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
     * @param ids ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("ids") List<Integer> ids);

    /**
     * 批量更新状态
     *
     * @param ids ID列表
     * @param status 状态值
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    // ========== 扩展查询方法 ==========

    /**
     * 根据参数查询
     *
     * @param params 查询参数
     * @return 对象列表
     */
    List<CourseComment> queryByParams(@Param("params") Map<String, Object> params);

    /**
     * 查询所有记录
     *
     * @return 对象列表
     */
    List<CourseComment> queryAll();

    /**
     * 教师查询评论
     *
     * @param params 查询参数
     * @return 对象列表
     */
    List<CourseComment> queryCommentsForTeacher(@Param("params") Map<String, Object> params);

    /**
     * 管理员查询评论
     *
     * @param params 查询参数
     * @return 对象列表
     */
    List<CourseComment> queryForAdmin(@Param("params") Map<String, Object> params);

    /**
     * 获取评论趋势分析
     *
     * @param params 查询参数
     * @return 趋势数据
     */
    Map<String, Object> getCommentTrendAnalysis(@Param("params") Map<String, Object> params);

    /**
     * 获取课程评分排行
     *
     * @param limit 限制数量
     * @return 排行数据
     */
    List<Map<String, Object>> getCourseRatingRank(@Param("limit") int limit);
    
    /**
     * 获取课程的平均评分
     * 
     * @param courseId 课程ID
     * @return 平均评分，如果没有评论则返回0.0
     */
    Double getAverageRatingByCourseId(@Param("courseId") Integer courseId);
}
