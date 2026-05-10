package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.CourseSubject;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 课程分类数据访问层接口
 * 
 * 提供课程分类管理的数据操作方法，包括分类的增删改查、批量操作等
 * 支持课程分类的层次管理、关联查询以及数据统计
 * 实现分类数据的高效存储和检索功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseSubjectDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CourseSubject queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param courseSubject 查询条件
     * @return 对象列表
     */
    List<CourseSubject> queryAllByLimit(CourseSubject courseSubject);

    /**
     * 统计总行数
     *
     * @param courseSubject 查询条件
     * @return 总行数
     */
    long count(CourseSubject courseSubject);

    /**
     * 新增数据
     *
     * @param courseSubject 实例对象
     * @return 影响行数
     */
    int insert(CourseSubject courseSubject);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<CourseSubject> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<CourseSubject> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<CourseSubject> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<CourseSubject> entities);

    /**
     * 修改数据
     *
     * @param courseSubject 实例对象
     * @return 影响行数
     */
    int update(CourseSubject courseSubject);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

