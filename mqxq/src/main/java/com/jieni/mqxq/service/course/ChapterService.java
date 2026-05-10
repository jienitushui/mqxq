package com.jieni.mqxq.service.course;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.chapter.ChapterCreateDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterQueryDTO;
import com.jieni.mqxq.domain.dto.chapter.ChapterUpdateDTO;
import com.jieni.mqxq.domain.entity.Chapter;
import com.jieni.mqxq.domain.vo.chapter.ChapterStatisticsVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterStructureVO;
import com.jieni.mqxq.domain.vo.chapter.ChapterVO;

import java.util.List;

/**
 * 课程章节服务接口
 * 
 * 提供课程章节管理的完整服务功能，包括章节的CRUD操作、分页查询、条件筛选、数量统计等。
 * 支持课程章节的层级管理、排序显示、状态控制等功能，为课程内容组织和学习路径规划提供数据支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface ChapterService {

    /**
     * 根据ID获取章节详情
     *
     * @param id 章节ID
     * @return 章节视图对象
     */
    ChapterVO getChapterDetail(Integer id);

    /**
     * 分页查询章节列表
     *
     * @param queryDTO 查询条件
     * @return 章节分页信息
     */
    PageInfo<ChapterVO> getChapterPage(ChapterQueryDTO queryDTO);

    /**
     * 创建章节
     *
     * @param createDTO 创建DTO
     * @param userId 创建用户ID
     * @return 章节视图对象
     */
    ChapterVO createChapter(ChapterCreateDTO createDTO, Integer userId);

    /**
     * 更新章节
     *
     * @param id 章节ID
     * @param updateDTO 更新DTO
     * @param userId 更新用户ID
     * @return 章节视图对象
     */
    ChapterVO updateChapter(Integer id, ChapterUpdateDTO updateDTO, Integer userId);

    /**
     * 删除章节
     *
     * @param id 章节ID
     */
    void deleteChapter(Integer id);

    /**
     * 批量删除章节（级联删除关联小节）
     *
     * @param ids 章节ID列表
     * @return 成功删除的数量
     */
    Integer batchDeleteChapters(List<Integer> ids);

    /**
     * 获取课程的所有章节
     *
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<ChapterVO> getCourseChapters(Integer courseId);

    /**
     * 获取课程章节结构（包含小节）
     *
     * @param courseId 课程ID
     * @return 章节结构列表
     */
    List<ChapterStructureVO> getCourseStructure(Integer courseId);

    /**
     * 获取章节统计信息
     *
     * @param courseId 课程ID，为null时统计全部
     * @return 统计信息
     */
    ChapterStatisticsVO getChapterStatistics(Integer courseId);

    /**
     * 验证章节是否属于指定教师的课程
     *
     * @param chapterId 章节ID
     * @param teacherId 教师ID
     * @return 是否属于该教师
     */
    boolean verifyChapterOwnership(Integer chapterId, Integer teacherId);

    /**
     * 验证课程是否属于指定教师
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 是否属于该教师
     */
    boolean verifyCourseOwnership(Integer courseId, Integer teacherId);
    
    /**
     * 根据ID查询章节实体（内部使用）
     *
     * @param id 章节ID
     * @return 章节实体
     */
    Chapter queryById(Integer id);

    /**
     * 查询所有章节（内部使用）
     *
     * @param chapter 筛选条件
     * @return 章节列表
     */
    List<Chapter> queryAll(Chapter chapter);
}
