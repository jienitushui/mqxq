package com.jieni.mqxq.service.impl.course;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.ChapterDao;
import com.jieni.mqxq.domain.dto.chapter.ChapterCreateDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterQueryDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterUpdateDTO;
import com.jieni.mqxq.domain.entity.Chapter;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.vo.section.SectionVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterStatisticsVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterStructureVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.ChapterService;
import com.jieni.mqxq.service.course.CourseService;
import com.jieni.mqxq.service.course.SectionService;
import com.jieni.mqxq.service.course.MyCourseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程章节服务实现类
 * 
 * 提供课程章节管理的完整业务服务，包括章节的增删改查、分页查询等
 * 支持章节的层次管理、排序管理以及与课程的关联管理
 * 实现章节数据统计和条件查询功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class ChapterServiceImpl implements ChapterService {
    
    @Resource
    private ChapterDao chapterDao;
    
    @Resource
    private SectionService sectionService;
    
    @Resource
    private CourseService courseService;

    @Resource
    private MyCourseService myCourseService;

    /**
     * 根据ID获取章节详情
     * 
     * @param id 章节ID
     * @return 章节视图对象
     */
    @Override
    public ChapterVO getChapterDetail(Integer id) {
        if (id == null || id <= 0) {
            throw new MyException("章节ID无效");
        }
        
        Chapter chapter = chapterDao.queryById(id);
        if (chapter == null) {
            throw new MyException("章节不存在");
        }
        
        return convertToVO(chapter);
    }

    /**
     * 分页查询章节列表
     * 
     * @param queryDTO 查询条件
     * @return 章节分页信息
     */
    @Override
    public PageInfo<ChapterVO> getChapterPage(ChapterQueryDTO queryDTO) {
        // 构建查询条件
        Chapter condition = new Chapter();
        if (queryDTO.getCourseId() != null) {
            condition.setCourseId(queryDTO.getCourseId());
        }
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            condition.setTitle(queryDTO.getTitle().trim());
        }
        
        // 分页查询
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        List<Chapter> chapters = chapterDao.queryAllByLimit(condition);
        PageInfo<Chapter> pageInfo = new PageInfo<>(chapters);
        
        // 转换为VO
        List<ChapterVO> chapterVOs = chapters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        PageInfo<ChapterVO> result = new PageInfo<>(chapterVOs);
        result.setTotal(pageInfo.getTotal());
        return result;
    }

    /**
     * 创建章节
     * 
     * @param createDTO 创建DTO
     * @param userId 创建用户ID
     * @return 章节视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterVO createChapter(ChapterCreateDTO createDTO, Integer userId) {
        // 验证课程是否存在
        Course course = courseService.queryById(createDTO.getCourseId());
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 构建章节实体
        Chapter chapter = new Chapter();
        chapter.setCourseId(createDTO.getCourseId());
        chapter.setTitle(createDTO.getTitle());
        
        Date now = new Date();
        chapter.setCreateTime(now);
        chapter.setCreateUser(userId);
        chapter.setUpdateTime(now);
        chapter.setUpdateUser(userId);
        
        // 保存章节
        chapterDao.insert(chapter);
        
        log.info("创建章节成功, chapterId: {}, title: {}, userId: {}", 
                chapter.getId(), chapter.getTitle(), userId);

        // 新增章节后重置已完成学员的学习状态为“学习中”
        tryResetLearningStatus(createDTO.getCourseId(), "新增章节: " + createDTO.getTitle());
        
        return convertToVO(chapter);
    }

    /**
     * 更新章节
     * 
     * @param id 章节ID
     * @param updateDTO 更新DTO
     * @param userId 更新用户ID
     * @return 章节视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChapterVO updateChapter(Integer id, ChapterUpdateDTO updateDTO, Integer userId) {
        // 验证章节是否存在
        Chapter existing = chapterDao.queryById(id);
        if (existing == null) {
            throw new MyException("章节不存在");
        }
        
        // 如果更新了课程ID，验证新课程是否存在
        if (updateDTO.getCourseId() != null && !updateDTO.getCourseId().equals(existing.getCourseId())) {
            Course course = courseService.queryById(updateDTO.getCourseId());
            if (course == null) {
                throw new MyException("目标课程不存在");
            }
        }
        
        // 构建更新实体
        Chapter chapter = new Chapter();
        chapter.setId(id);
        
        if (updateDTO.getTitle() != null && !updateDTO.getTitle().trim().isEmpty()) {
            chapter.setTitle(updateDTO.getTitle().trim());
        }
        if (updateDTO.getCourseId() != null) {
            chapter.setCourseId(updateDTO.getCourseId());
        }
        
        chapter.setUpdateTime(new Date());
        chapter.setUpdateUser(userId);
        
        // 更新章节
        chapterDao.update(chapter);
        
        log.info("更新章节成功, chapterId: {}, userId: {}", id, userId);
        
        return getChapterDetail(id);
    }

    /**
     * 删除章节
     * 
     * @param id 章节ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Integer id) {
        // 验证章节是否存在
        Chapter chapter = chapterDao.queryById(id);
        if (chapter == null) {
            throw new MyException("章节不存在");
        }
        
        // 删除关联的小节
        List<SectionVO> sections = sectionService.getSectionsByChapterId(id, null, false);
        for (SectionVO section : sections) {
            sectionService.deleteSection(section.getId(), null);
        }
        
        // 删除章节
        int rows = chapterDao.deleteById(id);
        if (rows == 0) {
            throw new MyException("删除章节失败");
        }
        
        log.info("删除章节成功, chapterId: {}, 级联删除小节数: {}", id, sections.size());
    }

    /**
     * 批量删除章节（级联删除关联小节）
     * 
     * @param ids 章节ID列表
     * @return 成功删除的数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchDeleteChapters(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new MyException("请选择要删除的章节");
        }
        
        int successCount = 0;
        for (Integer id : ids) {
            try {
                deleteChapter(id);
                successCount++;
            } catch (Exception e) {
                log.warn("删除章节失败, chapterId: {}, error: {}", id, e.getMessage());
            }
        }
        
        log.info("批量删除章节完成, 总数: {}, 成功: {}", ids.size(), successCount);
        return successCount;
    }

    /**
     * 获取课程的所有章节
     * 
     * @param courseId 课程ID
     * @return 章节列表
     */
    @Override
    public List<ChapterVO> getCourseChapters(Integer courseId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        
        Chapter condition = new Chapter();
        condition.setCourseId(courseId);
        
        List<Chapter> chapters = chapterDao.queryAllByLimit(condition);
        
        return chapters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取课程章节结构（包含小节）
     * 
     * @param courseId 课程ID
     * @return 章节结构列表
     */
    @Override
    public List<ChapterStructureVO> getCourseStructure(Integer courseId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        
        // 获取所有章节
        Chapter chapterCondition = new Chapter();
        chapterCondition.setCourseId(courseId);
        List<Chapter> chapters = chapterDao.queryAllByLimit(chapterCondition);
        
        // 获取所有已发布小节
        List<SectionVO> sections = sectionService.getSectionsByCourseId(courseId, null, true);
        
        // 按章节ID分组小节
        Map<Integer, List<SectionVO>> sectionsByChapter = sections.stream()
                .collect(Collectors.groupingBy(SectionVO::getChapterId));
        
        // 构建结构
        return chapters.stream()
                .map(chapter -> convertToStructureVO(chapter, sectionsByChapter.getOrDefault(chapter.getId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    /**
     * 获取章节统计信息
     * 
     * @param courseId 课程ID，为null时统计全部
     * @return 统计信息
     */
    @Override
    public ChapterStatisticsVO getChapterStatistics(Integer courseId) {
        ChapterStatisticsVO statistics = new ChapterStatisticsVO();
        statistics.setCourseId(courseId);
        
        // 获取章节列表
        Chapter chapterCondition = new Chapter();
        if (courseId != null) {
            chapterCondition.setCourseId(courseId);
        }
        List<Chapter> chapters = chapterDao.queryAllByLimit(chapterCondition);
        statistics.setTotalChapters(chapters.size());
        
        // 获取小节统计
        List<SectionVO> allSections = courseId != null 
                ? sectionService.getSectionsByCourseId(courseId, null, false)
                : new ArrayList<>();
        
        statistics.setTotalSections(allSections.size());
        
        // 统计已发布和未发布小节
        int publishedCount = 0;
        long totalDuration = 0;
        long videoCount = 0;
        
        for (SectionVO section : allSections) {
            if (section.getStatus() != null && section.getStatus() == 1) {
                publishedCount++;
            }
            if (section.getDuration() != null) {
                totalDuration += section.getDuration();
            }
            if (section.getVideoUrl() != null && !section.getVideoUrl().trim().isEmpty()) {
                videoCount++;
            }
        }
        
        statistics.setPublishedSections(publishedCount);
        statistics.setUnpublishedSections(allSections.size() - publishedCount);
        statistics.setTotalDuration(totalDuration);
        statistics.setSectionsWithVideo(videoCount);
        statistics.setSectionsWithoutVideo((long) allSections.size() - videoCount);
        
        return statistics;
    }

    /**
     * 验证章节是否属于指定教师的课程
     * 
     * @param chapterId 章节ID
     * @param teacherId 教师ID
     * @return 是否属于该教师
     */
    @Override
    public boolean verifyChapterOwnership(Integer chapterId, Integer teacherId) {
        if (chapterId == null || teacherId == null) {
            return false;
        }
        
        Chapter chapter = chapterDao.queryById(chapterId);
        if (chapter == null) {
            return false;
        }
        
        return verifyCourseOwnership(chapter.getCourseId(), teacherId);
    }

    /**
     * 验证课程是否属于指定教师
     * 
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 是否属于该教师
     */
    @Override
    public boolean verifyCourseOwnership(Integer courseId, Integer teacherId) {
        if (courseId == null || teacherId == null) {
            return false;
        }
        
        Course course = courseService.queryById(courseId);
        if (course == null) {
            return false;
        }
        
        return course.getTeacherId().equals(teacherId);
    }

    /**
     * 根据ID查询章节实体（内部使用）
     * 
     * @param id 章节ID
     * @return 章节实体
     */
    @Override
    public Chapter queryById(Integer id) {
        if (id == null || id <= 0) {
            throw new MyException("章节ID无效");
        }
        return chapterDao.queryById(id);
    }

    /**
     * 查询所有章节（内部使用）
     * 
     * @param chapter 筛选条件
     * @return 章节列表
     */
    @Override
    public List<Chapter> queryAll(Chapter chapter) {
        return chapterDao.queryAllByLimit(chapter);
    }

    /**
     * 新增课程内容后尝试重置学习状态
     */
    private void tryResetLearningStatus(Integer courseId, String reason) {
        try {
            myCourseService.resetCompletedStatusToLearning(courseId, reason);
        } catch (Exception ex) {
            log.warn("重置学习状态失败, courseId: {}, reason: {}, error: {}", courseId, reason, ex.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 将章节实体转换为VO
     */
    private ChapterVO convertToVO(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        
        ChapterVO vo = new ChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }

    /**
     * 将章节实体转换为结构VO
     */
    private ChapterStructureVO convertToStructureVO(Chapter chapter, List<SectionVO> sections) {
        ChapterStructureVO vo = new ChapterStructureVO();
        vo.setId(chapter.getId());
        vo.setTitle(chapter.getTitle());
        vo.setCreateTime(chapter.getCreateTime());
        
        List<ChapterStructureVO.SectionInfo> sectionInfos = sections.stream()
                .map(this::convertToSectionInfo)
                .collect(Collectors.toList());
        vo.setSections(sectionInfos);
        
        return vo;
    }

    /**
     * 将小节VO转换为SectionInfo
     */
    private ChapterStructureVO.SectionInfo convertToSectionInfo(SectionVO section) {
        ChapterStructureVO.SectionInfo info = new ChapterStructureVO.SectionInfo();
        info.setId(section.getId());
        info.setTitle(section.getTitle());
        info.setDuration(section.getDuration());
        info.setVideoUrl(section.getVideoUrl());
        info.setContent(section.getContent());
        info.setStatus(section.getStatus());
        return info;
    }
}
