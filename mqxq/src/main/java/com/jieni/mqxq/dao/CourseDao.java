package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Course;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 课程数据访问层接口
 * 
 * 提供课程相关的数据库操作接口，包括课程的增删改查、分页查询、条件筛选等功能。
 * 支持单条查询、批量操作、统计查询等数据库操作。与课程服务层配合实现完整的业务逻辑。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Course queryById(Integer id);

    /**
     * 查询指定行数据
     * @return 对象列表
     */
    List<Course> queryAllByLimit(@Param("course") Course course);

    /**
     * 统计总行数
     *
     * @param course 查询条件
     * @return 总行数
     */
    long count(@Param("course") Course course);

    /**
     * 新增数据
     *
     * @param course 实例对象
     * @return 影响行数
     */
    int insert(Course course);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Course> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Course> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Course> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Course> entities);

    /**
     * 修改数据
     *
     * @param course 实例对象
     * @return 影响行数
     */
    int update(Course course);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

