package com.jieni.mqxq.service.impl.homework;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.enums.ContentType;
import com.jieni.mqxq.dao.CourseHomeworkDao;
import com.jieni.mqxq.dao.HomeworkSubmissionDao;
import com.jieni.mqxq.dao.MyCourseDao;
import com.jieni.mqxq.dao.UserDao;
import com.jieni.mqxq.dao.CourseDao;
import com.jieni.mqxq.domain.dto.homework.*;
import com.jieni.mqxq.domain.entity.CourseHomework;
import com.jieni.mqxq.domain.entity.HomeworkSubmission;
import com.jieni.mqxq.domain.entity.MyCourse;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkSubmissionVO;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.homework.CourseHomeworkService;
import com.jieni.mqxq.service.infrastructure.MinIOFileStorageService;
import com.jieni.mqxq.service.course.MyCourseService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 课程作业服务实现类
 * 
 * 提供课程作业系统的完整业务服务，包括作业管理、提交和批改等功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CourseHomeworkServiceImpl implements CourseHomeworkService {
    
    @Resource
    private CourseHomeworkDao courseHomeworkDao;
    
    @Resource
    private HomeworkSubmissionDao homeworkSubmissionDao;
    
    @Resource
    private MyCourseDao myCourseDao;
    
    @Resource
    private MinIOFileStorageService minIOFileStorageService;

    @Resource
    private MyCourseService myCourseService;
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private CourseDao courseDao;

    // ========== 基础CRUD方法 ==========

    @Override
    public CourseHomeworkVO getHomeworkById(Integer id) {
        validateHomeworkId(id);
        CourseHomework homework = courseHomeworkDao.queryById(id);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        return convertToVO(homework);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkVO createHomework(CourseHomeworkCreateDTO createDTO, Integer teacherId) {
        validateCreateDTO(createDTO);
        
        CourseHomework homework = new CourseHomework();
        BeanUtil.copyProperties(createDTO, homework);
        
        Date now = new Date();
        homework.setCreateUser(teacherId);
        homework.setCreateTime(now);
        homework.setUpdateUser(teacherId);
        homework.setUpdateTime(now);
        
        if (homework.getStatus() == null) {
            homework.setStatus(0); // 默认未发布
        }
        
        courseHomeworkDao.insert(homework);
        log.info("教师{}创建作业成功，作业ID: {}", teacherId, homework.getId());

        // 新增作业后重置已完成学员的学习状态为“学习中”
        tryResetLearningStatus(createDTO.getCourseId(), "新增作业: " + createDTO.getTitle());
        
        return convertToVO(homework);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkVO updateHomework(CourseHomeworkUpdateDTO updateDTO, Integer teacherId) {
        validateHomeworkId(updateDTO.getId());
        
        CourseHomework existing = courseHomeworkDao.queryById(updateDTO.getId());
        if (existing == null) {
            throw new MyException("作业不存在");
        }
        
        // 管理员可以操作任何作业，跳过权限检查
        if (!StpUtil.hasRole("管理员")) {
            validateTeacherPermission(teacherId, existing.getCreateUser());
        }
        
        CourseHomework homework = new CourseHomework();
        BeanUtil.copyProperties(updateDTO, homework, "createUser", "createTime");
        homework.setUpdateUser(teacherId);
        homework.setUpdateTime(new Date());
        
        courseHomeworkDao.update(homework);
        log.info("教师{}更新作业{}成功", teacherId, homework.getId());
        
        return getHomeworkById(homework.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteHomework(Integer id, Integer teacherId) {
        validateHomeworkId(id);
        
        CourseHomework homework = courseHomeworkDao.queryById(id);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        validateTeacherPermission(teacherId, homework.getCreateUser());
        
        // 检查是否有学生提交
        Map<String, Object> params = new HashMap<>();
        params.put("homeworkId", id);
        List<HomeworkSubmission> submissions = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        
        if (!submissions.isEmpty()) {
            throw new MyException("作业已有学生提交，无法删除");
        }
        
        int result = courseHomeworkDao.deleteById(id);
        log.info("教师{}删除作业{}成功", teacherId, id);
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkVO publishHomework(Integer id, Integer teacherId) {
        validateHomeworkId(id);
        
        CourseHomework homework = courseHomeworkDao.queryById(id);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        validateTeacherPermission(teacherId, homework.getCreateUser());
        validateHomeworkForPublish(homework);
        
        homework.setStatus(1);
        homework.setUpdateUser(teacherId);
        homework.setUpdateTime(new Date());
        
        courseHomeworkDao.update(homework);
        log.info("教师{}发布作业{}成功", teacherId, id);
        
        return convertToVO(homework);
    }

    // ========== 查询方法 ==========

    @Override
    public PageInfo<CourseHomeworkVO> getHomeworkPage(CourseHomeworkQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<CourseHomework> homeworkList;
        
        if (queryDTO.getCourseName() != null || queryDTO.getTeacherName() != null) {
            homeworkList = courseHomeworkDao.queryForAdmin(params);
        } else {
            homeworkList = courseHomeworkDao.queryByParams(params);
            
            // 如果查询结果中缺少courseName或teacherName，批量查询并填充
            // 收集需要查询课程名称的courseId
            Set<Integer> courseIds = homeworkList.stream()
                    .filter(h -> (h.getCourseName() == null || h.getCourseName().isEmpty()) && h.getCourseId() != null)
                    .map(CourseHomework::getCourseId)
                    .collect(Collectors.toSet());
            
            // 收集需要查询教师名称的teacherId
            Set<Integer> teacherIds = homeworkList.stream()
                    .filter(h -> (h.getTeacherName() == null || h.getTeacherName().isEmpty()) && h.getCreateUser() != null)
                    .map(CourseHomework::getCreateUser)
                    .collect(Collectors.toSet());
            
            // 批量查询课程信息
            Map<Integer, String> courseNameMap = new HashMap<>();
            for (Integer courseId : courseIds) {
                try {
                    Course course = courseDao.queryById(courseId);
                    if (course != null) {
                        courseNameMap.put(courseId, course.getTitle() != null ? course.getTitle() : "");
                    }
                } catch (Exception e) {
                    log.warn("查询课程信息失败, courseId: {}", courseId, e);
                }
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
            
            // 填充缺失的字段
            for (CourseHomework homework : homeworkList) {
                if ((homework.getCourseName() == null || homework.getCourseName().isEmpty()) 
                        && homework.getCourseId() != null) {
                    homework.setCourseName(courseNameMap.getOrDefault(homework.getCourseId(), ""));
                }
                if ((homework.getTeacherName() == null || homework.getTeacherName().isEmpty()) 
                        && homework.getCreateUser() != null) {
                    homework.setTeacherName(teacherNameMap.getOrDefault(homework.getCreateUser(), ""));
                }
            }
        }
        
        PageInfo<CourseHomework> pageInfo = new PageInfo<>(homeworkList);
        return convertToVOPage(pageInfo);
    }

    @Override
    public PageInfo<CourseHomeworkVO> getPublishedHomeworkByCourse(Integer courseId, Integer pageNum, Integer pageSize) {
        validateCourseId(courseId);
        validatePageParams(pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        
        Map<String, Object> params = new HashMap<>();
        params.put("courseId", courseId);
        params.put("status", 1); // 已发布
        
        List<CourseHomework> homeworkList = courseHomeworkDao.queryByParams(params);
        PageInfo<CourseHomework> pageInfo = new PageInfo<>(homeworkList);
        
        return convertToVOPage(pageInfo);
    }

    @Override
    public PageInfo<CourseHomeworkVO> getMyHomeworkList(Integer userId, Integer pageNum, Integer pageSize, Integer status) {
        validatePageParams(pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        
        // 查询用户购买的课程
        MyCourse myCourse = new MyCourse();
        myCourse.setUserId(userId);
        List<MyCourse> myCourses = myCourseDao.queryAllByLimit(myCourse);
        
        if (myCourses.isEmpty()) {
            return new PageInfo<>(new ArrayList<>());
        }
        
        List<Integer> courseIds = myCourses.stream()
                .map(MyCourse::getCourseId)
                .collect(Collectors.toList());
        
        Map<String, Object> params = new HashMap<>();
        params.put("courseIds", courseIds);
        params.put("status", status != null ? status : 1); // 默认查询已发布的作业
        
        List<CourseHomework> homeworkList = courseHomeworkDao.queryByParams(params);
        PageInfo<CourseHomework> pageInfo = new PageInfo<>(homeworkList);
        
        return convertToVOPage(pageInfo);
    }

    // ========== 批量操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchPublishHomework(CourseHomeworkBatchOperationDTO batchDTO, Integer teacherId) {
        List<Integer> homeworkIds = batchDTO.getHomeworkIds();
        
        int count = 0;
        for (Integer homeworkId : homeworkIds) {
            try {
                CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
                if (homework != null && homework.getCreateUser().equals(teacherId)) {
                    homework.setStatus(1);
                    homework.setUpdateUser(teacherId);
                    homework.setUpdateTime(new Date());
                    courseHomeworkDao.update(homework);
                    count++;
                }
            } catch (Exception e) {
                log.error("批量发布作业失败，作业ID: {}", homeworkId, e);
            }
        }
        
        log.info("教师{}批量发布作业完成，成功{}个", teacherId, count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteHomework(CourseHomeworkBatchOperationDTO batchDTO, Integer teacherId) {
        List<Integer> homeworkIds = batchDTO.getHomeworkIds();
        
        int count = 0;
        for (Integer homeworkId : homeworkIds) {
            try {
                CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
                if (homework != null && homework.getCreateUser().equals(teacherId)) {
                    // 检查是否有提交记录
                    Map<String, Object> params = new HashMap<>();
                    params.put("homeworkId", homeworkId);
                    List<HomeworkSubmission> submissions = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
                    
                    if (submissions.isEmpty()) {
                        courseHomeworkDao.deleteById(homeworkId);
                        count++;
                    }
                }
            } catch (Exception e) {
                log.error("批量删除作业失败，作业ID: {}", homeworkId, e);
            }
        }
        
        log.info("教师{}批量删除作业完成，成功{}个", teacherId, count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUnpublishHomework(CourseHomeworkBatchOperationDTO batchDTO) {
        List<Integer> homeworkIds = batchDTO.getHomeworkIds();
        int result = courseHomeworkDao.batchUpdateStatus(homeworkIds, 0, null);
        
        log.info("批量取消发布作业完成，成功{}个", result);
        return result;
    }

    // ========== 高级功能 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkVO copyHomework(Integer homeworkId, Integer teacherId) {
        validateHomeworkId(homeworkId);
        
        CourseHomework original = courseHomeworkDao.queryById(homeworkId);
        if (original == null) {
            throw new MyException("原作业不存在");
        }
        
        validateTeacherPermission(teacherId, original.getCreateUser());
        
        CourseHomework newHomework = new CourseHomework();
        BeanUtil.copyProperties(original, newHomework, "id", "createTime", "updateTime", "createUser", "updateUser");
        
        newHomework.setTitle(original.getTitle() + " (副本)");
        newHomework.setStatus(0); // 草稿状态
        
        Date now = new Date();
        newHomework.setCreateUser(teacherId);
        newHomework.setCreateTime(now);
        newHomework.setUpdateUser(teacherId);
        newHomework.setUpdateTime(now);
        
        // 延后7天截止
        if (original.getEndTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(original.getEndTime());
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            newHomework.setEndTime(calendar.getTime());
        }
        
        courseHomeworkDao.insert(newHomework);
        log.info("教师{}复制作业成功，原作业ID: {}, 新作业ID: {}", teacherId, homeworkId, newHomework.getId());
        
        return convertToVO(newHomework);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkVO extendDeadline(CourseHomeworkUpdateDTO updateDTO, Integer teacherId) {
        validateHomeworkId(updateDTO.getId());
        
        if (updateDTO.getEndTime() == null) {
            throw new MyException("新的截止时间不能为空");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(updateDTO.getId());
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        validateTeacherPermission(teacherId, homework.getCreateUser());
        
        Date now = new Date();
        if (updateDTO.getEndTime().before(now)) {
            throw new MyException("新的截止时间不能早于当前时间");
        }
        
        if (homework.getEndTime() != null && updateDTO.getEndTime().before(homework.getEndTime())) {
            throw new MyException("新的截止时间不能早于原截止时间");
        }
        
        homework.setEndTime(updateDTO.getEndTime());
        homework.setUpdateUser(teacherId);
        homework.setUpdateTime(now);
        
        courseHomeworkDao.update(homework);
        log.info("教师{}延长作业{}截止时间成功", teacherId, homework.getId());
        
        return convertToVO(homework);
    }

    @Override
    public List<CourseHomeworkVO> getUpcomingDeadlineHomework(Integer teacherId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusDays(days);
        
        List<CourseHomework> homeworkList = courseHomeworkDao.queryUpcomingDeadline(
                teacherId, 1, now, futureTime);
        
        return homeworkList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ========== 统计分析 ==========

    @Override
    public Map<String, Object> getTeacherHomeworkStatistics(Integer teacherId, Integer courseId) {
        Map<String, Object> params = new HashMap<>();
        params.put("createUser", teacherId);
        if (courseId != null) {
            params.put("courseId", courseId);
        }
        
        List<CourseHomework> homeworkList = courseHomeworkDao.queryByParams(params);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalHomework", homeworkList.size());
        statistics.put("publishedHomework", homeworkList.stream().filter(h -> h.getStatus() == 1).count());
        statistics.put("draftHomework", homeworkList.stream().filter(h -> h.getStatus() == 0).count());
        
        // 提交统计
        int totalSubmissions = 0;
        int gradedSubmissions = 0;
        
        for (CourseHomework homework : homeworkList) {
            Map<String, Object> subParams = new HashMap<>();
            subParams.put("homeworkId", homework.getId());
            List<HomeworkSubmission> submissions = homeworkSubmissionDao.querySubmissionsWithStudentInfo(subParams);
            totalSubmissions += submissions.size();
            gradedSubmissions += submissions.stream().filter(s -> s.getStatus() == 1).count();
        }
        
        statistics.put("totalSubmissions", totalSubmissions);
        statistics.put("gradedSubmissions", gradedSubmissions);
        statistics.put("pendingGrading", totalSubmissions - gradedSubmissions);
        
        // 即将截止的作业
        Date threeDaysLater = new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L);
        Date now = new Date();
        long upcomingDeadlines = homeworkList.stream()
                .filter(h -> h.getEndTime() != null && h.getEndTime().after(now) && h.getEndTime().before(threeDaysLater))
                .count();
        statistics.put("upcomingDeadlines", upcomingDeadlines);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getAdminHomeworkStatistics() {
        return courseHomeworkDao.getHomeworkStatistics();
    }

    @Override
    public List<Map<String, Object>> getTeacherHomeworkRank() {
        return courseHomeworkDao.getTeacherHomeworkRank();
    }

    @Override
    public List<Map<String, Object>> getCourseHomeworkRank() {
        return courseHomeworkDao.getCourseHomeworkRank();
    }

    @Override
    public List<Map<String, Object>> getHomeworkTrendAnalysis(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return courseHomeworkDao.getHomeworkTrendAnalysis(startDate);
    }

    // ========== 作业提交 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitHomework(CourseHomeworkSubmitDTO submitDTO, Integer userId) {
        validateHomeworkId(submitDTO.getHomeworkId());
        
        CourseHomework homework = courseHomeworkDao.queryById(submitDTO.getHomeworkId());
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        if (homework.getStatus() != 1) {
            throw new MyException("作业未发布");
        }
        
        if (homework.getEndTime() != null && homework.getEndTime().before(new Date())) {
            throw new MyException("作业已过截止时间");
        }
        
        // 检查用户权限
        if (!checkUserCoursePermission(userId, homework.getCourseId())) {
            throw new MyException("无权限提交该作业");
        }
        
        // 检查是否已提交
        HomeworkSubmission existing = homeworkSubmissionDao.queryByHomeworkAndStudent(
                submitDTO.getHomeworkId(), userId);
        if (existing != null) {
            throw new MyException("已提交过该作业，请使用更新功能");
        }
        
        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomeworkId(submitDTO.getHomeworkId());
        submission.setStudentId(userId);
        submission.setContent(submitDTO.getContent());
        submission.setAttachmentUrl(submitDTO.getAttachmentUrl());
        submission.setStatus(0); // 待批改
        
        Date now = new Date();
        submission.setSubmitTime(now);
        submission.setCreateTime(now);
        submission.setCreateUser(userId);
        submission.setUpdateTime(now);
        submission.setUpdateUser(userId);
        
        homeworkSubmissionDao.insert(submission);
        log.info("用户{}提交作业{}成功", userId, submitDTO.getHomeworkId());
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateHomeworkSubmission(CourseHomeworkSubmitDTO submitDTO, Integer userId) {
        validateHomeworkId(submitDTO.getHomeworkId());
        
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(
                submitDTO.getHomeworkId(), userId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        if (submission.getStatus() == 1) {
            throw new MyException("作业已被批改，无法修改");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(submitDTO.getHomeworkId());
        if (homework != null && homework.getEndTime() != null && homework.getEndTime().before(new Date())) {
            throw new MyException("作业已过截止时间，无法修改");
        }
        
        submission.setContent(submitDTO.getContent());
        submission.setAttachmentUrl(submitDTO.getAttachmentUrl());
        submission.setUpdateTime(new Date());
        submission.setUpdateUser(userId);
        
        homeworkSubmissionDao.update(submission);
        log.info("用户{}更新作业{}提交成功", userId, submitDTO.getHomeworkId());
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawHomeworkSubmission(Integer homeworkId, Integer userId) {
        validateHomeworkId(homeworkId);
        
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, userId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        if (submission.getStatus() == 1) {
            throw new MyException("作业已被批改，无法撤回");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
        if (homework != null && homework.getEndTime() != null && homework.getEndTime().before(new Date())) {
            throw new MyException("作业已过截止时间，无法撤回");
        }
        
        homeworkSubmissionDao.deleteById(submission.getId());
        log.info("用户{}撤回作业{}提交成功", userId, homeworkId);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadHomeworkAttachment(Integer homeworkId, Integer userId, MultipartFile file) {
        validateHomeworkId(homeworkId);
        
        if (file == null || file.isEmpty()) {
            throw new MyException("文件不能为空");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        if (!checkUserCoursePermission(userId, homework.getCourseId())) {
            throw new MyException("无权限上传该作业文件");
        }
        
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "homework_" + homeworkId + "_" + userId + "_" + System.currentTimeMillis() + extension;
            
            String fileUrl = minIOFileStorageService.uploadFile(filename, file.getInputStream(), ContentType.DEFAULT);
            log.info("用户{}上传作业{}文件成功: {}", userId, homeworkId, fileUrl);
            
            return fileUrl;
        } catch (Exception e) {
            log.error("上传作业文件失败，用户ID: {}, 作业ID: {}", userId, homeworkId, e);
            throw new MyException("上传作业文件失败: " + e.getMessage());
        }
    }

    @Override
    public PageInfo<CourseHomeworkSubmissionVO> getUserSubmissions(Integer userId, Integer pageNum, Integer pageSize, Integer courseId) {
        validatePageParams(pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", userId);
        if (courseId != null) {
            params.put("courseId", courseId);
        }
        
        List<HomeworkSubmission> submissions = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        PageInfo<HomeworkSubmission> pageInfo = new PageInfo<>(submissions);
        
        return convertSubmissionToVOPage(pageInfo);
    }

    @Override
    public Map<String, Object> getUserSubmissionDetail(Integer homeworkId, Integer userId) {
        validateHomeworkId(homeworkId);
        
        Map<String, Object> result = new HashMap<>();
        
        CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        result.put("homework", convertToVO(homework));
        
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, userId);
        if (submission == null) {
            result.put("hasSubmitted", false);
            result.put("canSubmit", homework.getEndTime() == null || new Date().before(homework.getEndTime()));
        } else {
            result.put("hasSubmitted", true);
            result.put("canSubmit", false);
            result.put("submission", convertSubmissionToVO(submission));
        }
        
        return result;
    }

    // ========== 作业批改 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean gradeHomework(CourseHomeworkGradeDTO gradeDTO, Integer teacherId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryById(gradeDTO.getSubmissionId());
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(submission.getHomeworkId());
        if (homework == null || !homework.getCreateUser().equals(teacherId)) {
            throw new MyException("无权限批改该作业");
        }
        
        submission.setScore(gradeDTO.getScore());
        submission.setTeacherComment(gradeDTO.getTeacherComment());
        submission.setGradeUser(teacherId);
        submission.setGradeTime(new Date());
        submission.setStatus(1); // 已批改
        submission.setUpdateUser(teacherId);
        submission.setUpdateTime(new Date());
        
        homeworkSubmissionDao.update(submission);
        log.info("教师{}批改提交记录{}成功", teacherId, gradeDTO.getSubmissionId());
        
        return true;
    }

    @Override
    public PageInfo<CourseHomeworkSubmissionVO> getHomeworkSubmissions(Integer homeworkId, Integer pageNum, Integer pageSize, 
                                                                      Integer status, String studentName) {
        validateHomeworkId(homeworkId);
        validatePageParams(pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        
        Map<String, Object> params = new HashMap<>();
        params.put("homeworkId", homeworkId);
        if (status != null) {
            params.put("status", status);
        }
        if (studentName != null && !studentName.trim().isEmpty()) {
            params.put("studentName", studentName.trim());
        }
        
        List<HomeworkSubmission> submissions = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        PageInfo<HomeworkSubmission> pageInfo = new PageInfo<>(submissions);
        
        return convertSubmissionToVOPage(pageInfo);
    }

    @Override
    public Map<String, Object> getSubmissionDetailForTeacher(Integer submissionId, Integer teacherId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryById(submissionId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(submission.getHomeworkId());
        if (homework == null || !homework.getCreateUser().equals(teacherId)) {
            throw new MyException("无权限查看该提交");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("submission", convertSubmissionToVO(submission));
        result.put("homework", convertToVO(homework));
        
        return result;
    }

    @Override
    public Map<String, Object> getHomeworkSubmissionStatistics(Integer homeworkId) {
        validateHomeworkId(homeworkId);
        return homeworkSubmissionDao.getSubmissionStats(homeworkId);
    }

    @Override
    public void exportHomeworkData(CourseHomeworkQueryDTO queryDTO, HttpServletResponse response) {
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<CourseHomework> homeworkList = courseHomeworkDao.queryByParams(params);
        
        // 批量查询教师名称，避免在循环中逐个查询
        Set<Integer> teacherIds = homeworkList.stream()
                .map(CourseHomework::getCreateUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
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
        
        // 批量查询课程名称，避免在循环中逐个查询
        Set<Integer> courseIds = homeworkList.stream()
                .map(CourseHomework::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Integer, String> courseNameMap = new HashMap<>();
        for (Integer courseId : courseIds) {
            try {
                Course course = courseDao.queryById(courseId);
                if (course != null) {
                    courseNameMap.put(courseId, course.getTitle() != null ? course.getTitle() : "");
                }
            } catch (Exception e) {
                log.warn("查询课程信息失败, courseId: {}", courseId, e);
            }
        }
        
        List<Map<String, Object>> exportData = homeworkList.stream().map(homework -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", homework.getId());
            data.put("title", homework.getTitle());
            data.put("content", homework.getContent());
            data.put("courseId", homework.getCourseId());
            // 优先使用查询结果中的courseName，如果为空则从courseNameMap获取
            String courseName = homework.getCourseName();
            if (courseName == null || courseName.isEmpty()) {
                courseName = courseNameMap.getOrDefault(homework.getCourseId(), "");
            }
            data.put("courseName", courseName);
            data.put("score", homework.getScore());
            data.put("startTime", homework.getStartTime());
            data.put("endTime", homework.getEndTime());
            data.put("status", homework.getStatus() != null && homework.getStatus() == 1 ? "已发布" : "未发布");
            data.put("createTime", homework.getCreateTime());
            data.put("createUser", homework.getCreateUser());
            // 优先使用查询结果中的teacherName，如果为空则从teacherNameMap获取
            String teacherName = homework.getTeacherName();
            if (teacherName == null || teacherName.isEmpty()) {
                teacherName = teacherNameMap.getOrDefault(homework.getCreateUser(), "");
            }
            data.put("teacherName", teacherName);
            return data;
        }).collect(Collectors.toList());

        List<String> headers = Arrays.asList(
            "作业ID", "作业标题", "作业内容", "课程ID", "课程名称",
            "满分", "开始时间", "截止时间", "状态", "创建时间", "创建人ID", "教师姓名"
        );
        List<String> dataKeys = Arrays.asList(
            "id", "title", "content", "courseId", "courseName",
            "score", "startTime", "endTime", "status", "createTime", "createUser", "teacherName"
        );

        String fileName = com.jieni.mqxq.util.ExcelExportUtil.generateFileName("作业数据");
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + fileName + ".xlsx";

        com.jieni.mqxq.util.ExcelExportUtil.exportToFile(
            filePath, "作业数据", headers, dataKeys, exportData
        );
        com.jieni.mqxq.util.ExcelExportUtil.writeFileToResponseAndDelete(response, filePath, fileName);
        log.info("作业数据导出成功并已触发下载, 文件路径: {}", filePath);
    }

    @Override
    public void exportHomeworkSubmissions(Integer homeworkId, HttpServletResponse response) {
        validateHomeworkId(homeworkId);
        
        Map<String, Object> params = new HashMap<>();
        params.put("homeworkId", homeworkId);
        
        List<HomeworkSubmission> submissions = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        
        List<Map<String, Object>> exportData = submissions.stream().map(submission -> {
            Map<String, Object> data = new HashMap<>();
            data.put("studentId", submission.getStudentId());
            data.put("studentName", submission.getStudentName() != null ? submission.getStudentName() : "未知");
            data.put("homeworkTitle", submission.getHomeworkTitle() != null ? submission.getHomeworkTitle() : "");
            data.put("content", submission.getContent() != null ? submission.getContent() : "");
            data.put("attachmentUrl", submission.getAttachmentUrl() != null ? submission.getAttachmentUrl() : "");
            data.put("score", submission.getScore() != null ? submission.getScore() : "");
            data.put("maxScore", submission.getMaxScore() != null ? submission.getMaxScore() : "");
            data.put("teacherComment", submission.getTeacherComment() != null ? submission.getTeacherComment() : "");
            data.put("status", submission.getStatus() != null && submission.getStatus() == 1 ? "已批改" : "待批改");
            data.put("submitTime", submission.getSubmitTime());
            data.put("gradeTime", submission.getGradeTime());
            return data;
        }).collect(Collectors.toList());

        List<String> headers = Arrays.asList(
            "学生ID", "学生姓名", "作业标题", "提交内容", "附件URL",
            "得分", "满分", "教师评语", "状态", "提交时间", "批改时间"
        );
        List<String> dataKeys = Arrays.asList(
            "studentId", "studentName", "homeworkTitle", "content", "attachmentUrl",
            "score", "maxScore", "teacherComment", "status", "submitTime", "gradeTime"
        );

        String fileName = com.jieni.mqxq.util.ExcelExportUtil.generateFileName("作业提交记录");
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + fileName + ".xlsx";

        com.jieni.mqxq.util.ExcelExportUtil.exportToFile(
            filePath, "作业提交记录", headers, dataKeys, exportData
        );
        com.jieni.mqxq.util.ExcelExportUtil.writeFileToResponseAndDelete(response, filePath, fileName);
        log.info("作业提交记录导出成功并已触发下载, 文件路径: {}", filePath);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 验证作业ID
     */
    private void validateHomeworkId(Integer id) {
        if (id == null || id <= 0) {
            throw new MyException("作业ID无效");
        }
    }

    /**
     * 验证课程ID
     */
    private void validateCourseId(Integer courseId) {
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
    }

    /**
     * 验证分页参数
     */
    private void validatePageParams(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            throw new MyException("页码必须大于0");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new MyException("每页大小必须在1-100之间");
        }
    }

    /**
     * 验证教师权限
     */
    private void validateTeacherPermission(Integer teacherId, Integer createUser) {
        if (!teacherId.equals(createUser)) {
            throw new MyException("无权限操作该作业");
        }
    }

    /**
     * 验证创建DTO
     */
    private void validateCreateDTO(CourseHomeworkCreateDTO createDTO) {
        if (createDTO.getStartTime().after(createDTO.getEndTime())) {
            throw new MyException("开始时间不能晚于截止时间");
        }
    }

    /**
     * 验证作业是否可以发布
     */
    private void validateHomeworkForPublish(CourseHomework homework) {
        if (homework.getTitle() == null || homework.getTitle().trim().isEmpty()) {
            throw new MyException("作业标题不能为空");
        }
        if (homework.getStartTime() == null || homework.getEndTime() == null) {
            throw new MyException("开始时间和截止时间不能为空");
        }
    }

    /**
     * 检查用户课程权限
     */
    private boolean checkUserCoursePermission(Integer userId, Integer courseId) {
        MyCourse condition = new MyCourse();
        condition.setUserId(userId);
        condition.setCourseId(courseId);
        long count = myCourseDao.count(condition);
        return count > 0;
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
     * 构建查询参数
     */
    private Map<String, Object> buildQueryParams(CourseHomeworkQueryDTO queryDTO) {
        Map<String, Object> params = new HashMap<>();
        
        if (queryDTO.getId() != null) {
            params.put("id", queryDTO.getId());
        }
        if (queryDTO.getCourseId() != null) {
            params.put("courseId", queryDTO.getCourseId());
        }
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            params.put("title", queryDTO.getTitle().trim());
        }
        if (queryDTO.getStatus() != null) {
            params.put("status", queryDTO.getStatus());
        }
        if (queryDTO.getCreateUser() != null) {
            params.put("createUser", queryDTO.getCreateUser());
        }
        if (queryDTO.getCourseName() != null && !queryDTO.getCourseName().trim().isEmpty()) {
            params.put("courseName", queryDTO.getCourseName().trim());
        }
        if (queryDTO.getTeacherName() != null && !queryDTO.getTeacherName().trim().isEmpty()) {
            params.put("teacherName", queryDTO.getTeacherName().trim());
        }
        if (queryDTO.getOrderBy() != null && !queryDTO.getOrderBy().trim().isEmpty()) {
            params.put("orderBy", queryDTO.getOrderBy().trim());
        }
        
        return params;
    }

    /**
     * 转换Entity到VO
     */
    private CourseHomeworkVO convertToVO(CourseHomework homework) {
        CourseHomeworkVO vo = new CourseHomeworkVO();
        BeanUtil.copyProperties(homework, vo);
        
        // 处理submissionCount字段类型转换
        if (homework.getSubmissionCount() != null) {
            try {
                vo.setSubmissionCount(Integer.parseInt(homework.getSubmissionCount()));
            } catch (NumberFormatException e) {
                vo.setSubmissionCount(0);
            }
        }
        
        return vo;
    }

    /**
     * 转换分页结果
     */
    private PageInfo<CourseHomeworkVO> convertToVOPage(PageInfo<CourseHomework> pageInfo) {
        PageInfo<CourseHomeworkVO> voPageInfo = new PageInfo<>();
        BeanUtil.copyProperties(pageInfo, voPageInfo, "list");
        
        List<CourseHomeworkVO> voList = pageInfo.getList().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPageInfo.setList(voList);
        
        return voPageInfo;
    }

    /**
     * 转换提交记录Entity到VO
     */
    private CourseHomeworkSubmissionVO convertSubmissionToVO(HomeworkSubmission submission) {
        CourseHomeworkSubmissionVO vo = new CourseHomeworkSubmissionVO();
        BeanUtil.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 转换提交记录分页结果
     */
    private PageInfo<CourseHomeworkSubmissionVO> convertSubmissionToVOPage(PageInfo<HomeworkSubmission> pageInfo) {
        PageInfo<CourseHomeworkSubmissionVO> voPageInfo = new PageInfo<>();
        BeanUtil.copyProperties(pageInfo, voPageInfo, "list");
        
        List<CourseHomeworkSubmissionVO> voList = pageInfo.getList().stream()
                .map(this::convertSubmissionToVO)
                .collect(Collectors.toList());
        voPageInfo.setList(voList);
        
        return voPageInfo;
    }

    // ========== 权限验证和辅助方法 ==========

    /**
     * 检查教师对作业的权限
     */
    @Override
    public boolean checkTeacherHomeworkPermission(Integer teacherId, Integer homeworkId) {
        if (teacherId == null || homeworkId == null) {
            throw new MyException("教师ID或作业ID不能为空");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        // 检查是否是作业创建者
        if (!homework.getCreateUser().equals(teacherId)) {
            throw new MyException("您没有权限操作该作业");
        }
        
        return true;
    }

    /**
     * 检查用户是否可以提交作业
     */
    @Override
    public boolean checkUserSubmissionPermission(Integer userId, Integer courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        
        // 检查用户是否购买了课程
        MyCourse myCourse = myCourseDao.queryByCourseAndUser(courseId, userId);
        if (myCourse == null) {
            throw new MyException("您未购买该课程，无法提交作业");
        }
        
        return true;
    }

    /**
     * 检查用户是否已提交作业
     */
    @Override
    public boolean hasUserSubmitted(Integer userId, Integer homeworkId) {
        if (userId == null || homeworkId == null) {
            return false;
        }
        
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, userId);
        return submission != null;
    }

    /**
     * 根据ID获取作业实体
     */
    @Override
    public CourseHomework getById(Integer homeworkId) {
        if (homeworkId == null || homeworkId <= 0) {
            throw new MyException("作业ID无效");
        }
        
        CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        return homework;
    }

    /**
     * 上传作业文件
     */
    @Override
    public String uploadHomeworkFile(Integer userId, Integer homeworkId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MyException("文件不能为空");
        }
        
        try {
            // 使用MinIO上传文件
            String fileName = "homework/" + homeworkId + "/" + userId + "/" + 
                            System.currentTimeMillis() + "_" + file.getOriginalFilename();
            return minIOFileStorageService.uploadFile(fileName, file.getInputStream(), ContentType.DEFAULT);
        } catch (Exception e) {
            log.error("上传作业文件失败", e);
            throw new MyException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 批改作业（兼容旧方法签名）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean gradeHomework(Integer homeworkId, Integer userId, Integer score, String feedback, Integer teacherId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, userId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        // 验证教师权限
        checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        // 设置批改信息
        submission.setScore(new BigDecimal(score));
        submission.setTeacherComment(feedback);
        submission.setGradeTime(new Date());
        submission.setGradeUser(teacherId);
        submission.setStatus(1); // 已批改
        submission.setUpdateTime(new Date());
        submission.setUpdateUser(teacherId);
        
        return homeworkSubmissionDao.update(submission) > 0;
    }

    /**
     * 批量批改作业（兼容旧方法签名）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGradeHomework(Integer homeworkId, List<Map<String, Object>> gradings, Integer teacherId) {
        // 验证教师权限
        checkTeacherHomeworkPermission(teacherId, homeworkId);
        
        int successCount = 0;
        Date now = new Date();
        
        for (Map<String, Object> grading : gradings) {
            try {
                Integer userId = (Integer) grading.get("userId");
                Integer score = (Integer) grading.get("score");
                String feedback = (String) grading.get("feedback");
                
                HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, userId);
                if (submission == null) {
                    log.warn("学生未提交作业, userId: {}, homeworkId: {}", userId, homeworkId);
                    continue;
                }
                
                submission.setScore(new BigDecimal(score));
                submission.setTeacherComment(feedback);
                submission.setGradeTime(now);
                submission.setGradeUser(teacherId);
                submission.setStatus(1);
                submission.setUpdateTime(now);
                submission.setUpdateUser(teacherId);
                
                if (homeworkSubmissionDao.update(submission) > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批改作业失败", e);
            }
        }
        
        return successCount;
    }

    /**
     * 获取学生提交详情（教师端）
     */
    @Override
    public Map<String, Object> getStudentSubmissionDetail(Integer homeworkId, Integer userId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, userId);
        if (submission == null) {
            throw new MyException("学生未提交该作业");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("submission", submission);
        
        // 获取作业信息
        CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
        result.put("homework", homework);
        
        return result;
    }

    /**
     * 获取未提交学生列表
     */
    @Override
    public List<Map<String, Object>> getUnsubmittedStudents(Integer homeworkId) {
        // 获取作业信息
        CourseHomework homework = courseHomeworkDao.queryById(homeworkId);
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        // 获取所有购买了该课程的学生
        List<MyCourse> myCourses = myCourseDao.queryByCourseId(homework.getCourseId());
        
        // 获取已提交的学生ID列表
        List<HomeworkSubmission> submissions = homeworkSubmissionDao.queryByHomeworkId(homeworkId);
        Set<Integer> submittedStudentIds = submissions.stream()
                .map(HomeworkSubmission::getStudentId)
                .collect(Collectors.toSet());
        
        // 筛选出未提交的学生
        List<Map<String, Object>> unsubmittedStudents = new ArrayList<>();
        for (MyCourse myCourse : myCourses) {
            if (!submittedStudentIds.contains(myCourse.getUserId())) {
                Map<String, Object> student = new HashMap<>();
                student.put("userId", myCourse.getUserId());
                // TODO: 查询用户信息获取学生姓名
                unsubmittedStudents.add(student);
            }
        }
        
        return unsubmittedStudents;
    }
}
