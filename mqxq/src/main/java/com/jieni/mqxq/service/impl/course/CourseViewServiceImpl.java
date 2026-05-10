package com.jieni.mqxq.service.impl.course;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CourseDao;
import com.jieni.mqxq.dao.CourseViewDao;
import com.jieni.mqxq.domain.dto.courseview.CourseViewCreateDTO;
import com.jieni.mqxq.domain.dto.courseview.CourseViewQueryDTO;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.entity.CourseView;
import com.jieni.mqxq.domain.vo.courseview.CourseViewStatisticsVO;
import com.jieni.mqxq.domain.vo.courseview.CourseViewVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.CourseViewService;
import com.jieni.mqxq.service.course.MyCourseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程浏览记录服务实现类
 * 
 * 提供课程浏览记录的完整管理服务，包括浏览历史记录、统计分析等
 * 支持用户浏览行为的跟踪、分页查询以及批量操作功能
 * 实现浏览数据的存储管理和数据清理机制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CourseViewServiceImpl implements CourseViewService {
    
    @Resource
    private CourseViewDao courseViewDao;
    
    @Resource
    private CourseDao courseDao;

    @Resource
    private MyCourseService myCourseService;

    /**
     * 分页查询课程浏览记录
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageInfo<CourseViewVO> getCourseViewPage(CourseViewQueryDTO queryDTO) {
        log.info("分页查询课程浏览记录, 查询条件: {}", queryDTO);
        
        // 构建查询条件
        CourseView condition = new CourseView();
        if (queryDTO.getCourseId() != null) {
            condition.setCourseId(queryDTO.getCourseId());
        }
        if (queryDTO.getUserId() != null) {
            condition.setUserId(queryDTO.getUserId());
        }
        if (queryDTO.getIpAddress() != null) {
            condition.setIpAddress(queryDTO.getIpAddress());
        }
        if (queryDTO.getUserAgent() != null) {
            condition.setUserAgent(queryDTO.getUserAgent());
        }
        
        // 分页查询
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<CourseView> list = courseViewDao.queryAllByLimit(condition);
        PageInfo<CourseView> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        List<CourseViewVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建VO分页结果
        PageInfo<CourseViewVO> result = new PageInfo<>();
        BeanUtil.copyProperties(pageInfo, result);
        result.setList(voList);
        
        return result;
    }

    /**
     * 根据ID获取浏览记录详情
     *
     * @param id 浏览记录ID
     * @return 浏览记录详情
     */
    @Override
    public CourseViewVO getCourseViewById(Integer id) {
        log.info("获取浏览记录详情, ID: {}", id);
        
        CourseView courseView = courseViewDao.queryById(id);
        if (courseView == null) {
            throw new MyException("浏览记录不存在");
        }
        
        return convertToVO(courseView);
    }

    /**
     * 记录课程浏览行为
     *
     * @param createDTO 浏览记录创建信息
     * @return 浏览记录详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseViewVO recordCourseView(CourseViewCreateDTO createDTO) {
        log.info("记录课程浏览行为, 创建信息: {}", createDTO);

        if (createDTO.getCourseId() == null || createDTO.getUserId() == null) {
            throw new MyException("课程ID和用户ID不能为空");
        }

        // 构建实体
        CourseView courseView = new CourseView();
        courseView.setCourseId(createDTO.getCourseId());
        courseView.setUserId(createDTO.getUserId());
        courseView.setIpAddress(createDTO.getIpAddress());
        courseView.setUserAgent(createDTO.getUserAgent());
        Date now = new Date();
        courseView.setViewTime(now);
        courseView.setCreateTime(now);

        // 保存
        courseViewDao.insert(courseView);

        // 触发学习状态流转（0 -> 1）
        tryStartLearning(createDTO.getUserId(), createDTO.getCourseId());

        return convertToVO(courseView);
    }

    /**
     * 删除指定的浏览记录
     *
     * @param id 浏览记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCourseViewById(Integer id) {
        log.info("删除浏览记录, ID: {}", id);
        
        CourseView courseView = courseViewDao.queryById(id);
        if (courseView == null) {
            throw new MyException("浏览记录不存在");
        }
        
        int result = courseViewDao.deleteById(id);
        if (result == 0) {
            throw new MyException("删除浏览记录失败");
        }
    }

    /**
     * 批量删除浏览记录
     *
     * @param ids 浏览记录ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCourseViewByIds(List<Integer> ids) {
        log.info("批量删除浏览记录, IDs: {}", ids);
        
        if (ids == null || ids.isEmpty()) {
            throw new MyException("请选择要删除的浏览记录");
        }
        
        int result = courseViewDao.deleteByIds(ids);
        if (result == 0) {
            throw new MyException("删除浏览记录失败");
        }
    }

    /**
     * 清空指定用户的浏览记录
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUserCourseViews(Integer userId) {
        log.info("清空用户浏览记录, userId: {}", userId);
        
        CourseView condition = new CourseView();
        condition.setUserId(userId);
        List<CourseView> userViews = courseViewDao.queryAllByLimit(condition);
        
        if (userViews.isEmpty()) {
            return;
        }
        
        List<Integer> ids = userViews.stream()
                .map(CourseView::getId)
                .collect(Collectors.toList());
        
        courseViewDao.deleteByIds(ids);
    }

    /**
     * 获取平台浏览统计信息（管理员）
     *
     * @return 统计信息
     */
    @Override
    public CourseViewStatisticsVO getPlatformViewStatistics() {
        log.info("获取平台浏览统计信息");
        
        // 查询所有浏览记录
        CourseView condition = new CourseView();
        List<CourseView> allViews = courseViewDao.queryAllByLimit(condition);
        
        long totalViews = allViews.size();
        long uniqueUsers = allViews.stream()
                .map(CourseView::getUserId)
                .distinct()
                .count();
        long uniqueCourses = allViews.stream()
                .map(CourseView::getCourseId)
                .distinct()
                .count();
        
        CourseViewStatisticsVO statistics = new CourseViewStatisticsVO();
        statistics.setTotalViews(totalViews);
        statistics.setUniqueUsers(uniqueUsers);
        statistics.setUniqueCourses(uniqueCourses);
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseViewVO recordOrUpdateCourseView(CourseViewCreateDTO createDTO) {
        log.info("记录或更新课程浏览行为, 创建信息: {}", createDTO);

        if (createDTO.getCourseId() == null || createDTO.getUserId() == null) {
            throw new MyException("课程ID和用户ID不能为空");
        }

        CourseView condition = new CourseView();
        condition.setCourseId(createDTO.getCourseId());
        condition.setUserId(createDTO.getUserId());
        List<CourseView> existingRecords = courseViewDao.queryAllByLimit(condition);

        if (existingRecords.isEmpty()) {
            return recordCourseView(createDTO);
        }

        CourseView record = existingRecords.get(0);
        Date now = new Date();
        record.setIpAddress(createDTO.getIpAddress());
        record.setUserAgent(createDTO.getUserAgent());
        record.setViewTime(now);
        courseViewDao.update(record);

        // 触发学习状态流转（0 -> 1）
        tryStartLearning(createDTO.getUserId(), createDTO.getCourseId());

        return convertToVO(record);
    }

    /**
     * 获取教师课程浏览统计信息
     *
     * @param teacherId 教师ID
     * @return 统计信息
     */
    @Override
    public CourseViewStatisticsVO getTeacherCourseViewStatistics(Integer teacherId) {
        log.info("获取教师课程浏览统计, teacherId: {}", teacherId);
        
        // 获取教师的所有课程
        Course courseCondition = new Course();
        courseCondition.setTeacherId(teacherId);
        List<Course> teacherCourses = courseDao.queryAllByLimit(courseCondition);
        
        if (teacherCourses.isEmpty()) {
            return new CourseViewStatisticsVO(0L, 0L, 0L, 0L, 0L, 0L);
        }
        
        List<Integer> teacherCourseIds = teacherCourses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());
        
        // 统计已发布课程数
        long publishedCourses = teacherCourses.stream()
                .filter(course -> course.getStatus() != null && course.getStatus() == 1)
                .count();
        
        // 统计浏览记录
        CourseView viewCondition = new CourseView();
        List<CourseView> allViews = courseViewDao.queryAllByLimit(viewCondition);
        
        List<CourseView> teacherViews = allViews.stream()
                .filter(view -> teacherCourseIds.contains(view.getCourseId()))
                .collect(Collectors.toList());
        
        long totalViews = teacherViews.size();
        long totalStudents = teacherViews.stream()
                .map(CourseView::getUserId)
                .distinct()
                .count();
        
        CourseViewStatisticsVO statistics = new CourseViewStatisticsVO();
        statistics.setTotalViews(totalViews);
        statistics.setTotalStudents(totalStudents);
        statistics.setTotalCourses((long) teacherCourses.size());
        statistics.setPublishedCourses(publishedCourses);
        statistics.setUniqueUsers(totalStudents);
        statistics.setUniqueCourses((long) teacherCourseIds.size());
        
        return statistics;
    }

    /**
     * 验证浏览记录是否属于指定用户
     *
     * @param viewId 浏览记录ID
     * @param userId 用户ID
     */
    @Override
    public void validateCourseViewOwnership(Integer viewId, Integer userId) {
        CourseView courseView = courseViewDao.queryById(viewId);
        if (courseView == null) {
            throw new MyException("浏览记录不存在");
        }
        
        if (!courseView.getUserId().equals(userId)) {
            throw new MyException("无权访问此浏览记录");
        }
    }

    /**
     * 将Entity转换为VO
     *
     * @param courseView 实体对象
     * @return VO对象
     */
    private CourseViewVO convertToVO(CourseView courseView) {
        CourseViewVO vo = new CourseViewVO();
        BeanUtil.copyProperties(courseView, vo);
        return vo;
    }

    /**
     * 尝试将学习状态从已加入更新为学习中
     */
    private void tryStartLearning(Integer userId, Integer courseId) {
        try {
            myCourseService.checkAndStartLearning(userId, courseId);
        } catch (Exception ex) {
            log.warn("更新学习状态到'学习中'失败, userId: {}, courseId: {}, error: {}", userId, courseId, ex.getMessage());
        }
    }
}
