package com.jieni.mqxq.service.content;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.content.*;
import com.jieni.mqxq.domain.vo.content.AnnouncementSimpleVO;
import com.jieni.mqxq.domain.vo.content.AnnouncementVO;

import java.util.List;

/**
 * 公告服务接口
 * 
 * 提供公告管理的完整服务功能，包括公告的CRUD操作、发布管理、状态控制、分页查询、搜索等。
 * 支持公告的发布、撤回、批量删除、统计分析等高级功能，为平台公告系统提供全面的数据管理服务。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface AnnouncementsService {

    /**
     * 创建公告
     *
     * @param createDTO 创建DTO
     * @param createUserId 创建者ID
     * @return 公告VO
     */
    AnnouncementVO createAnnouncement(AnnouncementCreateDTO createDTO, Integer createUserId);

    /**
     * 更新公告
     *
     * @param id 公告ID
     * @param updateDTO 更新DTO
     * @param updateUserId 更新者ID
     * @return 公告VO
     */
    AnnouncementVO updateAnnouncement(Integer id, AnnouncementUpdateDTO updateDTO, Integer updateUserId);

    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    void deleteAnnouncement(Integer id);

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     */
    void batchDeleteAnnouncements(List<Integer> ids);

    /**
     * 根据ID获取公告详情
     *
     * @param id 公告ID
     * @return 公告VO
     */
    AnnouncementVO getAnnouncementById(Integer id);

    /**
     * 分页查询公告（管理员）
     *
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    PageInfo<AnnouncementSimpleVO> getAnnouncementPage(AnnouncementPageQueryDTO queryDTO);

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @param publishDTO 发布DTO
     * @return 公告VO
     */
    AnnouncementVO publishAnnouncement(Integer id, AnnouncementPublishDTO publishDTO);

    /**
     * 撤回公告
     *
     * @param id 公告ID
     * @return 公告VO
     */
    AnnouncementVO withdrawAnnouncement(Integer id);

    /**
     * 根据ID获取已发布的公告详情（公共接口）
     *
     * @param id 公告ID
     * @return 公告VO
     */
    AnnouncementVO getPublishedAnnouncementById(Integer id);

    /**
     * 分页查询已发布的公告（公共接口）
     *
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    PageInfo<AnnouncementSimpleVO> getPublishedAnnouncementPage(AnnouncementPageQueryDTO queryDTO);

    /**
     * 获取最新公告列表
     *
     * @param count 获取数量
     * @return 最新公告列表
     */
    List<AnnouncementSimpleVO> getLatestAnnouncements(Integer count);

    /**
     * 根据分类获取已发布的公告
     *
     * @param categoryId 分类ID
     * @return 公告列表
     */
    List<AnnouncementSimpleVO> getPublishedAnnouncementsByCategory(Integer categoryId);

    /**
     * 搜索已发布的公告
     *
     * @param searchDTO 搜索DTO
     * @return 搜索结果
     */
    PageInfo<AnnouncementSimpleVO> searchPublishedAnnouncements(AnnouncementSearchDTO searchDTO);
}
