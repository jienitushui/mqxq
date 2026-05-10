package com.jieni.mqxq.service.impl.content;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.AnnouncementCategoriesDao;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryCreateDTO;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryPageQueryDTO;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryUpdateDTO;
import com.jieni.mqxq.domain.entity.AnnouncementCategories;
import com.jieni.mqxq.domain.vo.content.AnnouncementCategoryVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.content.AnnouncementCategoriesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告分类服务实现类
 * 
 * 提供公告分类的完整业务逻辑实现，包括公告分类的增删改查、分页查询等功能。
 * 支持分类的创建、更新、删除以及重复性检查等业务逻辑。
 * 为公告管理模块提供基础的分类服务支持。
 * 
 * 主要功能：
 * - 公告分类的基础CRUD操作
 * - 分类名称唯一性验证
 * - 分页查询和条件筛选
 * - Entity与VO之间的转换
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class AnnouncementCategoriesServiceImpl implements AnnouncementCategoriesService {

    @Resource
    private AnnouncementCategoriesDao announcementCategoriesDao;

    /**
     * 创建公告分类
     * 
     * @param createDTO 创建DTO
     * @param createUserId 创建者ID
     * @return 公告分类VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementCategoryVO createCategory(AnnouncementCategoryCreateDTO createDTO, Integer createUserId) {
        log.info("创建公告分类，创建者ID: {}, 分类名称: {}", createUserId, createDTO.getName());
        
        // 检查分类名称是否已存在
        if (isCategoryNameExists(createDTO.getName(), null)) {
            throw new MyException("分类名称已存在，请使用其他名称");
        }
        
        // DTO转Entity
        AnnouncementCategories category = new AnnouncementCategories();
        category.setName(createDTO.getName().trim());
        category.setDescription(createDTO.getDescription());
        
        // 设置创建和更新信息
        Date now = new Date();
        category.setCreateTime(now);
        category.setCreateUser(createUserId);
        category.setUpdateTime(now);
        category.setUpdateUser(createUserId);
        
        // 保存到数据库
        announcementCategoriesDao.insert(category);
        
        log.info("公告分类创建成功，分类ID: {}", category.getId());
        
        // Entity转VO
        return convertToVO(category);
    }

    /**
     * 更新公告分类
     * 
     * @param id 分类ID
     * @param updateDTO 更新DTO
     * @param updateUserId 更新者ID
     * @return 公告分类VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementCategoryVO updateCategory(Integer id, AnnouncementCategoryUpdateDTO updateDTO, Integer updateUserId) {
        log.info("更新公告分类，分类ID: {}, 更新者ID: {}", id, updateUserId);
        
        // 检查分类是否存在
        AnnouncementCategories existingCategory = announcementCategoriesDao.queryById(id);
        if (existingCategory == null) {
            throw new MyException("公告分类不存在，ID: " + id);
        }
        
        // 如果更新名称，检查名称是否重复
        if (updateDTO.getName() != null && !updateDTO.getName().trim().isEmpty()) {
            if (isCategoryNameExists(updateDTO.getName(), id)) {
                throw new MyException("分类名称已存在，请使用其他名称");
            }
            existingCategory.setName(updateDTO.getName().trim());
        }
        
        // 更新其他字段
        if (updateDTO.getDescription() != null) {
            existingCategory.setDescription(updateDTO.getDescription());
        }
        
        // 设置更新信息
        existingCategory.setUpdateTime(new Date());
        existingCategory.setUpdateUser(updateUserId);
        
        // 更新数据库
        announcementCategoriesDao.update(existingCategory);
        
        log.info("公告分类更新成功，分类ID: {}", id);
        
        // 重新查询并转换为VO
        AnnouncementCategories updated = announcementCategoriesDao.queryById(id);
        return convertToVO(updated);
    }

    /**
     * 删除公告分类
     * 
     * @param id 分类ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Integer id) {
        log.info("删除公告分类，分类ID: {}", id);
        
        // 检查分类是否存在
        AnnouncementCategories category = announcementCategoriesDao.queryById(id);
        if (category == null) {
            throw new MyException("公告分类不存在，ID: " + id);
        }
        
        // TODO: 检查是否有关联的公告数据，如有则不允许删除或提示用户
        
        // 删除分类
        int rows = announcementCategoriesDao.deleteById(id);
        if (rows <= 0) {
            throw new MyException("删除公告分类失败");
        }
        
        log.info("公告分类删除成功，分类ID: {}", id);
    }

    /**
     * 根据ID获取公告分类详情
     * 
     * @param id 分类ID
     * @return 公告分类VO
     */
    @Override
    public AnnouncementCategoryVO getCategoryById(Integer id) {
        log.debug("查询公告分类详情，分类ID: {}", id);
        
        AnnouncementCategories category = announcementCategoriesDao.queryById(id);
        if (category == null) {
            throw new MyException("公告分类不存在，ID: " + id);
        }
        
        return convertToVO(category);
    }

    /**
     * 分页查询公告分类
     * 
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    @Override
    public PageInfo<AnnouncementCategoryVO> getCategoryPage(AnnouncementCategoryPageQueryDTO queryDTO) {
        log.debug("分页查询公告分类，页码: {}, 每页大小: {}", queryDTO.getPage(), queryDTO.getSize());
        
        // 构建查询条件
        AnnouncementCategories queryCondition = new AnnouncementCategories();
        if (queryDTO.getName() != null && !queryDTO.getName().trim().isEmpty()) {
            queryCondition.setName(queryDTO.getName().trim());
        }
        
        // 开启分页
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        List<AnnouncementCategories> list = announcementCategoriesDao.queryAllByLimit(queryCondition);
        PageInfo<AnnouncementCategories> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<AnnouncementCategoryVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构造返回的PageInfo
        PageInfo<AnnouncementCategoryVO> resultPageInfo = new PageInfo<>(voList);
        BeanUtil.copyProperties(pageInfo, resultPageInfo);
        
        return resultPageInfo;
    }

    /**
     * 获取所有公告分类
     * 
     * @return 所有公告分类列表
     */
    @Override
    public List<AnnouncementCategoryVO> getAllCategories() {
        log.debug("查询所有公告分类");
        
        List<AnnouncementCategories> list = announcementCategoriesDao.queryAll();
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 检查分类名称是否已存在
     * 
     * @param name 分类名称
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    @Override
    public boolean isCategoryNameExists(String name, Integer excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        AnnouncementCategories existing = announcementCategoriesDao.queryByName(name.trim());
        if (existing == null) {
            return false;
        }
        
        // 如果excludeId不为空，且查询到的记录ID与excludeId相同，说明是同一条记录，不算重复
        if (excludeId != null && existing.getId().equals(excludeId)) {
            return false;
        }
        
        return true;
    }

    /**
     * Entity转VO
     * 
     * @param entity 实体对象
     * @return VO对象
     */
    private AnnouncementCategoryVO convertToVO(AnnouncementCategories entity) {
        if (entity == null) {
            return null;
        }
        
        AnnouncementCategoryVO vo = new AnnouncementCategoryVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }
}
