package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Announcements;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 公告数据访问层接口
 * 
 * 提供公告管理的全面数据操作方法，包括公告的发布、撤回、查询等
 * 支持公告的批量操作、状态管理、分类查询以及搜索功能
 * 实现公告数据的高效存储管理和统计分析
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface AnnouncementsDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Announcements queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param announcements 查询条件
     * @return 对象列表
     */
    List<Announcements> queryAllByLimit(Announcements announcements);

    /**
     * 统计总行数
     *
     * @param announcements 查询条件
     * @return 总行数
     */
    long count(Announcements announcements);

    /**
     * 新增数据
     *
     * @param announcements 实例对象
     * @return 影响行数
     */
    int insert(Announcements announcements);

    /**
     * 修改数据
     *
     * @param announcements 实例对象
     * @return 影响行数
     */
    int update(Announcements announcements);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @param publishDate 发布时间
     * @return 影响行数
     */
    int publishAnnouncement(@Param("id") Integer id, @Param("publishDate") Date publishDate);

    /**
     * 撤回公告
     *
     * @param id 公告ID
     * @return 影响行数
     */
    int withdrawAnnouncement(@Param("id") Integer id);

    /**
     * 分页查询已发布的公告
     *
     * @param announcements 查询条件
     * @return 对象列表
     */
    List<Announcements> queryPublishedByLimit(Announcements announcements);

    /**
     * 统计已发布的公告数量
     *
     * @param announcements 查询条件
     * @return 已发布公告数量
     */
    long countPublished(Announcements announcements);

    /**
     * 根据ID查询已发布的公告
     *
     * @param id 公告ID
     * @return 公告对象
     */
    Announcements queryPublishedById(Integer id);

    /**
     * 获取最新公告列表
     *
     * @param count 获取数量
     * @return 最新公告列表
     */
    List<Announcements> getLatestAnnouncements(@Param("count") Integer count);

    /**
     * 根据分类获取已发布的公告
     *
     * @param categoryId 分类ID
     * @return 公告列表
     */
    List<Announcements> getPublishedByCategory(@Param("categoryId") Integer categoryId);

    /**
     * 搜索已发布的公告
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    List<Announcements> searchPublishedAnnouncements(@Param("keyword") String keyword);

    /**
     * 统计搜索结果的已发布公告数量
     *
     * @param keyword 搜索关键词
     * @return 搜索结果数量
     */
    long countSearchPublished(@Param("keyword") String keyword);

    /**
     * 根据标题模糊查询公告
     *
     * @param title 标题关键词
     * @return 公告列表
     */
    List<Announcements> queryByTitleLike(@Param("title") String title);

    /**
     * 根据创建者查询公告
     *
     * @param createUser 创建者ID
     * @return 公告列表
     */
    List<Announcements> queryByCreateUser(@Param("createUserId") Integer createUser);

    /**
     * 获取公告统计信息
     *
     * @return 统计信息
     */
    List<Announcements> getStatistics();
}

