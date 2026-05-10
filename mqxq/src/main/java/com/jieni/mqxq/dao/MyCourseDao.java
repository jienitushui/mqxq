package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.MyCourse;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 我的课程数据访问接口
 * 
 * 负责用户课程关联关系的数据库操作，包括课程学习记录的增删改查、用户课程查询等功能
 * 支持多角色查询（学员、教师、管理员）和复杂条件查询，为课程学习管理提供数据访问支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface MyCourseDao {

    /**
     * 通过ID查询单条数据（包含详细信息）
     *
     * @param id 主键
     * @return 实例对象
     */
    MyCourse queryById(Integer id);

    /**
     * 新增数据
     *
     * @param myCourse 实例对象
     * @return 影响行数
     */
    int insert(MyCourse myCourse);

    /**
     * 批量新增数据
     *
     * @param entities List<MyCourse> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<MyCourse> entities);

    /**
     * 修改数据
     *
     * @param myCourse 实例对象
     * @return 影响行数
     */
    int update(MyCourse myCourse);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 批量删除数据
     *
     * @param ids 主键列表
     * @return 影响行数
     */
    int deleteBatchByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据用户ID和课程ID查询我的课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 我的课程对象
     */
    MyCourse queryByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    /**
     * 根据条件查询列表（支持多条件组合查询）
     *
     * @param params 查询参数
     * @return 对象列表
     */
    List<MyCourse> queryListByConditions(@Param("params") Map<String, Object> params);

    /**
     * 统计符合条件的记录数
     *
     * @param params 查询参数
     * @return 记录总数
     */
    long countByConditions(@Param("params") Map<String, Object> params);

    // ========== 兼容性方法（用于其他模块调用，内部调用新方法） ==========

    /**
     * 根据参数查询（兼容性方法）
     *
     * @param params 查询参数
     * @return 对象列表
     */
    List<MyCourse> queryByParams(@Param("params") Map<String, Object> params);

    /**
     * 查询指定行数据（兼容性方法）
     *
     * @param myCourse 查询条件
     * @return 对象列表
     */
    List<MyCourse> queryAllByLimit(MyCourse myCourse);

    /**
     * 统计总行数（兼容性方法）
     *
     * @param myCourse 查询条件
     * @return 总行数
     */
    long count(MyCourse myCourse);

    /**
     * 根据课程ID和用户ID查询（兼容性方法）
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return 我的课程对象
     */
    MyCourse queryByCourseAndUser(@Param("courseId") Integer courseId, @Param("userId") Integer userId);

    /**
     * 根据课程ID查询所有学员（兼容性方法）
     *
     * @param courseId 课程ID
     * @return 学员列表
     */
    List<MyCourse> queryByCourseId(@Param("courseId") Integer courseId);
}

