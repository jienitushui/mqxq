package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.CourseView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程浏览记录数据访问层接口
 * 
 * 提供课程浏览记录的数据操作方法，包括浏览历史的增删改查、批量操作等
 * 支持用户浏览行为的跟踪记录、数据统计以及历史数据清理
 * 实现浏览数据的高效管理和分析查询
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseViewDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CourseView queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param courseView 查询条件
     * @return 对象列表
     */
    List<CourseView> queryAllByLimit(CourseView courseView);

    /**
     * 统计总行数
     *
     * @param courseView 查询条件
     * @return 总行数
     */
    long count(CourseView courseView);

    /**
     * 新增数据
     *
     * @param courseView 实例对象
     * @return 影响行数
     */
    int insert(CourseView courseView);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<CourseView> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<CourseView> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<CourseView> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<CourseView> entities);

    /**
     * 修改数据
     *
     * @param courseView 实例对象
     * @return 影响行数
     */
    int update(CourseView courseView);

    /**
     * 通过主键批量删除数据
     *
     * @param ids 主键列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

