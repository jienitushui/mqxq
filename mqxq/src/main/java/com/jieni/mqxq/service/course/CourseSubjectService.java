package com.jieni.mqxq.service.course;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.course.CreateCourseSubjectDTO;
import com.jieni.mqxq.domain.dto.course.UpdateCourseSubjectDTO;
import com.jieni.mqxq.domain.entity.CourseSubject;
import com.jieni.mqxq.domain.vo.course.CourseSubjectTreeVO;
import com.jieni.mqxq.domain.vo.course.CourseSubjectVO;

import java.util.List;

/**
 * 课程分类服务接口
 * 
 * 提供课程分类管理的完整业务功能，包括分类的CRUD操作、分页查询、
 * 树形结构查询、名称搜索等功能
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseSubjectService {

    /**
     * 根据ID获取课程分类详情
     *
     * @param id 分类ID
     * @return 课程分类VO对象
     */
    CourseSubjectVO getCourseSubjectById(Integer id);

    /**
     * 分页查询课程分类
     *
     * @param name 分类名称（支持模糊查询）
     * @param parentId 父分类ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<CourseSubjectVO> pageCourseSubjects(String name, Integer parentId, Integer pageNum, Integer pageSize);

    /**
     * 获取所有课程分类列表
     *
     * @return 课程分类列表
     */
    List<CourseSubjectVO> listAllCourseSubjects();

    /**
     * 根据父分类ID获取子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CourseSubjectVO> listChildrenByParentId(Integer parentId);

    /**
     * 获取树形结构的课程分类
     *
     * @return 树形结构的课程分类列表
     */
    List<CourseSubjectTreeVO> getCourseSubjectTree();

    /**
     * 根据名称搜索课程分类
     *
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    List<CourseSubjectVO> searchCourseSubjectsByName(String keyword);

    /**
     * 创建课程分类
     *
     * @param createDTO 创建DTO
     * @param currentUserId 当前用户ID
     * @return 创建成功的课程分类VO
     */
    CourseSubjectVO createCourseSubject(CreateCourseSubjectDTO createDTO, Integer currentUserId);

    /**
     * 更新课程分类
     *
     * @param id 分类ID
     * @param updateDTO 更新DTO
     * @param currentUserId 当前用户ID
     * @return 更新成功的课程分类VO
     */
    CourseSubjectVO updateCourseSubject(Integer id, UpdateCourseSubjectDTO updateDTO, Integer currentUserId);

    /**
     * 删除课程分类
     *
     * @param id 分类ID
     */
    void deleteCourseSubject(Integer id);

    /**
     * 检查分类名称是否已存在
     *
     * @param name 分类名称
     * @param excludeId 排除的分类ID（用于更新时排除自己）
     * @return 是否存在
     */
    boolean isCourseSubjectNameExists(String name, Integer excludeId);

    /**
     * 检查分类是否有子分类
     *
     * @param id 分类ID
     * @return 是否有子分类
     */
    boolean hasChildren(Integer id);

    // ==================== 内部辅助方法 ====================

    /**
     * 根据ID查询课程分类（内部使用，返回Entity）
     *
     * @param id 分类ID
     * @return 课程分类实体
     */
    CourseSubject queryById(Integer id);

    /**
     * 根据条件查询课程分类列表（内部使用，返回Entity列表）
     *
     * @param condition 查询条件
     * @return 课程分类实体列表
     */
    List<CourseSubject> queryAllByLimit(CourseSubject condition);
}
