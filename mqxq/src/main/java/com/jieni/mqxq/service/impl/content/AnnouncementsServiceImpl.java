package com.jieni.mqxq.service.impl.content;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.AnnouncementCategoriesDao;
import com.jieni.mqxq.dao.AnnouncementsDao;
import com.jieni.mqxq.domain.dto.content.*;
import com.jieni.mqxq.domain.entity.AnnouncementCategories;
import com.jieni.mqxq.domain.entity.Announcements;
import com.jieni.mqxq.domain.vo.content.AnnouncementSimpleVO;
import com.jieni.mqxq.domain.vo.content.AnnouncementVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.content.AnnouncementsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 * 
 * 提供公告管理的完整业务逻辑实现，包括公告的发布、撤回、查询等功能
 * 支持公告的分页查询、状态管理、批量操作以及统计分析
 * 实现公告的生命周期管理和搜索功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class AnnouncementsServiceImpl implements AnnouncementsService {

    @Resource
    private AnnouncementsDao announcementsDao;

    @Resource
    private AnnouncementCategoriesDao announcementCategoriesDao;

    /**
     * 创建公告
     * 
     * @param createDTO 创建DTO
     * @param createUserId 创建者ID
     * @return 公告VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO createAnnouncement(AnnouncementCreateDTO createDTO, Integer createUserId) {
        log.info("创建公告，创建者ID: {}, 公告标题: {}", createUserId, createDTO.getTitle());
        
        // 检查分类是否存在
        AnnouncementCategories category = announcementCategoriesDao.queryById(createDTO.getCategoryId());
        if (category == null) {
            throw new MyException("公告分类不存在，分类ID: " + createDTO.getCategoryId());
        }
        
        // DTO转Entity
        Announcements announcement = new Announcements();
        announcement.setCategoryId(createDTO.getCategoryId());
        announcement.setTitle(createDTO.getTitle().trim());
        announcement.setContent(createDTO.getContent());
        announcement.setPublishDate(createDTO.getPublishDate());
        
        // 设置创建和更新信息
        Date now = new Date();
        announcement.setCreateTime(now);
        announcement.setCreateUser(createUserId);
        announcement.setUpdateTime(now);
        announcement.setUpdateUser(createUserId);
        
        // 保存到数据库
        announcementsDao.insert(announcement);
        
        log.info("公告创建成功，公告ID: {}", announcement.getId());
        
        // Entity转VO
        return convertToVO(announcement, category);
    }

    /**
     * 更新公告
     * 
     * @param id 公告ID
     * @param updateDTO 更新DTO
     * @param updateUserId 更新者ID
     * @return 公告VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO updateAnnouncement(Integer id, AnnouncementUpdateDTO updateDTO, Integer updateUserId) {
        log.info("更新公告，公告ID: {}, 更新者ID: {}", id, updateUserId);
        
        // 检查公告是否存在
        Announcements existingAnnouncement = announcementsDao.queryById(id);
        if (existingAnnouncement == null) {
            throw new MyException("公告不存在，ID: " + id);
        }
        
        // 如果更新分类，检查分类是否存在
        if (updateDTO.getCategoryId() != null) {
            AnnouncementCategories category = announcementCategoriesDao.queryById(updateDTO.getCategoryId());
            if (category == null) {
                throw new MyException("公告分类不存在，分类ID: " + updateDTO.getCategoryId());
            }
            existingAnnouncement.setCategoryId(updateDTO.getCategoryId());
        }
        
        // 更新其他字段
        if (updateDTO.getTitle() != null && !updateDTO.getTitle().trim().isEmpty()) {
            existingAnnouncement.setTitle(updateDTO.getTitle().trim());
        }
        if (updateDTO.getContent() != null) {
            existingAnnouncement.setContent(updateDTO.getContent());
        }
        if (updateDTO.getPublishDate() != null) {
            existingAnnouncement.setPublishDate(updateDTO.getPublishDate());
        }
        
        // 设置更新信息
        existingAnnouncement.setUpdateTime(new Date());
        existingAnnouncement.setUpdateUser(updateUserId);
        
        // 更新数据库
        announcementsDao.update(existingAnnouncement);
        
        log.info("公告更新成功，公告ID: {}", id);
        
        // 重新查询并转换为VO
        Announcements updated = announcementsDao.queryById(id);
        AnnouncementCategories category = announcementCategoriesDao.queryById(updated.getCategoryId());
        return convertToVO(updated, category);
    }

    /**
     * 删除公告
     * 
     * @param id 公告ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Integer id) {
        log.info("删除公告，公告ID: {}", id);
        
        // 检查公告是否存在
        Announcements announcement = announcementsDao.queryById(id);
        if (announcement == null) {
            throw new MyException("公告不存在，ID: " + id);
        }
        
        // 删除公告
        int rows = announcementsDao.deleteById(id);
        if (rows <= 0) {
            throw new MyException("删除公告失败");
        }
        
        log.info("公告删除成功，公告ID: {}", id);
    }

    /**
     * 批量删除公告
     * 
     * @param ids 公告ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAnnouncements(List<Integer> ids) {
        log.info("批量删除公告，数量: {}", ids.size());
        
        // 批量删除
        int rows = announcementsDao.batchDeleteByIds(ids);
        if (rows <= 0) {
            throw new MyException("批量删除公告失败");
        }
        
        log.info("批量删除公告成功，删除数量: {}", rows);
    }

    /**
     * 根据ID获取公告详情
     * 
     * @param id 公告ID
     * @return 公告VO
     */
    @Override
    public AnnouncementVO getAnnouncementById(Integer id) {
        log.debug("查询公告详情，公告ID: {}", id);
        
        Announcements announcement = announcementsDao.queryById(id);
        if (announcement == null) {
            throw new MyException("公告不存在，ID: " + id);
        }
        
        AnnouncementCategories category = announcementCategoriesDao.queryById(announcement.getCategoryId());
        return convertToVO(announcement, category);
    }

    /**
     * 分页查询公告（管理员）
     * 
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    @Override
    public PageInfo<AnnouncementSimpleVO> getAnnouncementPage(AnnouncementPageQueryDTO queryDTO) {
        log.debug("分页查询公告，页码: {}, 每页大小: {}", queryDTO.getPage(), queryDTO.getSize());
        
        // 构建查询条件
        Announcements queryCondition = new Announcements();
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            queryCondition.setTitle(queryDTO.getTitle().trim());
        }
        if (queryDTO.getCategoryId() != null) {
            queryCondition.setCategoryId(queryDTO.getCategoryId());
        }
        
        // 开启分页
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        List<Announcements> list = announcementsDao.queryAllByLimit(queryCondition);
        PageInfo<Announcements> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<AnnouncementSimpleVO> voList = list.stream()
                .map(this::convertToSimpleVO)
                .collect(Collectors.toList());
        
        // 构造返回的PageInfo
        PageInfo<AnnouncementSimpleVO> resultPageInfo = new PageInfo<>(voList);
        BeanUtil.copyProperties(pageInfo, resultPageInfo);
        
        return resultPageInfo;
    }

    /**
     * 发布公告
     * 
     * @param id 公告ID
     * @param publishDTO 发布DTO
     * @return 公告VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO publishAnnouncement(Integer id, AnnouncementPublishDTO publishDTO) {
        log.info("发布公告，公告ID: {}", id);
        
        // 检查公告是否存在
        Announcements announcement = announcementsDao.queryById(id);
        if (announcement == null) {
            throw new MyException("公告不存在，ID: " + id);
        }
        
        // 如果已发布，不允许重复发布
        if (announcement.getPublishDate() != null) {
            throw new MyException("公告已发布，不能重复发布");
        }
        
        // 设置发布时间
        Date publishDate = publishDTO.getPublishDate() != null ? publishDTO.getPublishDate() : new Date();
        announcementsDao.publishAnnouncement(id, publishDate);
        
        log.info("公告发布成功，公告ID: {}", id);
        
        // 重新查询并转换为VO
        Announcements updated = announcementsDao.queryById(id);
        AnnouncementCategories category = announcementCategoriesDao.queryById(updated.getCategoryId());
        return convertToVO(updated, category);
    }

    /**
     * 撤回公告
     * 
     * @param id 公告ID
     * @return 公告VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO withdrawAnnouncement(Integer id) {
        log.info("撤回公告，公告ID: {}", id);
        
        // 检查公告是否存在
        Announcements announcement = announcementsDao.queryById(id);
        if (announcement == null) {
            throw new MyException("公告不存在，ID: " + id);
        }
        
        // 如果未发布，不允许撤回
        if (announcement.getPublishDate() == null) {
            throw new MyException("公告未发布，无需撤回");
        }
        
        // 撤回公告
        announcementsDao.withdrawAnnouncement(id);
        
        log.info("公告撤回成功，公告ID: {}", id);
        
        // 重新查询并转换为VO
        Announcements updated = announcementsDao.queryById(id);
        AnnouncementCategories category = announcementCategoriesDao.queryById(updated.getCategoryId());
        return convertToVO(updated, category);
    }

    /**
     * 根据ID获取已发布的公告详情（公共接口）
     * 
     * @param id 公告ID
     * @return 公告VO
     */
    @Override
    public AnnouncementVO getPublishedAnnouncementById(Integer id) {
        log.debug("查询已发布公告详情，公告ID: {}", id);
        
        Announcements announcement = announcementsDao.queryPublishedById(id);
        if (announcement == null) {
            throw new MyException("公告不存在或未发布，ID: " + id);
        }
        
        AnnouncementCategories category = announcementCategoriesDao.queryById(announcement.getCategoryId());
        return convertToVO(announcement, category);
    }

    /**
     * 分页查询已发布的公告（公共接口）
     * 
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    @Override
    public PageInfo<AnnouncementSimpleVO> getPublishedAnnouncementPage(AnnouncementPageQueryDTO queryDTO) {
        log.debug("分页查询已发布公告，页码: {}, 每页大小: {}", queryDTO.getPage(), queryDTO.getSize());
        
        // 构建查询条件
        Announcements queryCondition = new Announcements();
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            queryCondition.setTitle(queryDTO.getTitle().trim());
        }
        if (queryDTO.getCategoryId() != null) {
            queryCondition.setCategoryId(queryDTO.getCategoryId());
        }
        
        // 开启分页
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        List<Announcements> list = announcementsDao.queryPublishedByLimit(queryCondition);
        PageInfo<Announcements> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<AnnouncementSimpleVO> voList = list.stream()
                .map(this::convertToSimpleVO)
                .collect(Collectors.toList());
        
        // 构造返回的PageInfo
        PageInfo<AnnouncementSimpleVO> resultPageInfo = new PageInfo<>(voList);
        BeanUtil.copyProperties(pageInfo, resultPageInfo);
        
        return resultPageInfo;
    }

    /**
     * 获取最新公告列表
     * 
     * @param count 获取数量
     * @return 最新公告列表
     */
    @Override
    public List<AnnouncementSimpleVO> getLatestAnnouncements(Integer count) {
        log.debug("获取最新公告列表，数量: {}", count);
        
        // 参数验证
        if (count == null || count <= 0 || count > 20) {
            throw new MyException("获取数量必须在1-20之间");
        }
        
        List<Announcements> list = announcementsDao.getLatestAnnouncements(count);
        return list.stream()
                .map(this::convertToSimpleVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据分类获取已发布的公告
     * 
     * @param categoryId 分类ID
     * @return 公告列表
     */
    @Override
    public List<AnnouncementSimpleVO> getPublishedAnnouncementsByCategory(Integer categoryId) {
        log.debug("根据分类获取已发布公告，分类ID: {}", categoryId);
        
        List<Announcements> list = announcementsDao.getPublishedByCategory(categoryId);
        return list.stream()
                .map(this::convertToSimpleVO)
                .collect(Collectors.toList());
    }

    /**
     * 搜索已发布的公告
     * 
     * @param searchDTO 搜索DTO
     * @return 搜索结果
     */
    @Override
    public PageInfo<AnnouncementSimpleVO> searchPublishedAnnouncements(AnnouncementSearchDTO searchDTO) {
        log.debug("搜索已发布公告，关键词: {}, 页码: {}, 每页大小: {}", 
                  searchDTO.getKeyword(), searchDTO.getPage(), searchDTO.getSize());
        
        // 开启分页
        PageHelper.startPage(searchDTO.getPage(), searchDTO.getSize());
        List<Announcements> list = announcementsDao.searchPublishedAnnouncements(searchDTO.getKeyword().trim());
        PageInfo<Announcements> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<AnnouncementSimpleVO> voList = list.stream()
                .map(this::convertToSimpleVO)
                .collect(Collectors.toList());
        
        // 构造返回的PageInfo
        PageInfo<AnnouncementSimpleVO> resultPageInfo = new PageInfo<>(voList);
        BeanUtil.copyProperties(pageInfo, resultPageInfo);
        
        return resultPageInfo;
    }

    /**
     * Entity转VO（完整信息）
     * 
     * @param entity 实体对象
     * @param category 分类对象
     * @return VO对象
     */
    private AnnouncementVO convertToVO(Announcements entity, AnnouncementCategories category) {
        if (entity == null) {
            return null;
        }
        
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtil.copyProperties(entity, vo);
        
        // 设置分类名称
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        // 设置是否已发布
        vo.setIsPublished(entity.getPublishDate() != null);
        
        return vo;
    }

    /**
     * Entity转SimpleVO（简化信息）
     * 
     * @param entity 实体对象
     * @return SimpleVO对象
     */
    private AnnouncementSimpleVO convertToSimpleVO(Announcements entity) {
        if (entity == null) {
            return null;
        }
        
        AnnouncementSimpleVO vo = new AnnouncementSimpleVO();
        vo.setId(entity.getId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setTitle(entity.getTitle());
        vo.setPublishDate(entity.getPublishDate());
        vo.setIsPublished(entity.getPublishDate() != null);
        vo.setCreateTime(entity.getCreateTime());
        
        // 查询分类名称
        AnnouncementCategories category = announcementCategoriesDao.queryById(entity.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        return vo;
    }
}
