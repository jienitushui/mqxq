package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Section;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 课程章节小节数据访问接口
 * 
 * 负责课程章节小节的数据库操作，包括小节信息的增删改查、批量操作等功能
 * 支持分页查询、条件查询以及统计功能，为课程章节管理提供数据访问支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface SectionDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Section queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param section 查询条件
     * @return 对象列表
     */
    List<Section> queryAllByLimit(@Param("section") Section section);

    /**
     * 统计总行数
     *
     * @param section 查询条件
     * @return 总行数
     */
    long count(@Param("section") Section section);

    /**
     * 新增数据
     *
     * @param section 实例对象
     * @return 影响行数
     */
    int insert(Section section);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Section> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Section> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Section> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Section> entities);

    /**
     * 修改数据
     *
     * @param section 实例对象
     * @return 影响行数
     */
    int update(Section section);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

