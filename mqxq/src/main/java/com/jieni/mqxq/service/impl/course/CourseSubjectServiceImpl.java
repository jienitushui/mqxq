package com.jieni.mqxq.service.impl.course;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CourseSubjectDao;
import com.jieni.mqxq.domain.dto.course.CreateCourseSubjectDTO;
import com.jieni.mqxq.domain.dto.course.UpdateCourseSubjectDTO;
import com.jieni.mqxq.domain.entity.CourseSubject;
import com.jieni.mqxq.domain.vo.course.CourseSubjectTreeVO;
import com.jieni.mqxq.domain.vo.course.CourseSubjectVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.CourseSubjectService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程分类服务实现类
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CourseSubjectServiceImpl implements CourseSubjectService {
    
    @Resource
    private CourseSubjectDao courseSubjectDao;

    @Override
    public CourseSubjectVO getCourseSubjectById(Integer id) {
        if (id == null || id <= 0) {
            throw new MyException("课程分类ID无效");
        }
        
        CourseSubject courseSubject = courseSubjectDao.queryById(id);
        if (courseSubject == null) {
            throw new MyException("课程分类不存在");
        }
        
        return convertToVO(courseSubject);
    }

    @Override
    public PageInfo<CourseSubjectVO> pageCourseSubjects(String name, Integer parentId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        
        CourseSubject condition = new CourseSubject();
        if (name != null && !name.trim().isEmpty()) {
            condition.setName(name.trim());
        }
        if (parentId != null) {
            condition.setParentId(parentId);
        }
        
        PageHelper.startPage(pageNum, pageSize);
        List<CourseSubject> list = courseSubjectDao.queryAllByLimit(condition);
        PageInfo<CourseSubject> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<CourseSubjectVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        PageInfo<CourseSubjectVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        
        return result;
    }

    @Override
    public List<CourseSubjectVO> listAllCourseSubjects() {
        List<CourseSubject> list = courseSubjectDao.queryAllByLimit(new CourseSubject());
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseSubjectVO> listChildrenByParentId(Integer parentId) {
        if (parentId == null || parentId < 0) {
            throw new MyException("父分类ID无效");
        }
        
        CourseSubject condition = new CourseSubject();
        condition.setParentId(parentId);
        List<CourseSubject> children = courseSubjectDao.queryAllByLimit(condition);
        
        return children.stream()
                .map(this::convertToVO)
                .sorted(Comparator.comparing(CourseSubjectVO::getCreateTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseSubjectTreeVO> getCourseSubjectTree() {
        List<CourseSubject> allSubjects = courseSubjectDao.queryAllByLimit(new CourseSubject());
        
        // 按父分类ID分组
        Map<Integer, List<CourseSubject>> parentMap = allSubjects.stream()
                .collect(Collectors.groupingBy(s -> s.getParentId() == null ? 0 : s.getParentId()));
        
        return buildTree(0, parentMap);
    }

    @Override
    public List<CourseSubjectVO> searchCourseSubjectsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new MyException("搜索关键词不能为空");
        }
        
        // 构建查询条件，利用数据库的LIKE查询
        CourseSubject condition = new CourseSubject();
        condition.setName(keyword.trim());
        
        List<CourseSubject> subjects = courseSubjectDao.queryAllByLimit(condition);
        
        // 转换为VO并按创建时间排序
        return subjects.stream()
                .map(this::convertToVO)
                .sorted(Comparator.comparing(CourseSubjectVO::getCreateTime))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseSubjectVO createCourseSubject(CreateCourseSubjectDTO createDTO, Integer currentUserId) {
        if (createDTO == null) {
            throw new MyException("创建信息不能为空");
        }
        
        // 检查名称是否已存在
        if (isCourseSubjectNameExists(createDTO.getName(), null)) {
            throw new MyException("课程分类名称已存在");
        }
        
        // 构建实体
        CourseSubject courseSubject = new CourseSubject();
        courseSubject.setName(createDTO.getName().trim());
        courseSubject.setParentId(createDTO.getParentId() != null ? createDTO.getParentId() : 0);
        courseSubject.setCreateUser(currentUserId);
        courseSubject.setUpdateUser(currentUserId);
        courseSubject.setCreateTime(new Date());
        courseSubject.setUpdateTime(new Date());
        
        courseSubjectDao.insert(courseSubject);
        log.info("创建课程分类成功, ID: {}, 名称: {}", courseSubject.getId(), courseSubject.getName());
        
        return convertToVO(courseSubject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseSubjectVO updateCourseSubject(Integer id, UpdateCourseSubjectDTO updateDTO, Integer currentUserId) {
        if (id == null || id <= 0) {
            throw new MyException("课程分类ID无效");
        }
        if (updateDTO == null) {
            throw new MyException("更新信息不能为空");
        }
        
        // 检查分类是否存在
        CourseSubject existing = courseSubjectDao.queryById(id);
        if (existing == null) {
            throw new MyException("课程分类不存在");
        }
        
        // 检查名称是否重复（排除自己）
        if (isCourseSubjectNameExists(updateDTO.getName(), id)) {
            throw new MyException("课程分类名称已存在");
        }
        
        // 更新实体
        CourseSubject courseSubject = new CourseSubject();
        courseSubject.setId(id);
        courseSubject.setName(updateDTO.getName().trim());
        courseSubject.setParentId(updateDTO.getParentId() != null ? updateDTO.getParentId() : 0);
        courseSubject.setUpdateUser(currentUserId);
        courseSubject.setUpdateTime(new Date());
        
        courseSubjectDao.update(courseSubject);
        log.info("更新课程分类成功, ID: {}, 名称: {}", id, courseSubject.getName());
        
        return getCourseSubjectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseSubject(Integer id) {
        if (id == null || id <= 0) {
            throw new MyException("课程分类ID无效");
        }
        
        // 检查分类是否存在
        CourseSubject existing = courseSubjectDao.queryById(id);
        if (existing == null) {
            throw new MyException("课程分类不存在");
        }
        
        // 检查是否有子分类
        if (hasChildren(id)) {
            throw new MyException("该分类下有子分类，无法删除");
        }
        
        int result = courseSubjectDao.deleteById(id);
        if (result <= 0) {
            throw new MyException("删除课程分类失败");
        }
        
        log.info("删除课程分类成功, ID: {}", id);
    }

    @Override
    public boolean isCourseSubjectNameExists(String name, Integer excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        CourseSubject condition = new CourseSubject();
        condition.setName(name.trim());
        List<CourseSubject> list = courseSubjectDao.queryAllByLimit(condition);
        
        if (list.isEmpty()) {
            return false;
        }
        
        // 如果提供了excludeId，排除该ID的记录
        if (excludeId != null) {
            return list.stream().anyMatch(s -> !s.getId().equals(excludeId));
        }
        
        return true;
    }

    @Override
    public boolean hasChildren(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        CourseSubject condition = new CourseSubject();
        condition.setParentId(id);
        List<CourseSubject> children = courseSubjectDao.queryAllByLimit(condition);
        
        return !children.isEmpty();
    }

    /**
     * 递归构建树形结构
     */
    private List<CourseSubjectTreeVO> buildTree(Integer parentId, Map<Integer, List<CourseSubject>> parentMap) {
        return parentMap.getOrDefault(parentId, Collections.emptyList()).stream()
                .map(subject -> {
                    CourseSubjectTreeVO treeVO = new CourseSubjectTreeVO();
                    treeVO.setId(subject.getId());
                    treeVO.setName(subject.getName());
                    treeVO.setParentId(subject.getParentId());
                    treeVO.setCreateTime(subject.getCreateTime());
                    treeVO.setUpdateTime(subject.getUpdateTime());
                    treeVO.setChildren(buildTree(subject.getId(), parentMap));
                    return treeVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * Entity转VO
     */
    private CourseSubjectVO convertToVO(CourseSubject entity) {
        if (entity == null) {
            return null;
        }
        
        CourseSubjectVO vo = new CourseSubjectVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setParentId(entity.getParentId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        
        return vo;
    }

    // ==================== 内部辅助方法实现 ====================

    @Override
    public CourseSubject queryById(Integer id) {
        return courseSubjectDao.queryById(id);
    }

    @Override
    public List<CourseSubject> queryAllByLimit(CourseSubject condition) {
        return courseSubjectDao.queryAllByLimit(condition);
    }
}
