package com.jieni.mqxq.service.impl.course;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.SectionDao;
import com.jieni.mqxq.domain.dto.section.SectionCreateDTO;
import com.jieni.mqxq.domain.dto.section.SectionQueryDTO;
import com.jieni.mqxq.domain.dto.section.SectionUpdateDTO;
import com.jieni.mqxq.domain.entity.Chapter;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.entity.Section;
import com.jieni.mqxq.domain.vo.section.SectionStatisticsVO;
import com.jieni.mqxq.domain.vo.section.SectionVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.ChapterService;
import com.jieni.mqxq.service.course.CourseService;
import com.jieni.mqxq.service.course.SectionService;
import com.jieni.mqxq.service.course.MyCourseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程小节服务实现类
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class SectionServiceImpl implements SectionService {

    @Resource
    private SectionDao sectionDao;

    @Lazy
    @Resource
    private ChapterService chapterService;

    @Resource
    private CourseService courseService;

    @Resource
    private MyCourseService myCourseService;

    @Override
    public PageInfo<SectionVO> getSectionPage(SectionQueryDTO queryDTO) {
        log.debug("分页查询小节列表, 查询条件: {}", queryDTO);
        
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        Section condition = BeanUtil.copyProperties(queryDTO, Section.class);
        List<Section> sections = sectionDao.queryAllByLimit(condition);
        PageInfo<Section> pageInfo = new PageInfo<>(sections);
        
        // 转换为VO
        List<SectionVO> sectionVOs = sections.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        PageInfo<SectionVO> result = new PageInfo<>(sectionVOs);
        result.setTotal(pageInfo.getTotal());
        return result;
    }

    @Override
    public SectionVO getSectionById(Integer id) {
        if (id == null || id <= 0) {
            throw new MyException("小节ID不能为空");
        }
        
        Section section = sectionDao.queryById(id);
        if (section == null) {
            throw new MyException("小节不存在");
        }
        
        return convertToVO(section);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SectionVO createSection(SectionCreateDTO createDTO, Integer teacherId) {
        log.info("创建小节, 创建数据: {}, 教师ID: {}", createDTO, teacherId);
        
        // 验证章节和课程的关联关系
        if (!verifyChapterCourseMat(createDTO.getChapterId(), createDTO.getCourseId())) {
            throw new MyException("章节与课程不匹配");
        }
        
        // 如果是教师创建，验证课程归属
        if (teacherId != null && !verifyCourseOwnership(createDTO.getCourseId(), teacherId)) {
            throw new MyException("无权为此课程添加小节");
        }
        
        Section section = BeanUtil.copyProperties(createDTO, Section.class);
        Date now = new Date();
        section.setCreateTime(now);
        section.setUpdateTime(now);
        if (teacherId != null) {
            section.setCreateUser(teacherId);
            section.setUpdateUser(teacherId);
        }
        
        // 默认状态为未发布
        if (section.getStatus() == null) {
            section.setStatus(0);
        }
        
        sectionDao.insert(section);
        log.info("小节创建成功, ID: {}", section.getId());

        // 新增小节后重置已完成学员的学习状态为“学习中”
        tryResetLearningStatus(createDTO.getCourseId(), "新增小节: " + createDTO.getTitle());
        
        return convertToVO(section);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SectionVO updateSection(Integer id, SectionUpdateDTO updateDTO, Integer teacherId) {
        log.info("更新小节, ID: {}, 更新数据: {}, 教师ID: {}", id, updateDTO, teacherId);
        
        if (id == null || id <= 0) {
            throw new MyException("小节ID不能为空");
        }
        
        Section existing = sectionDao.queryById(id);
        if (existing == null) {
            throw new MyException("小节不存在");
        }
        
        // 如果是教师更新，验证小节归属
        if (teacherId != null && !verifySectionOwnership(id, teacherId)) {
            throw new MyException("无权修改此小节");
        }
        
        // 如果要更新章节ID或课程ID，需要验证关联关系
        Integer newChapterId = updateDTO.getChapterId() != null ? updateDTO.getChapterId() : existing.getChapterId();
        Integer newCourseId = updateDTO.getCourseId() != null ? updateDTO.getCourseId() : existing.getCourseId();
        
        if (!verifyChapterCourseMat(newChapterId, newCourseId)) {
            throw new MyException("章节与课程不匹配");
        }
        
        Section section = BeanUtil.copyProperties(updateDTO, Section.class);
        section.setId(id);
        section.setUpdateTime(new Date());
        if (teacherId != null) {
            section.setUpdateUser(teacherId);
        }
        
        sectionDao.update(section);
        log.info("小节更新成功, ID: {}", id);
        
        return getSectionById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SectionVO updateSectionStatus(Integer id, Integer status, Integer teacherId) {
        log.info("更新小节状态, ID: {}, 状态: {}, 教师ID: {}", id, status, teacherId);
        
        if (id == null || id <= 0) {
            throw new MyException("小节ID不能为空");
        }
        
        Section existing = sectionDao.queryById(id);
        if (existing == null) {
            throw new MyException("小节不存在");
        }
        
        // 如果是教师更新，验证小节归属
        if (teacherId != null && !verifySectionOwnership(id, teacherId)) {
            throw new MyException("无权操作此小节");
        }
        
        Section section = new Section();
        section.setId(id);
        section.setStatus(status);
        section.setUpdateTime(new Date());
        if (teacherId != null) {
            section.setUpdateUser(teacherId);
        }
        
        sectionDao.update(section);
        log.info("小节状态更新成功, ID: {}, 新状态: {}", id, status);
        
        return getSectionById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSection(Integer id, Integer teacherId) {
        log.info("删除小节, ID: {}, 教师ID: {}", id, teacherId);
        
        if (id == null || id <= 0) {
            throw new MyException("小节ID不能为空");
        }
        
        Section existing = sectionDao.queryById(id);
        if (existing == null) {
            throw new MyException("小节不存在");
        }
        
        // 如果是教师删除，验证小节归属
        if (teacherId != null && !verifySectionOwnership(id, teacherId)) {
            throw new MyException("无权删除此小节");
        }
        
        int rows = sectionDao.deleteById(id);
        if (rows == 0) {
            throw new MyException("删除小节失败");
        }
        
        log.info("小节删除成功, ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteSections(List<Integer> ids) {
        log.info("批量删除小节, IDs: {}", ids);
        
        if (ids == null || ids.isEmpty()) {
            throw new MyException("请选择要删除的小节");
        }
        
        int successCount = 0;
        for (Integer id : ids) {
            try {
                sectionDao.deleteById(id);
                successCount++;
            } catch (Exception e) {
                log.warn("删除小节失败, ID: {}, 错误: {}", id, e.getMessage());
            }
        }
        
        log.info("批量删除完成, 成功: {}, 总数: {}", successCount, ids.size());
    }

    @Override
    public List<SectionVO> getSectionsByChapterId(Integer chapterId, Integer teacherId, boolean publishedOnly) {
        log.debug("获取章节小节列表, 章节ID: {}, 教师ID: {}, 只查询已发布: {}", chapterId, teacherId, publishedOnly);
        
        if (chapterId == null || chapterId <= 0) {
            throw new MyException("章节ID不能为空");
        }
        
        // 如果是教师查询，验证章节归属
        if (teacherId != null) {
            Chapter chapter = chapterService.queryById(chapterId);
            if (chapter == null) {
                throw new MyException("章节不存在");
            }
            if (!verifyCourseOwnership(chapter.getCourseId(), teacherId)) {
                throw new MyException("无权访问该章节的小节");
            }
        }
        
        Section condition = new Section();
        condition.setChapterId(chapterId);
        if (publishedOnly) {
            condition.setStatus(1);
        }
        
        List<Section> sections = sectionDao.queryAllByLimit(condition);
        return sections.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SectionVO> getSectionsByCourseId(Integer courseId, Integer teacherId, boolean publishedOnly) {
        log.debug("获取课程小节列表, 课程ID: {}, 教师ID: {}, 只查询已发布: {}", courseId, teacherId, publishedOnly);
        
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID不能为空");
        }
        
        // 如果是教师查询，验证课程归属
        if (teacherId != null && !verifyCourseOwnership(courseId, teacherId)) {
            throw new MyException("无权访问该课程的小节");
        }
        
        Section condition = new Section();
        condition.setCourseId(courseId);
        if (publishedOnly) {
            condition.setStatus(1);
        }
        
        List<Section> sections = sectionDao.queryAllByLimit(condition);
        return sections.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public SectionStatisticsVO getSectionStatistics(Integer courseId, Integer teacherId) {
        log.debug("获取小节统计, 课程ID: {}, 教师ID: {}", courseId, teacherId);
        
        // 如果是教师查询，验证课程归属
        if (courseId != null && teacherId != null && !verifyCourseOwnership(courseId, teacherId)) {
            throw new MyException("无权访问该课程的统计信息");
        }
        
        Section condition = new Section();
        if (courseId != null) {
            condition.setCourseId(courseId);
        }
        
        List<Section> sections = sectionDao.queryAllByLimit(condition);
        
        long totalSections = sections.size();
        long publishedSections = sections.stream().filter(s -> s.getStatus() != null && s.getStatus() == 1).count();
        long unpublishedSections = totalSections - publishedSections;
        long sectionsWithVideo = sections.stream().filter(s -> s.getVideoUrl() != null && !s.getVideoUrl().trim().isEmpty()).count();
        long sectionsWithoutVideo = totalSections - sectionsWithVideo;
        long totalDuration = sections.stream()
                .filter(s -> s.getDuration() != null)
                .mapToLong(Section::getDuration)
                .sum();
        long averageDuration = totalSections > 0 ? totalDuration / totalSections : 0;
        
        return new SectionStatisticsVO(
                totalSections,
                publishedSections,
                unpublishedSections,
                sectionsWithVideo,
                sectionsWithoutVideo,
                totalDuration,
                averageDuration
        );
    }

    @Override
    public boolean verifySectionOwnership(Integer sectionId, Integer teacherId) {
        if (sectionId == null || teacherId == null) {
            return false;
        }
        
        Section section = sectionDao.queryById(sectionId);
        if (section == null) {
            return false;
        }
        
        return verifyCourseOwnership(section.getCourseId(), teacherId);
    }

    @Override
    public boolean verifyChapterCourseMat(Integer chapterId, Integer courseId) {
        if (chapterId == null || courseId == null) {
            return false;
        }
        
        Chapter chapter = chapterService.queryById(chapterId);
        return chapter != null && chapter.getCourseId().equals(courseId);
    }

    @Override
    public boolean verifyCourseOwnership(Integer courseId, Integer teacherId) {
        if (courseId == null || teacherId == null) {
            return false;
        }
        
        Course course = courseService.queryById(courseId);
        return course != null && course.getTeacherId().equals(teacherId);
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

    /**
     * 将Section实体转换为VO
     */
    private SectionVO convertToVO(Section section) {
        return BeanUtil.copyProperties(section, SectionVO.class);
    }
}
