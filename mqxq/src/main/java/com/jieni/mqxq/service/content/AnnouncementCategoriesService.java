package com.jieni.mqxq.service.content;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryCreateDTO;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryPageQueryDTO;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryUpdateDTO;
import com.jieni.mqxq.domain.entity.AnnouncementCategories;
import com.jieni.mqxq.domain.vo.content.AnnouncementCategoryVO;

import java.util.List;

/**
 * 公告分类服务接口
 * 
 * 提供公告分类管理的完整服务功能，包括分类的CRUD操作、分页查询、名称唯一性检查等。
 * 支持公告分类的展示、筛选、管理等业务场景，为公告系统提供基础数据管理能力。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface AnnouncementCategoriesService {

    /**
     * 创建公告分类
     *
     * @param createDTO 创建DTO
     * @param createUserId 创建者ID
     * @return 公告分类VO
     */
    AnnouncementCategoryVO createCategory(AnnouncementCategoryCreateDTO createDTO, Integer createUserId);

    /**
     * 更新公告分类
     *
     * @param id 分类ID
     * @param updateDTO 更新DTO
     * @param updateUserId 更新者ID
     * @return 公告分类VO
     */
    AnnouncementCategoryVO updateCategory(Integer id, AnnouncementCategoryUpdateDTO updateDTO, Integer updateUserId);

    /**
     * 删除公告分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Integer id);

    /**
     * 根据ID获取公告分类详情
     *
     * @param id 分类ID
     * @return 公告分类VO
     */
    AnnouncementCategoryVO getCategoryById(Integer id);

    /**
     * 分页查询公告分类
     *
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    PageInfo<AnnouncementCategoryVO> getCategoryPage(AnnouncementCategoryPageQueryDTO queryDTO);

    /**
     * 获取所有公告分类
     *
     * @return 所有公告分类列表
     */
    List<AnnouncementCategoryVO> getAllCategories();

    /**
     * 检查分类名称是否已存在
     *
     * @param name 分类名称
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isCategoryNameExists(String name, Integer excludeId);
}
