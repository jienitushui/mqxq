package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.AnnouncementCategories;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告分类数据访问层接口
 * 
 * 提供公告分类的基础数据操作方法，包括增删改查、条件查询等
 * 支持分类名称唯一性校验、分类数据统计以及关联查询
 * 实现与MyBatis的映射交互，提供高效的数据访问能力
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface AnnouncementCategoriesDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AnnouncementCategories queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param announcementCategories 查询条件
     * @return 对象列表
     */
    List<AnnouncementCategories> queryAllByLimit(AnnouncementCategories announcementCategories);

    /**
     * 统计总行数
     *
     * @param announcementCategories 查询条件
     * @return 总行数
     */
    long count(AnnouncementCategories announcementCategories);

    /**
     * 新增数据
     *
     * @param announcementCategories 实例对象
     * @return 影响行数
     */
    int insert(AnnouncementCategories announcementCategories);

    /**
     * 修改数据
     *
     * @param announcementCategories 实例对象
     * @return 影响行数
     */
    int update(AnnouncementCategories announcementCategories);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 查询所有公告分类
     *
     * @return 所有公告分类列表
     */
    List<AnnouncementCategories> queryAll();

    /**
     * 根据分类名称查询分类
     *
     * @param name 分类名称
     * @return 分类对象
     */
    AnnouncementCategories queryByName(@Param("name") String name);

    /**
     * 检查分类名称是否已存在
     *
     * @param name 分类名称
     * @return 是否存在
     */
    boolean existsByName(@Param("name") String name);
}

