package com.jieni.mqxq.service.course;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.section.SectionCreateDTO;
import com.jieni.mqxq.domain.dto.section.SectionQueryDTO;
import com.jieni.mqxq.domain.dto.section.SectionUpdateDTO;
import com.jieni.mqxq.domain.vo.section.SectionStatisticsVO;
import com.jieni.mqxq.domain.vo.section.SectionVO;

import java.util.List;

/**
 * 课程小节服务接口
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface SectionService {

    /**
     * 分页查询小节列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageInfo<SectionVO> getSectionPage(SectionQueryDTO queryDTO);

    /**
     * 根据ID获取小节详情
     *
     * @param id 小节ID
     * @return 小节详情
     */
    SectionVO getSectionById(Integer id);

    /**
     * 创建小节
     *
     * @param createDTO 创建数据
     * @param teacherId 教师ID（可为空，管理员创建时不需要）
     * @return 创建的小节
     */
    SectionVO createSection(SectionCreateDTO createDTO, Integer teacherId);

    /**
     * 更新小节
     *
     * @param id 小节ID
     * @param updateDTO 更新数据
     * @param teacherId 教师ID（可为空，管理员更新时不需要）
     * @return 更新后的小节
     */
    SectionVO updateSection(Integer id, SectionUpdateDTO updateDTO, Integer teacherId);

    /**
     * 更新小节状态
     *
     * @param id 小节ID
     * @param status 状态
     * @param teacherId 教师ID（可为空，管理员更新时不需要）
     * @return 更新后的小节
     */
    SectionVO updateSectionStatus(Integer id, Integer status, Integer teacherId);

    /**
     * 删除小节
     *
     * @param id 小节ID
     * @param teacherId 教师ID（可为空，管理员删除时不需要）
     */
    void deleteSection(Integer id, Integer teacherId);

    /**
     * 批量删除小节
     *
     * @param ids 小节ID列表
     */
    void batchDeleteSections(List<Integer> ids);

    /**
     * 根据章节ID获取小节列表
     *
     * @param chapterId 章节ID
     * @param teacherId 教师ID（可为空，管理员和学生查询时不需要）
     * @param publishedOnly 是否只查询已发布
     * @return 小节列表
     */
    List<SectionVO> getSectionsByChapterId(Integer chapterId, Integer teacherId, boolean publishedOnly);

    /**
     * 根据课程ID获取小节列表
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID（可为空，管理员和学生查询时不需要）
     * @param publishedOnly 是否只查询已发布
     * @return 小节列表
     */
    List<SectionVO> getSectionsByCourseId(Integer courseId, Integer teacherId, boolean publishedOnly);

    /**
     * 获取小节统计信息
     *
     * @param courseId 课程ID（可为空，为空时统计所有）
     * @param teacherId 教师ID（可为空，管理员查询时不需要）
     * @return 统计信息
     */
    SectionStatisticsVO getSectionStatistics(Integer courseId, Integer teacherId);

    /**
     * 验证小节是否属于指定教师的课程
     *
     * @param sectionId 小节ID
     * @param teacherId 教师ID
     * @return 是否属于
     */
    boolean verifySectionOwnership(Integer sectionId, Integer teacherId);

    /**
     * 验证章节和课程的关联关系
     *
     * @param chapterId 章节ID
     * @param courseId 课程ID
     * @return 是否匹配
     */
    boolean verifyChapterCourseMat(Integer chapterId, Integer courseId);

    /**
     * 验证课程是否属于指定教师
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 是否属于
     */
    boolean verifyCourseOwnership(Integer courseId, Integer teacherId);
}
