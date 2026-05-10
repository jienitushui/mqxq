package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Carousel;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 轮播图数据访问层接口
 * 
 * 提供首页轮播图的数据操作方法，包括轮播图的增删改查、批量操作等
 * 支持轮播图的排序管理、状态控制以及显示管理
 * 实现轮播图数据的高效存储和检索功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CarouselDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Carousel queryById(Integer id);

    /**
     * 查询指定行数据
     * @return 对象列表
     */
    List<Carousel> queryAllByLimit(Carousel carousel);

    /**
     * 统计总行数
     *
     * @param carousel 查询条件
     * @return 总行数
     */
    long count(Carousel carousel);

    /**
     * 新增数据
     *
     * @param carousel 实例对象
     * @return 影响行数
     */
    int insert(Carousel carousel);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Carousel> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Carousel> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Carousel> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Carousel> entities);

    /**
     * 修改数据
     *
     * @param carousel 实例对象
     * @return 影响行数
     */
    int update(Carousel carousel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

