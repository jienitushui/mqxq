package com.jieni.mqxq.service.impl.course;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CourseDao;
import com.jieni.mqxq.dao.UserDao;
import com.jieni.mqxq.domain.dto.course.*;
import com.jieni.mqxq.domain.dto.courseview.CourseViewCreateDTO;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.domain.vo.course.*;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.CourseService;
import com.jieni.mqxq.service.course.CourseViewService;
import com.jieni.mqxq.service.infrastructure.CourseVectorSyncService;
import com.jieni.mqxq.util.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    
    @Resource
    private CourseDao courseDao;
    
    @Resource
    private CourseViewService courseViewService;
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private CourseVectorSyncService courseVectorSyncService;

    @Override
    public CourseDetailVO getCourseDetailById(Integer id) {
        if (id == null || id <= 0) {
            throw new MyException("课程ID无效");
        }
        
        Course course = courseDao.queryById(id);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        CourseDetailVO vo = convertToDetailVO(course);
        fillTeacherName(vo);
        return vo;
    }

    @Override
    public PageInfo<CourseDetailVO> pageTeacherCourses(Integer teacherId, CourseQueryDTO queryDTO, Integer pageNum, Integer pageSize) {
        if (teacherId == null || teacherId <= 0) {
            throw new MyException("教师ID无效");
        }
        
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        
        // 构建查询条件
        Course condition = new Course();
        condition.setTeacherId(teacherId);
        
        if (queryDTO != null) {
            if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
                condition.setTitle(queryDTO.getTitle().trim());
            }
            if (queryDTO.getStatus() != null) {
                condition.setStatus(queryDTO.getStatus());
            }
            if (queryDTO.getSubjectId() != null) {
                condition.setSubjectId(queryDTO.getSubjectId());
            }
        }
        
        PageHelper.startPage(pageNum, pageSize);
        List<Course> list = courseDao.queryAllByLimit(condition);
        PageInfo<Course> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<CourseDetailVO> voList = list.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
        
        // 批量填充教师名称
        fillTeacherNamesForDetail(voList);
        
        PageInfo<CourseDetailVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        
        return result;
    }

    @Override
    public List<SimpleCourseVO> listSimpleCoursesByTeacher(Integer teacherId) {
        if (teacherId == null || teacherId <= 0) {
            throw new MyException("教师ID无效");
        }
        
        Course condition = new Course();
        condition.setTeacherId(teacherId);
        condition.setStatus(1); // 只查询已发布的课程
        
        List<Course> courses = courseDao.queryAllByLimit(condition);
        
        return courses.stream()
                .map(this::convertToSimpleVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseDetailVO createCourse(Integer teacherId, CreateCourseDTO createDTO) {
        if (teacherId == null || teacherId <= 0) {
            throw new MyException("教师ID无效");
        }
        if (createDTO == null) {
            throw new MyException("创建信息不能为空");
        }
        
        // 构建课程实体
        Course course = new Course();
        course.setTitle(createDTO.getTitle());
        course.setDescription(createDTO.getDescription());
        course.setCover(createDTO.getCover());
        course.setSubjectId(createDTO.getSubjectId());
        course.setPrice(createDTO.getPrice());
        course.setTeacherId(teacherId);
        course.setStatus(0); // 默认未发布
        course.setBuyCount(0);
        course.setViewCount(0);
        course.setCreateUser(teacherId);
        course.setUpdateUser(teacherId);
        course.setCreateTime(new Date());
        course.setUpdateTime(new Date());
        
        courseDao.insert(course);
        log.info("教师创建课程成功, teacherId: {}, courseId: {}, title: {}", teacherId, course.getId(), course.getTitle());
        
        // 同步到向量库（如果课程已发布）
        courseVectorSyncService.addCourseToVectorStore(course);
        
        CourseDetailVO vo = convertToDetailVO(course);
        fillTeacherName(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseDetailVO updateCourse(Integer courseId, Integer teacherId, UpdateCourseDTO updateDTO) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (teacherId == null || teacherId <= 0) {
            throw new MyException("教师ID无效");
        }
        if (updateDTO == null) {
            throw new MyException("更新信息不能为空");
        }
        
        // 检查课程是否存在
        Course existing = courseDao.queryById(courseId);
        if (existing == null) {
            throw new MyException("课程不存在");
        }
        
        // 验证权限
        if (!existing.getTeacherId().equals(teacherId)) {
            throw new MyException("无权修改此课程");
        }
        
        // 更新课程信息
        Course course = new Course();
        course.setId(courseId);
        
        if (updateDTO.getTitle() != null) {
            course.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            course.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getCover() != null) {
            course.setCover(updateDTO.getCover());
        }
        if (updateDTO.getSubjectId() != null) {
            course.setSubjectId(updateDTO.getSubjectId());
        }
        if (updateDTO.getPrice() != null) {
            course.setPrice(updateDTO.getPrice());
        }
        
        course.setUpdateUser(teacherId);
        course.setUpdateTime(new Date());
        
        courseDao.update(course);
        log.info("教师更新课程成功, teacherId: {}, courseId: {}", teacherId, courseId);
        
        // 同步到向量库（更新或删除）
        Course updatedCourse = courseDao.queryById(courseId);
        if (updatedCourse != null) {
            courseVectorSyncService.updateCourseInVectorStore(updatedCourse);
        }
        
        return getCourseDetailById(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishCourse(Integer courseId, Integer teacherId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (teacherId == null || teacherId <= 0) {
            throw new MyException("教师ID无效");
        }
        
        // 检查课程是否存在
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 验证权限
        if (!course.getTeacherId().equals(teacherId)) {
            throw new MyException("无权发布此课程");
        }
        
        // 检查课程是否可以发布
        if (!canPublishCourse(courseId)) {
            throw new MyException("课程信息不完整，无法发布");
        }
        
        // 更新状态
        Course updateCourse = new Course();
        updateCourse.setId(courseId);
        updateCourse.setStatus(1);
        updateCourse.setPublishTime(new Date());
        updateCourse.setUpdateUser(teacherId);
        updateCourse.setUpdateTime(new Date());
        
        courseDao.update(updateCourse);
        log.info("教师发布课程成功, teacherId: {}, courseId: {}", teacherId, courseId);
        
        // 同步到向量库（发布后添加到向量库）
        Course publishedCourse = courseDao.queryById(courseId);
        if (publishedCourse != null) {
            courseVectorSyncService.addCourseToVectorStore(publishedCourse);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishCourse(Integer courseId, Integer teacherId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (teacherId == null || teacherId <= 0) {
            throw new MyException("教师ID无效");
        }
        
        // 检查课程是否存在
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 验证权限
        if (!course.getTeacherId().equals(teacherId)) {
            throw new MyException("无权操作此课程");
        }
        
        // 更新状态
        Course updateCourse = new Course();
        updateCourse.setId(courseId);
        updateCourse.setStatus(0);
        updateCourse.setUpdateUser(teacherId);
        updateCourse.setUpdateTime(new Date());
        
        courseDao.update(updateCourse);
        log.info("教师取消发布课程成功, teacherId: {}, courseId: {}", teacherId, courseId);
        
        // 同步到向量库（取消发布后从向量库删除）
        courseVectorSyncService.deleteCourseFromVectorStore(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Integer courseId, Integer teacherId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (teacherId == null || teacherId <= 0) {
            throw new MyException("教师ID无效");
        }
        
        // 检查课程是否存在
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 验证权限
        if (!course.getTeacherId().equals(teacherId)) {
            throw new MyException("无权删除此课程");
        }
        
        // 已发布的课程不允许删除
        if (course.getStatus() != null && course.getStatus() == 1) {
            throw new MyException("已发布的课程不能删除，请先取消发布");
        }
        
        int result = courseDao.deleteById(courseId);
        if (result <= 0) {
            throw new MyException("删除课程失败");
        }
        
        // 同步到向量库（删除课程）
        courseVectorSyncService.deleteCourseFromVectorStore(courseId);
        
        log.info("教师删除课程成功, teacherId: {}, courseId: {}", teacherId, courseId);
    }

    @Override
    public boolean isCourseOwnedByTeacher(Integer courseId, Integer teacherId) {
        if (courseId == null || courseId <= 0 || teacherId == null || teacherId <= 0) {
            return false;
        }
        
        Course course = courseDao.queryById(courseId);
        return course != null && course.getTeacherId().equals(teacherId);
    }

    @Override
    public boolean canPublishCourse(Integer courseId) {
        if (courseId == null || courseId <= 0) {
            return false;
        }
        
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            return false;
        }
        
        // 检查必要信息是否完整
        return course.getTitle() != null && !course.getTitle().trim().isEmpty()
                && course.getDescription() != null && !course.getDescription().trim().isEmpty()
                && course.getCover() != null && !course.getCover().trim().isEmpty();
    }

    /**
     * Entity转详情VO
     */
    private CourseDetailVO convertToDetailVO(Course entity) {
        if (entity == null) {
            return null;
        }
        
        CourseDetailVO vo = new CourseDetailVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setCover(entity.getCover());
        vo.setSubjectId(entity.getSubjectId());
        vo.setTeacherId(entity.getTeacherId());
        vo.setPrice(entity.getPrice());
        vo.setLessonNum(entity.getLessonNum());
        vo.setDurationSum(entity.getDurationSum());
        vo.setStatus(entity.getStatus());
        vo.setBuyCount(entity.getBuyCount());
        vo.setViewCount(entity.getViewCount());
        vo.setPublishTime(entity.getPublishTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        
        return vo;
    }
    
    /**
     * 填充单个课程详情的教师名称
     */
    private void fillTeacherName(CourseDetailVO vo) {
        if (vo == null || vo.getTeacherId() == null) {
            return;
        }
        
        try {
            User user = userDao.queryById(vo.getTeacherId());
            if (user != null) {
                vo.setTeacherName(user.getUsername() != null ? user.getUsername() : "");
            }
        } catch (Exception e) {
            log.warn("查询教师信息失败, teacherId: {}", vo.getTeacherId(), e);
        }
    }
    
    /**
     * 批量填充课程详情VO的教师名称
     */
    private void fillTeacherNamesForDetail(List<CourseDetailVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        
        // 收集所有需要查询的教师ID
        Set<Integer> teacherIds = voList.stream()
                .filter(vo -> vo.getTeacherId() != null)
                .map(CourseDetailVO::getTeacherId)
                .collect(Collectors.toSet());
        
        if (teacherIds.isEmpty()) {
            return;
        }
        
        // 批量查询教师信息
        Map<Integer, String> teacherNameMap = new HashMap<>();
        for (Integer teacherId : teacherIds) {
            try {
                User user = userDao.queryById(teacherId);
                if (user != null) {
                    teacherNameMap.put(teacherId, user.getUsername() != null ? user.getUsername() : "");
                }
            } catch (Exception e) {
                log.warn("查询教师信息失败, teacherId: {}", teacherId, e);
            }
        }
        
        // 填充教师名称
        for (CourseDetailVO vo : voList) {
            if (vo.getTeacherId() != null) {
                vo.setTeacherName(teacherNameMap.getOrDefault(vo.getTeacherId(), ""));
            }
        }
    }

    /**
     * Entity转简化VO
     */
    private SimpleCourseVO convertToSimpleVO(Course entity) {
        if (entity == null) {
            return null;
        }
        
        SimpleCourseVO vo = new SimpleCourseVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setSubjectId(entity.getSubjectId());
        
        return vo;
    }

    /**
     * Entity转列表VO
     */
    private CourseListVO convertToListVO(Course entity) {
        if (entity == null) {
            return null;
        }
        
        CourseListVO vo = new CourseListVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setCover(entity.getCover());
        vo.setSubjectId(entity.getSubjectId());
        vo.setTeacherId(entity.getTeacherId());
        vo.setPrice(entity.getPrice());
        vo.setLessonNum(entity.getLessonNum());
        vo.setDurationSum(entity.getDurationSum());
        vo.setStatus(entity.getStatus());
        vo.setBuyCount(entity.getBuyCount());
        vo.setViewCount(entity.getViewCount());
        vo.setPublishTime(entity.getPublishTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        
        return vo;
    }
    
    /**
     * 批量填充教师名称
     */
    private void fillTeacherNames(List<CourseListVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        
        // 收集所有需要查询的教师ID
        Set<Integer> teacherIds = voList.stream()
                .filter(vo -> vo.getTeacherId() != null)
                .map(CourseListVO::getTeacherId)
                .collect(Collectors.toSet());
        
        if (teacherIds.isEmpty()) {
            return;
        }
        
        // 批量查询教师信息
        Map<Integer, String> teacherNameMap = new HashMap<>();
        for (Integer teacherId : teacherIds) {
            try {
                User user = userDao.queryById(teacherId);
                if (user != null) {
                    teacherNameMap.put(teacherId, user.getUsername() != null ? user.getUsername() : "");
                }
            } catch (Exception e) {
                log.warn("查询教师信息失败, teacherId: {}", teacherId, e);
            }
        }
        
        // 填充教师名称
        for (CourseListVO vo : voList) {
            if (vo.getTeacherId() != null) {
                vo.setTeacherName(teacherNameMap.getOrDefault(vo.getTeacherId(), ""));
            }
        }
    }

    // ==================== 管理员端方法实现 ====================

    @Override
    public PageInfo<CourseListVO> pageCoursesForAdmin(CoursePageQueryDTO queryDTO) {
        if (queryDTO == null) {
            throw new MyException("查询参数不能为空");
        }
        
        Integer pageNum = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
        Integer pageSize = queryDTO.getSize() != null ? queryDTO.getSize() : 10;
        
        // 构建查询条件
        Course condition = new Course();
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            condition.setTitle(queryDTO.getTitle().trim());
        }
        if (queryDTO.getStatus() != null) {
            condition.setStatus(queryDTO.getStatus());
        }
        if (queryDTO.getSubjectId() != null) {
            condition.setSubjectId(queryDTO.getSubjectId());
        }
        if (queryDTO.getTeacherId() != null) {
            condition.setTeacherId(queryDTO.getTeacherId());
        }
        
        PageHelper.startPage(pageNum, pageSize);
        List<Course> list = courseDao.queryAllByLimit(condition);
        PageInfo<Course> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<CourseListVO> voList = list.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        
        PageInfo<CourseListVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createCourseByAdmin(Integer adminId, CreateCourseDTO createDTO) {
        if (adminId == null || adminId <= 0) {
            throw new MyException("管理员ID无效");
        }
        if (createDTO == null) {
            throw new MyException("创建信息不能为空");
        }
        if (createDTO.getTeacherId() == null) {
            throw new MyException("请选择课程教师");
        }
        
        // 构建课程实体
        Course course = new Course();
        course.setTitle(createDTO.getTitle());
        course.setDescription(createDTO.getDescription());
        course.setCover(createDTO.getCover());
        course.setSubjectId(createDTO.getSubjectId());
        course.setPrice(createDTO.getPrice());
        course.setTeacherId(createDTO.getTeacherId());
        course.setStatus(0); // 默认未发布
        course.setBuyCount(0);
        course.setViewCount(0);
        course.setCreateUser(adminId);
        course.setUpdateUser(adminId);
        course.setCreateTime(new Date());
        course.setUpdateTime(new Date());
        
        courseDao.insert(course);
        log.info("管理员创建课程成功, adminId: {}, courseId: {}, title: {}", adminId, course.getId(), course.getTitle());
        
        // 同步到向量库（如果课程已发布）
        courseVectorSyncService.addCourseToVectorStore(course);
        
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseByAdmin(Integer courseId, Integer adminId, UpdateCourseDTO updateDTO) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (adminId == null || adminId <= 0) {
            throw new MyException("管理员ID无效");
        }
        if (updateDTO == null) {
            throw new MyException("更新信息不能为空");
        }
        
        // 检查课程是否存在
        Course existing = courseDao.queryById(courseId);
        if (existing == null) {
            throw new MyException("课程不存在");
        }
        
        // 更新课程信息
        Course course = new Course();
        course.setId(courseId);
        
        if (updateDTO.getTitle() != null) {
            course.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            course.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getCover() != null) {
            course.setCover(updateDTO.getCover());
        }
        if (updateDTO.getSubjectId() != null) {
            course.setSubjectId(updateDTO.getSubjectId());
        }
        if (updateDTO.getPrice() != null) {
            course.setPrice(updateDTO.getPrice());
        }
        
        course.setUpdateUser(adminId);
        course.setUpdateTime(new Date());
        
        courseDao.update(course);
        log.info("管理员更新课程成功, adminId: {}, courseId: {}", adminId, courseId);
        
        // 同步到向量库（更新或删除）
        Course updatedCourse = courseDao.queryById(courseId);
        if (updatedCourse != null) {
            courseVectorSyncService.updateCourseInVectorStore(updatedCourse);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishCourseByAdmin(Integer courseId, Integer adminId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (adminId == null || adminId <= 0) {
            throw new MyException("管理员ID无效");
        }
        
        // 检查课程是否存在
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 检查课程是否可以发布
        if (!canPublishCourse(courseId)) {
            throw new MyException("课程信息不完整，无法发布");
        }
        
        // 更新状态
        Course updateCourse = new Course();
        updateCourse.setId(courseId);
        updateCourse.setStatus(1);
        updateCourse.setPublishTime(new Date());
        updateCourse.setUpdateUser(adminId);
        updateCourse.setUpdateTime(new Date());
        
        courseDao.update(updateCourse);
        log.info("管理员发布课程成功, adminId: {}, courseId: {}", adminId, courseId);
        
        // 同步到向量库（发布后添加到向量库）
        Course publishedCourse = courseDao.queryById(courseId);
        if (publishedCourse != null) {
            courseVectorSyncService.addCourseToVectorStore(publishedCourse);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishCourseByAdmin(Integer courseId, Integer adminId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (adminId == null || adminId <= 0) {
            throw new MyException("管理员ID无效");
        }
        
        // 检查课程是否存在
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 更新状态
        Course updateCourse = new Course();
        updateCourse.setId(courseId);
        updateCourse.setStatus(0);
        updateCourse.setUpdateUser(adminId);
        updateCourse.setUpdateTime(new Date());
        
        courseDao.update(updateCourse);
        log.info("管理员取消发布课程成功, adminId: {}, courseId: {}", adminId, courseId);
        
        // 同步到向量库（取消发布后从向量库删除）
        courseVectorSyncService.deleteCourseFromVectorStore(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseByAdmin(Integer courseId, Integer adminId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        if (adminId == null || adminId <= 0) {
            throw new MyException("管理员ID无效");
        }
        
        // 检查课程是否存在
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        int result = courseDao.deleteById(courseId);
        if (result <= 0) {
            throw new MyException("删除课程失败");
        }
        
        // 同步到向量库（删除课程）
        courseVectorSyncService.deleteCourseFromVectorStore(courseId);
        
        log.info("管理员删除课程成功, adminId: {}, courseId: {}", adminId, courseId);
    }

    @Override
    public CourseStatisticsVO getCourseStatistics() {
        Course condition = new Course();
        List<Course> allCourses = courseDao.queryAllByLimit(condition);
        
        long totalCount = allCourses.size();
        long publishedCount = allCourses.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .count();
        long unpublishedCount = totalCount - publishedCount;
        
        return new CourseStatisticsVO(totalCount, publishedCount, unpublishedCount);
    }

    // ==================== 用户端方法实现 ====================

    @Override
    public PageInfo<CourseListVO> pagePublishedCourses(PublishedCourseQueryDTO queryDTO) {
        if (queryDTO == null) {
            throw new MyException("查询参数不能为空");
        }
        
        Integer pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        Integer pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        
        // 构建查询条件 - 只查询已发布的课程
        Course condition = new Course();
        condition.setStatus(1);
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            condition.setTitle(queryDTO.getTitle().trim());
        }
        if (queryDTO.getSubjectId() != null) {
            condition.setSubjectId(queryDTO.getSubjectId());
        }
        
        PageHelper.startPage(pageNum, pageSize);
        List<Course> list = courseDao.queryAllByLimit(condition);
        PageInfo<Course> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<CourseListVO> voList = list.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        
        // 批量填充教师名称
        fillTeacherNames(voList);
        
        PageInfo<CourseListVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseDetailVO getPublishedCourseDetail(Integer courseId, Integer userId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        
        Course course = courseDao.queryById(courseId);
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 检查课程是否已发布
        if (course.getStatus() == null || course.getStatus() != 1) {
            throw new MyException("该课程暂未发布");
        }
        
        // 记录浏览
        if (userId != null) {
            recordCourseView(courseId, userId);
        }
        
        // 更新浏览数
        Course updateCourse = new Course();
        updateCourse.setId(courseId);
        updateCourse.setViewCount(course.getViewCount() == null ? 1 : course.getViewCount() + 1);
        courseDao.update(updateCourse);
        
        CourseDetailVO vo = convertToDetailVO(course);
        fillTeacherName(vo);
        return vo;
    }

    @Override
    public List<CourseListVO> listHotCourses(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }
        
        Course condition = new Course();
        condition.setStatus(1); // 只查询已发布的课程
        
        List<Course> courses = courseDao.queryAllByLimit(condition);
        
        // 按浏览量降序排序
        List<CourseListVO> voList = courses.stream()
                .sorted(Comparator.comparingInt((Course c) -> c.getViewCount() == null ? 0 : c.getViewCount()).reversed())
                .limit(limit)
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        
        // 批量填充教师名称
        fillTeacherNames(voList);
        
        return voList;
    }

    @Override
    public PageInfo<CourseListVO> pageFreeCourses(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        
        Course condition = new Course();
        condition.setStatus(1); // 已发布
        condition.setPrice(BigDecimal.ZERO); // 免费
        
        PageHelper.startPage(pageNum, pageSize);
        List<Course> list = courseDao.queryAllByLimit(condition);
        PageInfo<Course> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<CourseListVO> voList = list.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        
        // 批量填充教师名称
        fillTeacherNames(voList);
        
        PageInfo<CourseListVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        
        return result;
    }

    @Override
    public Course queryById(Integer id) {
        return courseDao.queryById(id);
    }

    @Override
    public List<Course> queryAll(Course condition) {
        return courseDao.queryAllByLimit(condition);
    }

    @Override
    public Course update(Course course) {
        if (course == null || course.getId() == null) {
            throw new MyException("课程信息或ID不能为空");
        }
        courseDao.update(course);
        return courseDao.queryById(course.getId());
    }

    @Override
    public void recordCourseView(Integer courseId, Integer userId) {
        if (courseId == null || courseId <= 0 || userId == null || userId <= 0) {
            return;
        }
        
        try {
            CourseViewCreateDTO createDTO = new CourseViewCreateDTO();
            createDTO.setCourseId(courseId);
            createDTO.setUserId(userId);
            createDTO.setIpAddress(IpUtils.getIpAddr());
            
            // 获取User-Agent
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                createDTO.setUserAgent(request.getHeader("User-Agent"));
            }
            
            courseViewService.recordOrUpdateCourseView(createDTO);
        } catch (Exception e) {
            log.error("记录课程浏览失败, courseId: {}, userId: {}, error: {}", courseId, userId, e.getMessage());
            // 不抛出异常，避免影响主流程
        }
    }
}
