package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Chapter;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 章节数据访问层接口
 * 
 * 提供章节相关的数据库操作接口，包括章节的增删改查、分页查询、统计查询等功能。
 * 支持单条查询、批量操作、条件筛选等数据库操作。为课程内容管理系统提供数据层支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface ChapterDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Chapter queryById(Integer id);

    /**
     * 查询指定行数据
     * @return 对象列表
     */
    List<Chapter> queryAllByLimit(Chapter chapter);

    /**
     * 统计总行数
     *
     * @param chapter 查询条件
     * @return 总行数
     */
    long count(Chapter chapter);

    /**
     * 新增数据
     *
     * @param chapter 实例对象
     * @return 影响行数
     */
    int insert(Chapter chapter);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Chapter> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Chapter> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Chapter> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Chapter> entities);

    /**
     * 修改数据
     *
     * @param chapter 实例对象
     * @return 影响行数
     */
    int update(Chapter chapter);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

