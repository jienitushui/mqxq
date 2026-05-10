package com.jieni.mqxq.service.impl.course;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CourseCommentDao;
import com.jieni.mqxq.dao.CourseDao;
import com.jieni.mqxq.dao.MyCourseDao;
import com.jieni.mqxq.dao.OrdersDao;
import com.jieni.mqxq.domain.dto.course.MyCourseCreateDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseQueryDTO;
import com.jieni.mqxq.domain.dto.course.MyCourseUpdateDTO;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.entity.MyCourse;
import com.jieni.mqxq.domain.entity.Orders;
import com.jieni.mqxq.domain.vo.course.MyCourseStatisticsVO;
import com.jieni.mqxq.domain.vo.course.MyCourseVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.MyCourseService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 我的课程服务实现类
 *
 * 提供用户课程关系管理的完整业务实现，包括课程加入、学习状态管理、学习统计等功能
 * 支持学习状态管理、学习时长统计以及课程状态更新功能
 * 实现用户课程数据的存储管理和分析统计，为不同角色提供对应的数据服务
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class MyCourseServiceImpl implements MyCourseService {

    @Resource
    private MyCourseDao myCourseDao;

    @Resource
    private CourseDao courseDao;

    @Resource
    private CourseCommentDao courseCommentDao;

    @Resource
    private OrdersDao orderDao;

    @Resource
    private com.jieni.mqxq.dao.CourseViewDao courseViewDao;

    @Resource
    private com.jieni.mqxq.dao.HomeworkSubmissionDao homeworkSubmissionDao;

    @Resource
    private com.jieni.mqxq.dao.CourseHomeworkDao courseHomeworkDao;

    @Resource
    private com.jieni.mqxq.dao.SectionDao sectionDao;

    @Resource
    private com.jieni.mqxq.dao.ChapterDao chapterDao;

    // ========== 基础CRUD方法 ==========

    @Override
    public MyCourseVO getMyCourseById(Integer id) {
        validateId(id, "课程记录ID");
        MyCourse myCourse = myCourseDao.queryById(id);
        if (myCourse == null) {
            throw new MyException("课程记录不存在");
        }
        return convertToVO(myCourse);
    }

    @Override
    public MyCourseVO getMyCourseByCourseId(Integer userId, Integer courseId) {
        validateId(userId, "用户ID");
        validateId(courseId, "课程ID");
        MyCourse myCourse = myCourseDao.queryByUserIdAndCourseId(userId, courseId);
        if (myCourse == null) {
            throw new MyException("课程记录不存在，请先加入该课程");
        }
        return convertToVO(myCourse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyCourseVO joinCourse(Integer userId, MyCourseCreateDTO createDTO) {
        validateId(userId, "用户ID");
        validateId(createDTO.getCourseId(), "课程ID");

        // 检查是否已加入
        if (hasJoinedCourse(userId, createDTO.getCourseId())) {
            throw new MyException("您已加入该课程");
        }

        // 查找已完成的订单
        Orders order = orderDao.selectByUserIdAndCourseIdDone(userId, createDTO.getCourseId());
        if (order == null) {
            throw new MyException("未找到该课程的有效订单，请先购买课程");
        }

        // 创建课程记录
        MyCourse myCourse = new MyCourse();
        myCourse.setUserId(userId);
        myCourse.setCourseId(createDTO.getCourseId());
        myCourse.setOrderId(order.getId());
        myCourse.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 0);
        
        Date now = new Date();
        myCourse.setCreateTime(now);
        myCourse.setUpdateTime(now);

        myCourseDao.insert(myCourse);
        log.info("用户{}成功加入课程{}", userId, createDTO.getCourseId());

        return getMyCourseById(myCourse.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyCourseVO updateStudyProgress(Integer userId, MyCourseUpdateDTO updateDTO) {
        validateId(userId, "用户ID");
        validateId(updateDTO.getId(), "课程记录ID");

        MyCourse myCourse = myCourseDao.queryById(updateDTO.getId());
        if (myCourse == null) {
            throw new MyException("课程记录不存在");
        }

        // 验证权限：只能更新自己的课程
        if (!myCourse.getUserId().equals(userId)) {
            throw new MyException("无权限更新此课程记录");
        }

        // 更新状态
        if (updateDTO.getStatus() != null) {
            myCourse.setStatus(updateDTO.getStatus());
        }
        myCourse.setUpdateTime(new Date());

        myCourseDao.update(myCourse);
        log.info("用户{}更新课程记录{}学习状态", userId, updateDTO.getId());

        return getMyCourseById(updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitCourse(Integer userId, Integer courseId) {
        validateId(userId, "用户ID");
        validateId(courseId, "课程ID");

        MyCourse myCourse = findByUserAndCourse(userId, courseId);
        if (myCourse == null) {
            throw new MyException("未找到课程记录");
        }

        myCourseDao.deleteById(myCourse.getId());
        log.info("用户{}成功退出课程{}", userId, courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMyCourseById(Integer id) {
        validateId(id, "课程记录ID");
        
        MyCourse myCourse = myCourseDao.queryById(id);
        if (myCourse == null) {
            throw new MyException("课程记录不存在");
        }

        myCourseDao.deleteById(id);
        log.info("删除课程记录成功，ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteMyCourses(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new MyException("ID列表不能为空");
        }

        int count = myCourseDao.deleteBatchByIds(ids);
        log.info("批量删除课程记录完成，成功删除{}条", count);
        return count;
    }

    // ========== 查询方法 ==========

    @Override
    public boolean hasJoinedCourse(Integer userId, Integer courseId) {
        validateId(userId, "用户ID");
        validateId(courseId, "课程ID");
        return findByUserAndCourse(userId, courseId) != null;
    }

    @Override
    public PageInfo<MyCourseVO> getMyCourseListByPage(MyCourseQueryDTO queryDTO) {
        if (queryDTO == null) {
            throw new MyException("查询条件不能为空");
        }

        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<MyCourse> list = myCourseDao.queryListByConditions(params);
        
        PageInfo<MyCourse> pageInfo = new PageInfo<>(list);
        return convertToPageVO(pageInfo);
    }

    // ========== 统计方法 ==========

    @Override
    public MyCourseStatisticsVO getUserStudyStatistics(Integer userId) {
        validateId(userId, "用户ID");

        MyCourseQueryDTO queryDTO = new MyCourseQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Integer.MAX_VALUE);
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<MyCourse> allCourses = myCourseDao.queryListByConditions(params);

        MyCourseStatisticsVO statisticsVO = new MyCourseStatisticsVO();
        statisticsVO.setTotalCourses(allCourses.size());
        statisticsVO.setCompletedCourses((int) allCourses.stream().filter(c -> c.getStatus() == 2).count());
        statisticsVO.setStudyingCourses((int) allCourses.stream().filter(c -> c.getStatus() == 1).count());

        return statisticsVO;
    }

    @Override
    public MyCourseStatisticsVO getCourseStudyStatistics(Integer courseId) {
        validateId(courseId, "课程ID");

        MyCourseQueryDTO queryDTO = new MyCourseQueryDTO();
        queryDTO.setCourseId(courseId);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Integer.MAX_VALUE);
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<MyCourse> courseStudents = myCourseDao.queryListByConditions(params);

        MyCourseStatisticsVO statisticsVO = new MyCourseStatisticsVO();
        statisticsVO.setTotalStudents(courseStudents.size());
        statisticsVO.setActiveStudents((int) courseStudents.stream().filter(c -> c.getStatus() == 1).count());
        statisticsVO.setCompletedStudents((int) courseStudents.stream().filter(c -> c.getStatus() == 2).count());

        // 计算完成率
        if (!courseStudents.isEmpty()) {
            double completionRate = (double) statisticsVO.getCompletedStudents() / courseStudents.size() * 100;
            statisticsVO.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        } else {
            statisticsVO.setCompletionRate(0.0);
        }

        return statisticsVO;
    }

    @Override
    public MyCourseStatisticsVO getTeacherCoursesStatistics(Integer teacherId) {
        validateId(teacherId, "教师ID");

        MyCourseQueryDTO queryDTO = new MyCourseQueryDTO();
        queryDTO.setTeacherId(teacherId);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Integer.MAX_VALUE);
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<MyCourse> teacherCourseStudents = myCourseDao.queryListByConditions(params);

        MyCourseStatisticsVO statisticsVO = new MyCourseStatisticsVO();
        
        // 课程数量统计
        Set<Integer> uniqueCourses = teacherCourseStudents.stream()
                .map(MyCourse::getCourseId)
                .collect(Collectors.toSet());
        statisticsVO.setTotalCourses(uniqueCourses.size());
        
        // 学员统计
        statisticsVO.setTotalStudents(teacherCourseStudents.size());
        
        // 计算教师所有课程的平均评分
        double totalRating = 0.0;
        int courseCount = 0;
        for (Integer courseId : uniqueCourses) {
            Double courseRating = courseCommentDao.getAverageRatingByCourseId(courseId);
            if (courseRating != null && courseRating > 0) {
                totalRating += courseRating;
                courseCount++;
            }
        }
        double averageRating = courseCount > 0 ? totalRating / courseCount : 0.0;
        statisticsVO.setAverageRating(Math.round(averageRating * 100.0) / 100.0);

        return statisticsVO;
    }

    @Override
    public MyCourseStatisticsVO getOverallStudyStatistics() {
        Map<String, Object> params = new HashMap<>();
        List<MyCourse> allRecords = myCourseDao.queryListByConditions(params);

        MyCourseStatisticsVO statisticsVO = new MyCourseStatisticsVO();
        
        // 总体统计
        Set<Integer> uniqueUsers = allRecords.stream().map(MyCourse::getUserId).collect(Collectors.toSet());
        Set<Integer> uniqueCourses = allRecords.stream().map(MyCourse::getCourseId).collect(Collectors.toSet());
        
        statisticsVO.setTotalUsers(uniqueUsers.size());
        statisticsVO.setTotalCourses(uniqueCourses.size());
        statisticsVO.setTotalEnrollments(allRecords.size());
        
        // 完成率统计
        long completedCount = allRecords.stream().filter(c -> c.getStatus() == 2).count();
        double completionRate = allRecords.isEmpty() ? 0.0 : (double) completedCount / allRecords.size() * 100;
        statisticsVO.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);

        return statisticsVO;
    }

    // ========== 教师端方法 ==========

    @Override
    public boolean checkTeacherCoursePermission(Integer teacherId, Integer courseId) {
        validateId(teacherId, "教师ID");
        validateId(courseId, "课程ID");

        Course course = courseDao.queryById(courseId);
        return course != null && course.getTeacherId().equals(teacherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRemoveStudentsByTeacher(List<Integer> myCourseIds, Integer teacherId) {
        validateId(teacherId, "教师ID");
        if (myCourseIds == null || myCourseIds.isEmpty()) {
            throw new MyException("ID列表不能为空");
        }

        int count = 0;
        for (Integer id : myCourseIds) {
            MyCourse myCourse = myCourseDao.queryById(id);
            if (myCourse != null && checkTeacherCoursePermission(teacherId, myCourse.getCourseId())) {
                myCourseDao.deleteById(id);
                count++;
            }
        }
        
        log.info("教师{}批量移除学员完成，成功移除{}个", teacherId, count);
        return count;
    }

    @Override
    public void exportCourseStudents(Integer courseId, HttpServletResponse response) {
        log.info("导出课程学员列表, courseId: {}", courseId);
        
        validateId(courseId, "课程ID");

        // 1. 查询数据
        MyCourseQueryDTO queryDTO = new MyCourseQueryDTO();
        queryDTO.setCourseId(courseId);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Integer.MAX_VALUE);
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<MyCourse> students = myCourseDao.queryListByConditions(params);

        // 2. 转换为导出格式
        List<Map<String, Object>> exportData = students.stream().map(student -> {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", student.getUserId());
            data.put("userName", student.getUserNickname() != null ? student.getUserNickname() : "未知");
            data.put("courseId", student.getCourseId());
            data.put("courseName", student.getCourseName() != null ? student.getCourseName() : "");
            data.put("status", getStatusText(student.getStatus()));
            data.put("joinTime", student.getCreateTime());
            data.put("lastStudyTime", student.getUpdateTime());
            return data;
        }).collect(Collectors.toList());

        // 3. 定义表头和数据键
        List<String> headers = Arrays.asList(
            "学员ID", "学员姓名", "课程ID", "课程名称", 
            "学习状态", "加入时间", "最后学习时间"
        );
        List<String> dataKeys = Arrays.asList(
            "userId", "userName", "courseId", "courseName",
            "status", "joinTime", "lastStudyTime"
        );

        // 4. 生成Excel文件并写入响应
        String fileName = com.jieni.mqxq.util.ExcelExportUtil.generateFileName("课程学员列表");
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + fileName + ".xlsx";
        
        com.jieni.mqxq.util.ExcelExportUtil.exportToFile(
            filePath, "课程学员列表", headers, dataKeys, exportData
        );
        com.jieni.mqxq.util.ExcelExportUtil.writeFileToResponseAndDelete(response, filePath, fileName);
        
        log.info("课程学员列表导出成功并已触发下载, 文件路径: {}", filePath);
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "已加入";
            case 1: return "学习中";
            case 2: return "已完成";
            default: return "未知";
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 验证ID是否有效
     */
    private void validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new MyException(fieldName + "不能为空且必须大于0");
        }
    }

    /**
     * 根据用户ID和课程ID查询
     */
    private MyCourse findByUserAndCourse(Integer userId, Integer courseId) {
        return myCourseDao.queryByUserIdAndCourseId(userId, courseId);
    }

    /**
     * 构建查询参数
     */
    private Map<String, Object> buildQueryParams(MyCourseQueryDTO queryDTO) {
        Map<String, Object> params = new HashMap<>();
        
        if (queryDTO.getUserId() != null) {
            params.put("userId", queryDTO.getUserId());
        }
        if (queryDTO.getCourseId() != null) {
            params.put("courseId", queryDTO.getCourseId());
        }
        if (queryDTO.getOrderId() != null) {
            params.put("orderId", queryDTO.getOrderId());
        }
        if (queryDTO.getStatus() != null) {
            params.put("status", queryDTO.getStatus());
        }
        if (queryDTO.getTeacherId() != null) {
            params.put("teacherId", queryDTO.getTeacherId());
        }
        if (queryDTO.getUserNickname() != null && !queryDTO.getUserNickname().trim().isEmpty()) {
            params.put("userNickname", queryDTO.getUserNickname().trim());
        }
        if (queryDTO.getCourseName() != null && !queryDTO.getCourseName().trim().isEmpty()) {
            params.put("courseName", queryDTO.getCourseName().trim());
        }
        if (queryDTO.getStudentName() != null && !queryDTO.getStudentName().trim().isEmpty()) {
            params.put("studentName", queryDTO.getStudentName().trim());
        }
        
        return params;
    }

    /**
     * 转换为VO对象
     */
    private MyCourseVO convertToVO(MyCourse myCourse) {
        if (myCourse == null) {
            return null;
        }
        
        MyCourseVO vo = new MyCourseVO();
        BeanUtils.copyProperties(myCourse, vo);
        return vo;
    }

    /**
     * 转换分页结果为VO分页
     */
    private PageInfo<MyCourseVO> convertToPageVO(PageInfo<MyCourse> pageInfo) {
        PageInfo<MyCourseVO> voPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, voPageInfo);
        voPageInfo.setList(pageInfo.getList().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPageInfo;
    }

    // ========== 学习状态自动流转方法 ==========

    /**
     * 检查并更新学习状态为"学习中"
     * 当用户首次学习行为时调用（观看课程、提交作业等）
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否更新了状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndStartLearning(Integer userId, Integer courseId) {
        try {
            validateId(userId, "用户ID");
            validateId(courseId, "课程ID");

            MyCourse myCourse = findByUserAndCourse(userId, courseId);
            if (myCourse == null) {
                log.warn("用户{}未加入课程{}，无法更新学习状态", userId, courseId);
                return false;
            }

            // 只有状态为"已加入"(0)时才更新为"学习中"(1)
            if (myCourse.getStatus() != null && myCourse.getStatus() == 0) {
                myCourse.setStatus(1);
                myCourse.setUpdateTime(new Date());
                myCourseDao.update(myCourse);
                log.info("用户{}开始学习课程{}，状态从'已加入'更新为'学习中'", userId, courseId);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("更新学习状态失败, userId: {}, courseId: {}", userId, courseId, e);
            return false;
        }
    }

    /**
     * 检查并更新学习状态为"已完成"
     * 根据学习进度自动判断是否完成
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否更新了状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndCompleteCourse(Integer userId, Integer courseId) {
        try {
            validateId(userId, "用户ID");
            validateId(courseId, "课程ID");

            MyCourse myCourse = findByUserAndCourse(userId, courseId);
            if (myCourse == null) {
                log.warn("用户{}未加入课程{}，无法更新学习状态", userId, courseId);
                return false;
            }

            // 只有状态为"学习中"(1)时才检查是否可以更新为"已完成"(2)
            if (myCourse.getStatus() != null && myCourse.getStatus() == 1) {
                // 检查是否满足完成条件
                if (isCourseLearningCompleted(userId, courseId)) {
                    myCourse.setStatus(2);
                    myCourse.setUpdateTime(new Date());
                    myCourseDao.update(myCourse);
                    log.info("用户{}完成课程{}学习，状态从'学习中'更新为'已完成'", userId, courseId);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("检查课程完成状态失败, userId: {}, courseId: {}", userId, courseId, e);
            return false;
        }
    }

    /**
     * 判断课程是否学习完成
     * 根据观看记录、作业完成情况等综合判断
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否完成
     */
    @Override
    public boolean isCourseLearningCompleted(Integer userId, Integer courseId) {
        try {
            validateId(userId, "用户ID");
            validateId(courseId, "课程ID");

            // 1. 获取课程的所有章节和小节
            com.jieni.mqxq.domain.entity.Chapter chapterCondition = new com.jieni.mqxq.domain.entity.Chapter();
            chapterCondition.setCourseId(courseId);
            List<com.jieni.mqxq.domain.entity.Chapter> chapters = chapterDao.queryAllByLimit(chapterCondition);
            
            if (chapters.isEmpty()) {
                // 如果课程没有章节，检查是否有作业
                return checkHomeworkCompletion(userId, courseId);
            }

            // 2. 获取所有小节
            com.jieni.mqxq.domain.entity.Section sectionCondition = new com.jieni.mqxq.domain.entity.Section();
            sectionCondition.setCourseId(courseId);
            List<com.jieni.mqxq.domain.entity.Section> sections = sectionDao.queryAllByLimit(sectionCondition);
            
            if (sections.isEmpty()) {
                // 如果没有小节，检查作业完成情况
                return checkHomeworkCompletion(userId, courseId);
            }

            // 3. 检查观看记录 - 至少观看过一次课程内容
            com.jieni.mqxq.domain.entity.CourseView viewCondition = new com.jieni.mqxq.domain.entity.CourseView();
            viewCondition.setUserId(userId);
            viewCondition.setCourseId(courseId);
            List<com.jieni.mqxq.domain.entity.CourseView> viewRecords = courseViewDao.queryAllByLimit(viewCondition);
            
            if (viewRecords.isEmpty()) {
                log.debug("用户{}未观看课程{}，未完成", userId, courseId);
                return false;
            }

            // 4. 检查作业完成情况
            boolean homeworkCompleted = checkHomeworkCompletion(userId, courseId);
            
            // 5. 综合判断：有观看记录且作业已完成（如果有作业的话）
            log.debug("用户{}课程{}完成检查: 观看记录={}, 作业完成={}", 
                    userId, courseId, !viewRecords.isEmpty(), homeworkCompleted);
            
            return !viewRecords.isEmpty() && homeworkCompleted;

        } catch (Exception e) {
            log.error("判断课程完成状态失败, userId: {}, courseId: {}", userId, courseId, e);
            return false;
        }
    }

    /**
     * 检查作业完成情况
     * 如果课程有必修作业，则必须全部完成；如果没有作业，则返回true
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 作业是否完成
     */
    private boolean checkHomeworkCompletion(Integer userId, Integer courseId) {
        try {
            // 获取课程的所有作业
            Map<String, Object> homeworkParams = new HashMap<>();
            homeworkParams.put("courseId", courseId);
            List<com.jieni.mqxq.domain.entity.CourseHomework> homeworks =
                    courseHomeworkDao.queryByParams(homeworkParams);
            
            if (homeworks.isEmpty()) {
                // 没有作业，视为完成
                return true;
            }

            // 获取用户的作业提交记录
            com.jieni.mqxq.domain.entity.HomeworkSubmission submissionCondition = 
                    new com.jieni.mqxq.domain.entity.HomeworkSubmission();
            submissionCondition.setStudentId(userId);
            List<com.jieni.mqxq.domain.entity.HomeworkSubmission> submissions = 
                    homeworkSubmissionDao.queryAllByLimit(submissionCondition);
            
            // 筛选出该课程的提交记录
            Set<Integer> submittedHomeworkIds = submissions.stream()
                    .filter(s -> homeworks.stream()
                            .anyMatch(h -> h.getId().equals(s.getHomeworkId())))
                    .map(com.jieni.mqxq.domain.entity.HomeworkSubmission::getHomeworkId)
                    .collect(Collectors.toSet());

            // 检查是否所有作业都已提交
            long totalHomeworks = homeworks.size();
            long submittedCount = submittedHomeworkIds.size();
            
            log.debug("用户{}课程{}作业完成情况: {}/{}", userId, courseId, submittedCount, totalHomeworks);
            
            // 所有作业都已提交才算完成
            return submittedCount >= totalHomeworks;

        } catch (Exception e) {
            log.error("检查作业完成情况失败, userId: {}, courseId: {}", userId, courseId, e);
            // 出错时返回false，保守处理
            return false;
        }
    }

    /**
     * 重置课程学习状态为"学习中"
     * 当课程内容更新（新增章节、小节、作业）时，将已完成的用户状态重置为学习中
     *
     * @param courseId 课程ID
     * @param reason   重置原因（用于日志记录）
     * @return 重置的用户数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetCompletedStatusToLearning(Integer courseId, String reason) {
        try {
            validateId(courseId, "课程ID");

            // 查询该课程所有状态为"已完成"(2)的学习记录
            MyCourseQueryDTO queryDTO = new MyCourseQueryDTO();
            queryDTO.setCourseId(courseId);
            queryDTO.setStatus(2); // 已完成
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(Integer.MAX_VALUE);

            Map<String, Object> params = buildQueryParams(queryDTO);
            List<MyCourse> completedCourses = myCourseDao.queryListByConditions(params);

            if (completedCourses.isEmpty()) {
                log.info("课程{}没有已完成的学习记录，无需重置", courseId);
                return 0;
            }

            // 批量更新状态为"学习中"(1)
            int resetCount = 0;
            Date now = new Date();
            for (MyCourse myCourse : completedCourses) {
                myCourse.setStatus(1); // 重置为学习中
                myCourse.setUpdateTime(now);
                myCourseDao.update(myCourse);
                resetCount++;
            }

            log.info("课程{}内容更新，重置{}个用户的学习状态为'学习中'，原因: {}", 
                    courseId, resetCount, reason);

            return resetCount;

        } catch (Exception e) {
            log.error("重置课程学习状态失败, courseId: {}, reason: {}", courseId, reason, e);
            throw new MyException("重置学习状态失败: " + e.getMessage());
        }
    }
}
