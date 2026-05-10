package com.jieni.mqxq.service.impl.dashboard;

import com.jieni.mqxq.dao.*;
import com.jieni.mqxq.domain.entity.*;
import com.jieni.mqxq.domain.vo.dashboard.*;
import com.jieni.mqxq.service.dashboard.DashboardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据大屏服务实现类
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Resource
    private CourseDao courseDao;
    
    @Resource
    private OrdersDao ordersDao;
    
    @Resource
    private CourseCommentDao courseCommentDao;
    
    @Resource
    private MyCourseDao myCourseDao;
    
    @Resource
    private CourseHomeworkDao courseHomeworkDao;
    
    @Resource
    private HomeworkSubmissionDao homeworkSubmissionDao;
    
    @Resource
    private CourseViewDao courseViewDao;
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private UserRoleDao userRoleDao;
    
    @Resource
    private RoleDao roleDao;
    
    @Resource
    private CourseSubjectDao courseSubjectDao;
    
    @Resource
    private ChapterDao chapterDao;
    
    @Resource
    private SectionDao sectionDao;
    
    @Resource
    private AnnouncementsDao announcementsDao;
    
    // ========== 教师端实现 ==========
    
    @Override
    public TeacherDashboardOverviewVO getTeacherOverview(Integer teacherId) {
        log.info("获取教师端数据大屏概览, teacherId: {}", teacherId);
        
        TeacherDashboardOverviewVO overview = new TeacherDashboardOverviewVO();
        
        // 课程相关指标
        Course courseCondition = new Course();
        courseCondition.setTeacherId(teacherId);
        List<Course> courses = courseDao.queryAllByLimit(courseCondition);
        
        overview.setTotalCourses(courses.size());
        overview.setPublishedCourses((int) courses.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count());
        overview.setUnpublishedCourses((int) courses.stream().filter(c -> c.getStatus() == null || c.getStatus() == 0).count());
        overview.setTotalViews(courses.stream().mapToInt(c -> c.getViewCount() != null ? c.getViewCount() : 0).sum());
        
        // 课程评论统计
        CourseComment commentCondition = new CourseComment();
        commentCondition.setTeacherId(teacherId);
        List<CourseComment> comments = courseCommentDao.queryAllByLimit(commentCondition);
        overview.setTotalComments(comments.size());
        
        if (!comments.isEmpty()) {
            double avgRating = comments.stream()
                    .mapToInt(c -> c.getScore() != null ? c.getScore() : 5)
                    .average()
                    .orElse(0.0);
            overview.setAvgRating(Math.round(avgRating * 100.0) / 100.0);
        } else {
            overview.setAvgRating(0.0);
        }
        
        // 收入统计
        BigDecimal totalRevenue = ordersDao.getRevenueByTeacherId(teacherId);
        overview.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // 今日和本月收入（需要从orders表查询）
        BigDecimal todayRevenue = calculateTodayRevenue(teacherId);
        BigDecimal monthRevenue = calculateMonthRevenue(teacherId);
        overview.setTodayRevenue(todayRevenue);
        overview.setMonthRevenue(monthRevenue);
        overview.setCumulativeRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // 今日订单数
        overview.setTodayOrderCount(calculateTodayOrderCount(teacherId));
        
        // 学员相关指标
        List<Integer> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        if (!courseIds.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("courseIds", courseIds);
            List<MyCourse> myCourses = myCourseDao.queryListByConditions(params);
            
            Set<Integer> uniqueStudents = myCourses.stream()
                    .map(MyCourse::getUserId)
                    .collect(Collectors.toSet());
            
            overview.setTotalStudents(uniqueStudents.size());
            overview.setStudyingStudents((int) myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 1).count());
            overview.setCompletedStudents((int) myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 2).count());
            overview.setJoinedStudents((int) myCourses.stream().filter(mc -> mc.getStatus() == null || mc.getStatus() == 0).count());
            
            // 计算完成率
            if (overview.getTotalStudents() > 0) {
                double completionRate = (double) overview.getCompletedStudents() / overview.getTotalStudents() * 100;
                overview.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
            } else {
                overview.setCompletionRate(0.0);
            }
        } else {
            overview.setTotalStudents(0);
            overview.setStudyingStudents(0);
            overview.setCompletedStudents(0);
            overview.setJoinedStudents(0);
            overview.setCompletionRate(0.0);
        }
        
        // 作业相关指标
        Map<String, Object> homeworkParams = new HashMap<>();
        homeworkParams.put("createUser", teacherId);
        List<CourseHomework> homeworks = courseHomeworkDao.queryByParams(homeworkParams);
        
        overview.setTotalHomeworks(homeworks.size());
        overview.setPublishedHomeworks((int) homeworks.stream().filter(h -> h.getStatus() != null && h.getStatus() == 1).count());
        
        // 待批改和已批改作业数
        List<Integer> homeworkIds = homeworks.stream().map(CourseHomework::getId).collect(Collectors.toList());
        if (!homeworkIds.isEmpty()) {
            HomeworkSubmission submissionCondition = new HomeworkSubmission();
            List<HomeworkSubmission> submissions = homeworkSubmissionDao.queryAllByLimit(submissionCondition);
            
            List<HomeworkSubmission> teacherSubmissions = submissions.stream()
                    .filter(s -> homeworkIds.contains(s.getHomeworkId()))
                    .collect(Collectors.toList());
            
            overview.setPendingGradingCount((int) teacherSubmissions.stream().filter(s -> s.getStatus() == null || s.getStatus() == 0).count());
            overview.setGradedCount((int) teacherSubmissions.stream().filter(s -> s.getStatus() != null && s.getStatus() == 1).count());
            
            // 计算平均分
            List<HomeworkSubmission> gradedSubmissions = teacherSubmissions.stream()
                    .filter(s -> s.getStatus() != null && s.getStatus() == 1 && s.getScore() != null)
                    .collect(Collectors.toList());
            
            if (!gradedSubmissions.isEmpty()) {
                double avgScore = gradedSubmissions.stream()
                        .mapToDouble(s -> s.getScore().doubleValue())
                        .average()
                        .orElse(0.0);
                overview.setAvgHomeworkScore(Math.round(avgScore * 100.0) / 100.0);
            } else {
                overview.setAvgHomeworkScore(0.0);
            }
        } else {
            overview.setPendingGradingCount(0);
            overview.setGradedCount(0);
            overview.setAvgHomeworkScore(0.0);
        }
        
        return overview;
    }
    
    @Override
    public List<DistributionDataVO> getTeacherCourseStatusDistribution(Integer teacherId) {
        Course condition = new Course();
        condition.setTeacherId(teacherId);
        List<Course> courses = courseDao.queryAllByLimit(condition);
        
        long published = courses.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count();
        long unpublished = courses.size() - published;
        
        List<DistributionDataVO> result = new ArrayList<>();
        if (courses.size() > 0) {
            result.add(new DistributionDataVO("已发布", published, Math.round((double) published / courses.size() * 10000) / 100.0));
            result.add(new DistributionDataVO("未发布", unpublished, Math.round((double) unpublished / courses.size() * 10000) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<DistributionDataVO> getTeacherStudentStatusDistribution(Integer teacherId) {
        Course courseCondition = new Course();
        courseCondition.setTeacherId(teacherId);
        List<Course> courses = courseDao.queryAllByLimit(courseCondition);
        
        List<Integer> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("courseIds", courseIds);
        List<MyCourse> myCourses = myCourseDao.queryListByConditions(params);
        
        long joined = myCourses.stream().filter(mc -> mc.getStatus() == null || mc.getStatus() == 0).count();
        long studying = myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 1).count();
        long completed = myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 2).count();
        
        List<DistributionDataVO> result = new ArrayList<>();
        if (myCourses.size() > 0) {
            double total = myCourses.size();
            result.add(new DistributionDataVO("已加入", joined, Math.round(joined / total * 10000) / 100.0));
            result.add(new DistributionDataVO("学习中", studying, Math.round(studying / total * 10000) / 100.0));
            result.add(new DistributionDataVO("已完成", completed, Math.round(completed / total * 10000) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<TrendDataVO> getTeacherCourseViewTrend(Integer teacherId, Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        Course courseCondition = new Course();
        courseCondition.setTeacherId(teacherId);
        List<Course> courses = courseDao.queryAllByLimit(courseCondition);
        List<Integer> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        
        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        CourseView viewCondition = new CourseView();
        List<CourseView> views = courseViewDao.queryAllByLimit(viewCondition);
        
        // 筛选该教师的课程浏览记录
        List<CourseView> teacherViews = views.stream()
                .filter(v -> courseIds.contains(v.getCourseId()))
                .collect(Collectors.toList());
        
        // 按日期分组统计
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();
        
        Map<String, Long> dateCountMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (CourseView view : teacherViews) {
            if (view.getViewTime() != null && view.getViewTime().after(startDate)) {
                String date = sdf.format(view.getViewTime());
                dateCountMap.put(date, dateCountMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        return dateCountMap.entrySet().stream()
                .map(e -> new TrendDataVO(e.getKey(), e.getValue(), null, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TrendDataVO> getTeacherRevenueTrend(Integer teacherId, Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        // 使用OrdersDao的查询方法
        List<Map<String, Object>> revenueData = ordersDao.getTeacherRevenueTrend(teacherId, days);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        return revenueData.stream()
                .map(data -> {
                    String date;
                    Object dateObj = data.get("date");
                    if (dateObj instanceof java.sql.Date) {
                        date = sdf.format((java.sql.Date) dateObj);
                    } else if (dateObj instanceof Date) {
                        date = sdf.format((Date) dateObj);
                    } else if (dateObj instanceof String) {
                        date = (String) dateObj;
                    } else {
                        date = dateObj != null ? dateObj.toString() : "";
                    }
                    
                    BigDecimal amount = (BigDecimal) data.get("revenue");
                    return new TrendDataVO(date, null, amount != null ? amount : BigDecimal.ZERO, null);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TrendDataVO> getTeacherStudentGrowthTrend(Integer teacherId, Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        Course courseCondition = new Course();
        courseCondition.setTeacherId(teacherId);
        List<Course> courses = courseDao.queryAllByLimit(courseCondition);
        List<Integer> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        
        if (courseIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("courseIds", courseIds);
        List<MyCourse> myCourses = myCourseDao.queryListByConditions(params);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();
        
        Map<String, Long> dateCountMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (MyCourse mc : myCourses) {
            if (mc.getCreateTime() != null && mc.getCreateTime().after(startDate)) {
                String date = sdf.format(mc.getCreateTime());
                dateCountMap.put(date, dateCountMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        return dateCountMap.entrySet().stream()
                .map(e -> new TrendDataVO(e.getKey(), e.getValue(), null, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RankingDataVO> getTeacherTopCourses(Integer teacherId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        List<Map<String, Object>> topCourses = ordersDao.getTopCoursesByTeacherId(teacherId, limit);
        
        List<RankingDataVO> result = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> data : topCourses) {
            RankingDataVO vo = new RankingDataVO();
            vo.setRank(rank++);
            vo.setId((Integer) data.get("courseId"));
            vo.setName((String) data.get("courseName"));
            vo.setImageUrl((String) data.get("courseImg"));
            vo.setCount(((Number) data.get("orderCount")).longValue());
            vo.setAmount((BigDecimal) data.get("totalRevenue"));
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public List<RankingDataVO> getTeacherCourseCompletionRanking(Integer teacherId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        Course courseCondition = new Course();
        courseCondition.setTeacherId(teacherId);
        List<Course> courses = courseDao.queryAllByLimit(courseCondition);
        
        List<RankingDataVO> result = new ArrayList<>();
        int rank = 1;
        
        for (Course course : courses) {
            Map<String, Object> params = new HashMap<>();
            params.put("courseId", course.getId());
            List<MyCourse> myCourses = myCourseDao.queryListByConditions(params);
            
            if (myCourses.isEmpty()) {
                continue;
            }
            
            long total = myCourses.size();
            long completed = myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 2).count();
            double completionRate = total > 0 ? (double) completed / total * 100 : 0.0;
            
            RankingDataVO vo = new RankingDataVO();
            vo.setRank(rank++);
            vo.setId(course.getId());
            vo.setName(course.getTitle());
            vo.setImageUrl(course.getCover());
            vo.setCount(total);
            vo.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
            result.add(vo);
            
            if (result.size() >= limit) {
                break;
            }
        }
        
        // 按完成率排序
        result.sort((a, b) -> Double.compare(
                b.getCompletionRate() != null ? b.getCompletionRate() : 0.0,
                a.getCompletionRate() != null ? a.getCompletionRate() : 0.0
        ));
        
        // 重新设置排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        
        return result;
    }
    
    // ========== 管理员端实现 ==========
    
    @Override
    public AdminDashboardOverviewVO getAdminOverview() {
        log.info("获取管理员端数据大屏概览");
        
        AdminDashboardOverviewVO overview = new AdminDashboardOverviewVO();
        
        // 用户统计
        User userCondition = new User();
        List<User> users = userDao.queryAllByLimit(userCondition);
        overview.setTotalUsers(users.size());
        
        // 角色统计
        List<UserRole> userRoles = userRoleDao.queryAllByLimit(new UserRole());
        Map<Integer, Long> roleCountMap = userRoles.stream()
                .collect(Collectors.groupingBy(UserRole::getRoleId, Collectors.counting()));
        
        overview.setTotalTeachers(roleCountMap.getOrDefault(2, 0L).intValue());
        overview.setTotalStudents(roleCountMap.getOrDefault(3, 0L).intValue());
        
        // 今日新增用户
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();
        
        long todayNewUsers = users.stream()
                .filter(u -> u.getCreateTime() != null && u.getCreateTime().after(todayStart))
                .count();
        overview.setTodayNewUsers((int) todayNewUsers);
        
        // 课程统计
        List<Course> courses = courseDao.queryAllByLimit(new Course());
        overview.setTotalCourses(courses.size());
        overview.setPublishedCourses((int) courses.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count());
        
        // 订单统计
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        overview.setTotalOrders(orders.size());
        overview.setCompletedOrders((int) orders.stream().filter(o -> "DONE".equals(o.getStatus())).count());
        
        // 今日新增订单（复用上面定义的 todayStart 变量）
        long todayNewOrders = orders.stream()
                .filter(o -> {
                    if (o.getCreateTime() == null) {
                        return false;
                    }
                    try {
                        // 尝试解析两种可能的日期格式
                        Date createDate = null;
                        try {
                            // 格式1: yyyy-MM-dd HH:mm:ss
                            createDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getCreateTime());
                        } catch (Exception e1) {
                            try {
                                // 格式2: EEE MMM dd HH:mm:ss zzz yyyy (Java Date.toString()格式)
                                createDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(o.getCreateTime());
                            } catch (Exception e2) {
                                // 如果两种格式都解析失败，返回false
                                return false;
                            }
                        }
                        // 判断订单创建时间是否在今天（大于等于今天00:00:00）
                        return createDate != null && !createDate.before(todayStart);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
        overview.setTodayNewOrders((int) todayNewOrders);
        
        // 收入统计
        BigDecimal totalRevenue = ordersDao.getTotalRevenue();
        overview.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        BigDecimal todayRevenue = calculateAdminTodayRevenue();
        overview.setTodayRevenue(todayRevenue);
        
        // 学习统计
        List<MyCourse> myCourses = myCourseDao.queryAllByLimit(new MyCourse());
        overview.setTotalLearningRecords(myCourses.size());
        overview.setStudyingRecords((int) myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 1).count());
        overview.setCompletedRecords((int) myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 2).count());
        
        if (myCourses.size() > 0) {
            double completionRate = (double) overview.getCompletedRecords() / myCourses.size() * 100;
            overview.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        } else {
            overview.setCompletionRate(0.0);
        }
        
        // 浏览量统计
        List<CourseView> views = courseViewDao.queryAllByLimit(new CourseView());
        overview.setTotalViews(views.size());
        Set<Integer> uniqueVisitors = views.stream()
                .map(CourseView::getUserId)
                .collect(Collectors.toSet());
        overview.setUniqueVisitors(uniqueVisitors.size());
        
        // 内容统计
        overview.setTotalHomeworks(courseHomeworkDao.queryByParams(new HashMap<>()).size());
        overview.setTotalSubmissions(homeworkSubmissionDao.queryAllByLimit(new HomeworkSubmission()).size());
        overview.setTotalComments(courseCommentDao.queryAllByLimit(new CourseComment()).size());
        overview.setTotalAnnouncements(announcementsDao.queryAllByLimit(new Announcements()).size());
        overview.setTotalChapters(chapterDao.queryAllByLimit(new Chapter()).size());
        overview.setTotalSections(sectionDao.queryAllByLimit(new Section()).size());
        
        return overview;
    }
    
    @Override
    public List<DistributionDataVO> getAdminUserRoleDistribution() {
        List<UserRole> userRoles = userRoleDao.queryAllByLimit(new UserRole());
        List<Role> roles = roleDao.queryAll();
        
        Map<Integer, String> roleNameMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, Role::getRoleName));
        
        Map<Integer, Long> roleCountMap = userRoles.stream()
                .collect(Collectors.groupingBy(UserRole::getRoleId, Collectors.counting()));
        
        long total = userRoles.size();
        List<DistributionDataVO> result = new ArrayList<>();
        
        for (Map.Entry<Integer, Long> entry : roleCountMap.entrySet()) {
            String roleName = roleNameMap.getOrDefault(entry.getKey(), "未知");
            long count = entry.getValue();
            double percentage = total > 0 ? (double) count / total * 100 : 0.0;
            result.add(new DistributionDataVO(roleName, count, Math.round(percentage * 100.0) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<DistributionDataVO> getAdminUserStatusDistribution() {
        List<User> users = userDao.queryAllByLimit(new User());
        
        long enabled = users.stream().filter(u -> u.getStatus() != null && u.getStatus() == 1).count();
        long disabled = users.size() - enabled;
        
        List<DistributionDataVO> result = new ArrayList<>();
        if (users.size() > 0) {
            double total = users.size();
            result.add(new DistributionDataVO("启用", enabled, Math.round(enabled / total * 10000) / 100.0));
            result.add(new DistributionDataVO("禁用", disabled, Math.round(disabled / total * 10000) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<DistributionDataVO> getAdminCourseCategoryDistribution() {
        List<Course> courses = courseDao.queryAllByLimit(new Course());
        List<CourseSubject> subjects = courseSubjectDao.queryAllByLimit(new CourseSubject());
        
        Map<Integer, String> subjectNameMap = subjects.stream()
                .collect(Collectors.toMap(CourseSubject::getId, CourseSubject::getName));
        
        Map<Integer, Long> categoryCountMap = courses.stream()
                .filter(c -> c.getSubjectId() != null)
                .collect(Collectors.groupingBy(Course::getSubjectId, Collectors.counting()));
        
        List<DistributionDataVO> result = new ArrayList<>();
        long total = courses.size();
        
        for (Map.Entry<Integer, Long> entry : categoryCountMap.entrySet()) {
            String categoryName = subjectNameMap.getOrDefault(entry.getKey(), "未分类");
            long count = entry.getValue();
            double percentage = total > 0 ? (double) count / total * 100 : 0.0;
            result.add(new DistributionDataVO(categoryName, count, Math.round(percentage * 100.0) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<DistributionDataVO> getAdminCourseStatusDistribution() {
        List<Course> courses = courseDao.queryAllByLimit(new Course());
        
        long published = courses.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count();
        long unpublished = courses.size() - published;
        
        List<DistributionDataVO> result = new ArrayList<>();
        if (courses.size() > 0) {
            double total = courses.size();
            result.add(new DistributionDataVO("已发布", published, Math.round(published / total * 10000) / 100.0));
            result.add(new DistributionDataVO("未发布", unpublished, Math.round(unpublished / total * 10000) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<DistributionDataVO> getAdminOrderStatusDistribution() {
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        
        Map<String, Long> statusCountMap = orders.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(Orders::getStatus, Collectors.counting()));
        
        Map<String, String> statusNameMap = new HashMap<>();
        statusNameMap.put("CANCEL", "已取消");
        statusNameMap.put("NOT_PAY", "待支付");
        statusNameMap.put("DONE", "已完成");
        statusNameMap.put("REFUND_DONE", "已退款");
        statusNameMap.put("COMMENT_DONE", "已评价");
        
        List<DistributionDataVO> result = new ArrayList<>();
        long total = orders.size();
        
        for (Map.Entry<String, Long> entry : statusCountMap.entrySet()) {
            String statusName = statusNameMap.getOrDefault(entry.getKey(), entry.getKey());
            long count = entry.getValue();
            double percentage = total > 0 ? (double) count / total * 100 : 0.0;
            result.add(new DistributionDataVO(statusName, count, Math.round(percentage * 100.0) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<DistributionDataVO> getAdminLearningStatusDistribution() {
        List<MyCourse> myCourses = myCourseDao.queryAllByLimit(new MyCourse());
        
        long joined = myCourses.stream().filter(mc -> mc.getStatus() == null || mc.getStatus() == 0).count();
        long studying = myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 1).count();
        long completed = myCourses.stream().filter(mc -> mc.getStatus() != null && mc.getStatus() == 2).count();
        
        List<DistributionDataVO> result = new ArrayList<>();
        if (myCourses.size() > 0) {
            double total = myCourses.size();
            result.add(new DistributionDataVO("已加入", joined, Math.round(joined / total * 10000) / 100.0));
            result.add(new DistributionDataVO("学习中", studying, Math.round(studying / total * 10000) / 100.0));
            result.add(new DistributionDataVO("已完成", completed, Math.round(completed / total * 10000) / 100.0));
        }
        
        return result;
    }
    
    @Override
    public List<TrendDataVO> getAdminUserRegistrationTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        List<User> users = userDao.queryAllByLimit(new User());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();
        
        Map<String, Long> dateCountMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (User user : users) {
            if (user.getCreateTime() != null && user.getCreateTime().after(startDate)) {
                String date = sdf.format(user.getCreateTime());
                dateCountMap.put(date, dateCountMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        return dateCountMap.entrySet().stream()
                .map(e -> new TrendDataVO(e.getKey(), e.getValue(), null, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TrendDataVO> getAdminOrderRevenueTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        List<Orders> doneOrders = orders.stream()
                .filter(o -> "DONE".equals(o.getStatus()) && o.getPayTime() != null)
                .collect(Collectors.toList());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();
        
        Map<String, BigDecimal> dateAmountMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Orders order : doneOrders) {
            try {
                Date payDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(order.getPayTime());
                if (payDate.after(startDate)) {
                    String date = sdf.format(payDate);
                    BigDecimal amount = order.getGoodsPrice() != null ? order.getGoodsPrice() : BigDecimal.ZERO;
                    dateAmountMap.put(date, dateAmountMap.getOrDefault(date, BigDecimal.ZERO).add(amount));
                }
            } catch (Exception e) {
                log.warn("解析支付时间失败: {}", order.getPayTime());
            }
        }
        
        return dateAmountMap.entrySet().stream()
                .map(e -> new TrendDataVO(e.getKey(), null, e.getValue(), null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TrendDataVO> getAdminOrderCountTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();
        
        Map<String, Long> dateCountMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Orders order : orders) {
            if (order.getCreateTime() != null) {
                try {
                    Date createDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(order.getCreateTime());
                    if (createDate.after(startDate)) {
                        String date = sdf.format(createDate);
                        dateCountMap.put(date, dateCountMap.getOrDefault(date, 0L) + 1);
                    }
                } catch (Exception e) {
                    log.warn("解析订单创建时间失败: {}", order.getCreateTime());
                }
            }
        }
        
        return dateCountMap.entrySet().stream()
                .map(e -> new TrendDataVO(e.getKey(), e.getValue(), null, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TrendDataVO> getAdminCourseCreationTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        
        List<Course> courses = courseDao.queryAllByLimit(new Course());
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date endDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();
        
        Map<String, Long> dateCountMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        // 初始化所有日期为0
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(startDate);
        while (!dateCal.getTime().after(endDate)) {
            String dateStr = sdf.format(dateCal.getTime());
            dateCountMap.put(dateStr, 0L);
            dateCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 统计实际创建的课程
        for (Course course : courses) {
            if (course.getCreateTime() != null) {
                Date createTime = course.getCreateTime();
                // 重置时间部分为0，只比较日期
                Calendar createCal = Calendar.getInstance();
                createCal.setTime(createTime);
                createCal.set(Calendar.HOUR_OF_DAY, 0);
                createCal.set(Calendar.MINUTE, 0);
                createCal.set(Calendar.SECOND, 0);
                createCal.set(Calendar.MILLISECOND, 0);
                Date createDate = createCal.getTime();
                
                // 检查是否在日期范围内（包括边界）
                if (!createDate.before(startDate) && !createDate.after(endDate)) {
                    String date = sdf.format(createDate);
                    dateCountMap.put(date, dateCountMap.getOrDefault(date, 0L) + 1);
                }
            }
        }
        
        return dateCountMap.entrySet().stream()
                .map(e -> new TrendDataVO(e.getKey(), e.getValue(), BigDecimal.ZERO, 0.0))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RankingDataVO> getAdminTopCoursesByViews(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        List<Course> courses = courseDao.queryAllByLimit(new Course());
        
        List<RankingDataVO> result = courses.stream()
                .sorted((a, b) -> Integer.compare(
                        b.getViewCount() != null ? b.getViewCount() : 0,
                        a.getViewCount() != null ? a.getViewCount() : 0
                ))
                .limit(limit)
                .map(course -> {
                    RankingDataVO vo = new RankingDataVO();
                    vo.setId(course.getId());
                    vo.setName(course.getTitle());
                    vo.setImageUrl(course.getCover());
                    vo.setCount((long) (course.getViewCount() != null ? course.getViewCount() : 0));
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 设置排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        
        return result;
    }
    
    @Override
    public List<RankingDataVO> getAdminTopCoursesByRevenue(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        List<Orders> doneOrders = orders.stream()
                .filter(o -> "DONE".equals(o.getStatus()))
                .collect(Collectors.toList());
        
        Map<Integer, CourseRevenueInfo> courseRevenueMap = new HashMap<>();
        
        for (Orders order : doneOrders) {
            if (order.getGoodsId() != null) {
                CourseRevenueInfo info = courseRevenueMap.getOrDefault(order.getGoodsId(), new CourseRevenueInfo());
                info.orderCount++;
                info.totalRevenue = info.totalRevenue.add(order.getGoodsPrice() != null ? order.getGoodsPrice() : BigDecimal.ZERO);
                courseRevenueMap.put(order.getGoodsId(), info);
            }
        }
        
        List<Course> courses = courseDao.queryAllByLimit(new Course());
        Map<Integer, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        
        List<RankingDataVO> result = courseRevenueMap.entrySet().stream()
                .map(entry -> {
                    Course course = courseMap.get(entry.getKey());
                    if (course == null) {
                        return null;
                    }
                    
                    RankingDataVO vo = new RankingDataVO();
                    vo.setId(course.getId());
                    vo.setName(course.getTitle());
                    vo.setImageUrl(course.getCover());
                    vo.setCount((long) entry.getValue().orderCount);
                    vo.setAmount(entry.getValue().totalRevenue);
                    return vo;
                })
                .filter(vo -> vo != null)
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .limit(limit)
                .collect(Collectors.toList());
        
        // 设置排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        
        return result;
    }
    
    @Override
    public List<RankingDataVO> getAdminTopTeachers(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }
        
        // 获取所有教师
        List<UserRole> teacherRoles = userRoleDao.queryAllByLimit(new UserRole()).stream()
                .filter(ur -> ur.getRoleId() != null && ur.getRoleId() == 2)
                .collect(Collectors.toList());
        
        List<Integer> teacherIds = teacherRoles.stream()
                .map(UserRole::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        List<User> users = userDao.queryAllByLimit(new User());
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        List<RankingDataVO> result = new ArrayList<>();
        
        for (Integer teacherId : teacherIds) {
            User teacher = userMap.get(teacherId);
            if (teacher == null) {
                continue;
            }
            
            BigDecimal revenue = ordersDao.getRevenueByTeacherId(teacherId);
            if (revenue == null || revenue.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            
            RankingDataVO vo = new RankingDataVO();
            vo.setId(teacherId);
            vo.setName(teacher.getName());
            vo.setAmount(revenue);
            
            // 统计课程数和学员数
            Course courseCondition = new Course();
            courseCondition.setTeacherId(teacherId);
            List<Course> courses = courseDao.queryAllByLimit(courseCondition);
            vo.setCount((long) courses.size());
            
            result.add(vo);
        }
        
        // 按收入排序
        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        
        // 限制数量并设置排名
        result = result.stream().limit(limit).collect(Collectors.toList());
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        
        return result;
    }
    
    // ========== 私有辅助方法 ==========
    
    private BigDecimal calculateTodayRevenue(Integer teacherId) {
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        return orders.stream()
                .filter(o -> o.getStatus() != null && "DONE".equals(o.getStatus()))
                .filter(o -> {
                    if (o.getPayTime() == null) return false;
                    try {
                        Date payDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getPayTime());
                        String payDateStr = new SimpleDateFormat("yyyy-MM-dd").format(payDate);
                        return today.equals(payDateStr);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(o -> {
                    // 检查订单对应的课程是否属于该教师
                    if (o.getGoodsId() == null) return false;
                    Course course = courseDao.queryById(o.getGoodsId());
                    return course != null && course.getTeacherId() != null && course.getTeacherId().equals(teacherId);
                })
                .map(o -> o.getGoodsPrice() != null ? o.getGoodsPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateMonthRevenue(Integer teacherId) {
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        int currentYear = cal.get(Calendar.YEAR);
        
        return orders.stream()
                .filter(o -> o.getStatus() != null && "DONE".equals(o.getStatus()))
                .filter(o -> {
                    if (o.getPayTime() == null) return false;
                    try {
                        Date payDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getPayTime());
                        cal.setTime(payDate);
                        return cal.get(Calendar.MONTH) + 1 == currentMonth && cal.get(Calendar.YEAR) == currentYear;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(o -> {
                    if (o.getGoodsId() == null) return false;
                    Course course = courseDao.queryById(o.getGoodsId());
                    return course != null && course.getTeacherId() != null && course.getTeacherId().equals(teacherId);
                })
                .map(o -> o.getGoodsPrice() != null ? o.getGoodsPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private Integer calculateTodayOrderCount(Integer teacherId) {
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        return (int) orders.stream()
                .filter(o -> o.getStatus() != null && "DONE".equals(o.getStatus()))
                .filter(o -> {
                    if (o.getPayTime() == null) return false;
                    try {
                        Date payDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getPayTime());
                        String payDateStr = new SimpleDateFormat("yyyy-MM-dd").format(payDate);
                        return today.equals(payDateStr);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(o -> {
                    if (o.getGoodsId() == null) return false;
                    Course course = courseDao.queryById(o.getGoodsId());
                    return course != null && course.getTeacherId() != null && course.getTeacherId().equals(teacherId);
                })
                .count();
    }
    
    private BigDecimal calculateAdminTodayRevenue() {
        List<Orders> orders = ordersDao.queryAllByLimit(new Orders());
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        return orders.stream()
                .filter(o -> o.getStatus() != null && "DONE".equals(o.getStatus()))
                .filter(o -> {
                    if (o.getPayTime() == null) return false;
                    try {
                        Date payDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getPayTime());
                        String payDateStr = new SimpleDateFormat("yyyy-MM-dd").format(payDate);
                        return today.equals(payDateStr);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(o -> o.getGoodsPrice() != null ? o.getGoodsPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // 内部类：课程收入信息
    private static class CourseRevenueInfo {
        int orderCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
    }
}

