package com.jieni.mqxq.service.impl.course;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CourseCommentDao;
import com.jieni.mqxq.dao.CourseDao;
import com.jieni.mqxq.dao.MyCourseDao;
import com.jieni.mqxq.dao.UserDao;
import com.jieni.mqxq.domain.dto.comment.*;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.entity.CourseComment;
import com.jieni.mqxq.domain.entity.MyCourse;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.domain.vo.comment.*;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.CourseCommentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程评论服务实现类
 * 
 * 提供课程评论系统的完整业务功能，支持多角色评论管理和统计分析。
 * 实现评论的发表、查询、审核、统计等核心功能，确保评论系统的完整性和安全性。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CourseCommentServiceImpl implements CourseCommentService {
    
    @Resource
    private CourseCommentDao courseCommentDao;
    
    @Resource
    private CourseDao courseDao;
    
    @Resource
    private MyCourseDao myCourseDao;
    
    @Resource
    private UserDao userDao;

    // ========== 用户端方法 ==========

    /**
     * 用户发表课程评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Integer userId, CommentCreateDTO dto) {
        log.info("用户发表课程评论, userId: {}, courseId: {}, score: {}", 
                userId, dto.getCourseId(), dto.getScore());
        
        // 检查用户是否已评论过该课程
        if (hasUserCommented(userId, dto.getCourseId())) {
            throw new MyException("您已经评论过该课程");
        }
        
        // 检查用户是否有权限评论（是否加入了该课程）
        if (!checkCommentPermission(userId, dto.getCourseId())) {
            throw new MyException("您需要先加入课程才能评论");
        }
        
        // 查询课程信息
        Course course = courseDao.queryById(dto.getCourseId());
        if (course == null) {
            throw new MyException("课程不存在");
        }
        
        // 查询用户信息
        User user = userDao.queryById(userId);
        if (user == null) {
            throw new MyException("用户不存在");
        }
        
        // 构建评论实体
        CourseComment comment = new CourseComment();
        comment.setCourseId(dto.getCourseId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setScore(dto.getScore());
        comment.setTeacherId(course.getTeacherId());
        comment.setStatus(1); // 默认显示
        
        Date now = new Date();
        comment.setCreateTime(now);
        comment.setUpdateTime(now);
        
        courseCommentDao.insert(comment);
        log.info("用户{}发表课程{}评论成功", userId, dto.getCourseId());
        
        return convertToVO(comment);
    }

    /**
     * 用户修改自己的评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO updateCommentByUser(Integer userId, Integer commentId, CommentUpdateDTO dto) {
        log.info("用户修改评论, userId: {}, commentId: {}", userId, commentId);
        
        CourseComment comment = courseCommentDao.queryById(commentId);
        if (comment == null) {
            throw new MyException("评论不存在");
        }
        
        // 验证权限
        if (!comment.getUserId().equals(userId)) {
            throw new MyException("无权限修改该评论");
        }
        
        comment.setContent(dto.getContent());
        comment.setScore(dto.getScore());
        comment.setUpdateTime(new Date());
        
        courseCommentDao.update(comment);
        log.info("用户{}修改评论{}成功", userId, commentId);
        
        return convertToVO(courseCommentDao.queryById(commentId));
    }

    /**
     * 用户删除自己的评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByUser(Integer userId, Integer commentId) {
        log.info("用户删除评论, userId: {}, commentId: {}", userId, commentId);
        
        CourseComment comment = courseCommentDao.queryById(commentId);
        if (comment == null) {
            throw new MyException("评论不存在");
        }
        
        // 验证权限
        if (!comment.getUserId().equals(userId)) {
            throw new MyException("无权限删除该评论");
        }
        
        if (courseCommentDao.deleteById(commentId) <= 0) {
            throw new MyException("评论删除失败");
        }
        
        log.info("用户{}删除评论{}成功", userId, commentId);
    }

    /**
     * 获取课程评论分页列表（用户可见）
     */
    @Override
    public PageInfo<CommentVO> getCourseCommentsPage(CommentQueryDTO queryDTO) {
        log.info("获取课程评论列表, courseId: {}, page: {}, size: {}", 
                queryDTO.getCourseId(), queryDTO.getPage(), queryDTO.getSize());
        
        if (queryDTO.getCourseId() == null) {
            throw new MyException("课程ID不能为空");
        }
        
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        
            Map<String, Object> params = new HashMap<>();
        params.put("courseId", queryDTO.getCourseId());
            params.put("status", 1); // 只显示可见的评论
        params.put("orderBy", convertOrderBy(queryDTO.getOrderBy()));
        
        List<CourseComment> list = courseCommentDao.queryByParams(params);
        PageInfo<CourseComment> pageInfo = new PageInfo<>(list);
        
        return convertPageInfo(pageInfo);
    }

    /**
     * 获取用户的评论分页列表
     */
    @Override
    public PageInfo<CommentVO> getUserCommentsPage(Integer userId, CommentQueryDTO queryDTO) {
        log.info("获取用户评论列表, userId: {}, page: {}, size: {}", 
                userId, queryDTO.getPage(), queryDTO.getSize());
        
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
        
        List<CourseComment> list = courseCommentDao.queryByParams(params);
        PageInfo<CourseComment> pageInfo = new PageInfo<>(list);
        
        return convertPageInfo(pageInfo);
    }

    /**
     * 获取课程评分统计
     */
    @Override
    public CommentRatingVO getCourseRatingStatistics(Integer courseId) {
        log.info("获取课程评分统计, courseId: {}", courseId);
        
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        
            Map<String, Object> params = new HashMap<>();
            params.put("courseId", courseId);
            params.put("status", 1); // 只统计可见的评论
        
        List<CourseComment> comments = courseCommentDao.queryByParams(params);
        
        CommentRatingVO vo = new CommentRatingVO();
        vo.setTotalComments(comments.size());
            
            if (comments.isEmpty()) {
            vo.setAverageRating(0.0);
            vo.setRatingDistribution(new int[]{0, 0, 0, 0, 0});
            return vo;
            }
            
            // 计算平均评分
            double averageRating = comments.stream()
                    .mapToInt(CourseComment::getScore)
                    .average()
                    .orElse(0.0);
        vo.setAverageRating(Math.round(averageRating * 100.0) / 100.0);
            
            // 评分分布统计
        int[] distribution = new int[5];
            for (CourseComment comment : comments) {
                if (comment.getScore() >= 1 && comment.getScore() <= 5) {
                    distribution[comment.getScore() - 1]++;
                }
            }
        vo.setRatingDistribution(distribution);
        
        return vo;
    }

    /**
     * 检查用户评论状态
     */
    @Override
    public Map<String, Object> checkUserCommentStatus(Integer userId, Integer courseId) {
        log.info("检查用户评论状态, userId: {}, courseId: {}", userId, courseId);
        
        boolean hasCommented = hasUserCommented(userId, courseId);
        CommentVO comment = null;
        
        if (hasCommented) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("courseId", courseId);
            List<CourseComment> comments = courseCommentDao.queryByParams(params);
            if (!comments.isEmpty()) {
                comment = convertToVO(comments.get(0));
                // 填充扩展字段
                fillCommentExtendFields(comment);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("hasCommented", hasCommented);
        result.put("comment", comment);
        return result;
    }
    
    /**
     * 填充评论VO的扩展字段（课程名称、教师名称、用户名称、用户头像）
     */
    private void fillCommentExtendFields(CommentVO vo) {
        if (vo == null) {
            return;
        }
        
        // 填充课程名称
        if (vo.getCourseName() == null && vo.getCourseId() != null) {
            try {
                Course course = courseDao.queryById(vo.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getTitle());
                }
            } catch (Exception e) {
                log.warn("查询课程信息失败, courseId: {}", vo.getCourseId(), e);
            }
        }
        
        // 填充教师名称
        if (vo.getTeacherName() == null && vo.getTeacherId() != null) {
            try {
                User teacher = userDao.queryById(vo.getTeacherId());
                if (teacher != null) {
                    vo.setTeacherName(teacher.getUsername() != null ? teacher.getUsername() : "");
                }
            } catch (Exception e) {
                log.warn("查询教师信息失败, teacherId: {}", vo.getTeacherId(), e);
            }
        }
        
        // 填充用户名称和头像（仅在 JOIN 查询未获取到时才查询）
        if (vo.getUserId() != null && (vo.getUserName() == null || vo.getUserAvatar() == null)) {
            try {
                User user = userDao.queryById(vo.getUserId());
                if (user != null) {
                    if (vo.getUserName() == null || vo.getUserName().isEmpty()) {
                        // 使用 name 字段作为用户名（与 JOIN 查询保持一致）
                        vo.setUserName(user.getName() != null ? user.getName() : "");
                    }
                    if (vo.getUserAvatar() == null || vo.getUserAvatar().isEmpty()) {
                        vo.setUserAvatar(user.getAvatar() != null ? user.getAvatar() : "");
                    }
                }
            } catch (Exception e) {
                log.warn("查询用户信息失败, userId: {}", vo.getUserId(), e);
            }
        }
    }

    // ========== 教师端方法 ==========

    /**
     * 教师获取自己所有课程的评论分页列表
     */
    @Override
    public PageInfo<CommentVO> getTeacherCoursesCommentsPage(Integer teacherId, CommentQueryDTO queryDTO) {
        log.info("教师查看课程评论, teacherId: {}, page: {}, size: {}", 
                teacherId, queryDTO.getPage(), queryDTO.getSize());
        
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        
        Map<String, Object> params = new HashMap<>();
        params.put("teacherId", teacherId);
        if (queryDTO.getCourseId() != null) {
            params.put("courseId", queryDTO.getCourseId());
        }
        if (queryDTO.getScore() != null) {
            params.put("score", queryDTO.getScore());
        }
        if (queryDTO.getStatus() != null) {
            params.put("status", queryDTO.getStatus());
        }
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            params.put("keyword", "%" + queryDTO.getKeyword().trim() + "%");
        }
        
        List<CourseComment> list = courseCommentDao.queryCommentsForTeacher(params);
        PageInfo<CourseComment> pageInfo = new PageInfo<>(list);
        
        return convertPageInfo(pageInfo);
    }

    /**
     * 教师获取指定课程的评论分页列表
     */
    @Override
    public PageInfo<CommentVO> getTeacherCourseCommentsPage(Integer teacherId, Integer courseId, CommentQueryDTO queryDTO) {
        log.info("教师查看指定课程评论, teacherId: {}, courseId: {}, page: {}, size: {}", 
                teacherId, courseId, queryDTO.getPage(), queryDTO.getSize());
        
        // 验证教师权限
        if (!checkTeacherCoursePermission(teacherId, courseId)) {
            throw new MyException("您没有权限查看该课程评论");
        }
        
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        
            Map<String, Object> params = new HashMap<>();
        params.put("courseId", courseId);
        if (queryDTO.getScore() != null) {
            params.put("score", queryDTO.getScore());
        }
        params.put("orderBy", convertOrderBy(queryDTO.getOrderBy()));
        
        List<CourseComment> list = courseCommentDao.queryByParams(params);
        PageInfo<CourseComment> pageInfo = new PageInfo<>(list);
        
        return convertPageInfo(pageInfo);
    }

    /**
     * 教师获取评论详情
     */
    @Override
    public CommentVO getCommentDetailForTeacher(Integer teacherId, Integer commentId) {
        log.info("教师查看评论详情, teacherId: {}, commentId: {}", teacherId, commentId);
        
        CourseComment comment = courseCommentDao.queryById(commentId);
        if (comment == null) {
            throw new MyException("评论不存在");
        }
        
        // 验证教师权限
        if (!checkTeacherCoursePermission(teacherId, comment.getCourseId())) {
            throw new MyException("您没有权限查看该评论");
        }
        
        return convertToVO(comment);
    }

    /**
     * 教师获取自己所有课程的评分统计
     */
    @Override
    public CommentStatisticsVO getTeacherCoursesRatingStatistics(Integer teacherId) {
        log.info("教师获取课程评分统计, teacherId: {}", teacherId);
        
            Map<String, Object> params = new HashMap<>();
            params.put("teacherId", teacherId);
        List<CourseComment> comments = courseCommentDao.queryCommentsForTeacher(params);
            
        CommentStatisticsVO vo = new CommentStatisticsVO();
        vo.setTotalComments((long) comments.size());
            
            if (comments.isEmpty()) {
            vo.setAverageRating(0.0);
            vo.setTotalCourses(0);
            vo.setRecentComments(0L);
            return vo;
            }
            
            // 计算平均评分
            double averageRating = comments.stream()
                    .mapToInt(CourseComment::getScore)
                    .average()
                    .orElse(0.0);
        vo.setAverageRating(Math.round(averageRating * 100.0) / 100.0);
            
            // 统计涉及的课程数量
            Set<Integer> uniqueCourses = comments.stream()
                    .map(CourseComment::getCourseId)
                    .collect(Collectors.toSet());
        vo.setTotalCourses(uniqueCourses.size());
            
            // 最近7天的评论数量
            Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
            long recentComments = comments.stream()
                    .filter(c -> c.getCreateTime() != null && c.getCreateTime().after(sevenDaysAgo))
                    .count();
        vo.setRecentComments(recentComments);
        
        return vo;
    }

    /**
     * 教师获取指定课程的评分统计
     */
    @Override
    public CommentRatingVO getTeacherCourseRatingStatistics(Integer teacherId, Integer courseId) {
        log.info("教师获取指定课程评分统计, teacherId: {}, courseId: {}", teacherId, courseId);
        
        // 验证教师权限
        if (!checkTeacherCoursePermission(teacherId, courseId)) {
            throw new MyException("您没有权限查看该课程评分统计");
        }
        
        return getCourseRatingStatistics(courseId);
    }

    /**
     * 教师获取最新评论列表
     */
    @Override
    public List<CommentVO> getLatestCommentsForTeacher(Integer teacherId, Integer limit) {
        log.info("教师获取最新评论, teacherId: {}, limit: {}", teacherId, limit);
        
            Map<String, Object> params = new HashMap<>();
            params.put("teacherId", teacherId);
            params.put("limit", limit);
            params.put("orderBy", "create_time DESC");
            
        List<CourseComment> comments = courseCommentDao.queryCommentsForTeacher(params);
        return convertToVOList(comments);
    }

    /**
     * 教师获取评论趋势分析
     */
    @Override
    public Map<String, Object> getCommentTrendAnalysisForTeacher(Integer teacherId, CommentQueryDTO queryDTO) {
        log.info("教师获取评论趋势分析, teacherId: {}, courseId: {}, days: {}", 
                teacherId, queryDTO.getCourseId(), queryDTO.getDays());
        
        if (queryDTO.getCourseId() != null) {
            // 验证教师权限
            if (!checkTeacherCoursePermission(teacherId, queryDTO.getCourseId())) {
                throw new MyException("您没有权限查看该课程评论趋势");
            }
        }
        
            Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -queryDTO.getDays());
            Date startDate = cal.getTime();
        
            Map<String, Object> params = new HashMap<>();
            params.put("teacherId", teacherId);
        if (queryDTO.getCourseId() != null) {
            params.put("courseId", queryDTO.getCourseId());
        }
            params.put("startDate", startDate);
            
        return courseCommentDao.getCommentTrendAnalysis(params);
    }

    /**
     * 教师导出课程评论
     */
    @Override
    public void exportCourseCommentsForTeacher(Integer teacherId, Integer courseId, HttpServletResponse response) {
        log.info("教师导出课程评论, teacherId: {}, courseId: {}", teacherId, courseId);
        
        // 验证教师权限
        if (!checkTeacherCoursePermission(teacherId, courseId)) {
            throw new MyException("您没有权限导出该课程评论");
        }
        
        // 1. 查询数据
        Map<String, Object> params = new HashMap<>();
        params.put("courseId", courseId);
        List<CourseComment> comments = courseCommentDao.queryByParams(params);
        
        // 2. 转换为导出格式
        List<Map<String, Object>> exportData = comments.stream().map(comment -> {
            Map<String, Object> data = new HashMap<>();
            data.put("commentId", comment.getId());
            data.put("userId", comment.getUserId());
            data.put("userName", comment.getUserName() != null ? comment.getUserName() : "未知");
            data.put("rating", comment.getScore() != null ? comment.getScore() : 0);
            data.put("content", comment.getContent() != null ? comment.getContent() : "");
            data.put("status", comment.getStatus() == 1 ? "显示" : "隐藏");
            data.put("createTime", comment.getCreateTime());
            return data;
        }).collect(Collectors.toList());
        
        // 3. 定义表头和数据键
        List<String> headers = Arrays.asList(
            "评论ID", "用户ID", "用户姓名", "评分", "评论内容", "状态", "评论时间"
        );
        List<String> dataKeys = Arrays.asList(
            "commentId", "userId", "userName", "rating", "content", "status", "createTime"
        );
        
        // 4. 生成Excel文件并写入响应
        String fileName = com.jieni.mqxq.util.ExcelExportUtil.generateFileName("课程评论");
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + fileName + ".xlsx";
        
        com.jieni.mqxq.util.ExcelExportUtil.exportToFile(
            filePath, "课程评论", headers, dataKeys, exportData
        );
        com.jieni.mqxq.util.ExcelExportUtil.writeFileToResponseAndDelete(response, filePath, fileName);
        
        log.info("课程评论导出成功并已触发下载, 文件路径: {}", filePath);
    }

    // ========== 管理员端方法 ==========

    /**
     * 管理员获取评论分页列表
     */
    @Override
    public PageInfo<CommentVO> getCommentsPageForAdmin(CommentQueryDTO queryDTO) {
        log.info("管理员查询评论列表, page: {}, size: {}", queryDTO.getPage(), queryDTO.getSize());
        
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        
        Map<String, Object> params = new HashMap<>();
        if (queryDTO.getCourseId() != null) {
            params.put("courseId", queryDTO.getCourseId());
        }
        if (queryDTO.getUserId() != null) {
            params.put("userId", queryDTO.getUserId());
        }
        if (queryDTO.getStatus() != null) {
            params.put("status", queryDTO.getStatus());
        }
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            params.put("keyword", "%" + queryDTO.getKeyword().trim() + "%");
        }
        
        List<CourseComment> list = courseCommentDao.queryForAdmin(params);
        PageInfo<CourseComment> pageInfo = new PageInfo<>(list);
        
        return convertPageInfo(pageInfo);
    }

    /**
     * 管理员获取评论详情
     */
    @Override
    public CommentVO getCommentDetailForAdmin(Integer commentId) {
        log.info("管理员查看评论详情, commentId: {}", commentId);
        
        CourseComment comment = courseCommentDao.queryById(commentId);
        if (comment == null) {
            throw new MyException("评论不存在");
        }
        
        return convertToVO(comment);
    }

    /**
     * 管理员切换评论状态（显示/隐藏）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String toggleCommentStatus(Integer commentId) {
        log.info("管理员切换评论状态, commentId: {}", commentId);
        
        CourseComment comment = courseCommentDao.queryById(commentId);
        if (comment == null) {
            throw new MyException("评论不存在");
        }
        
        // 切换状态：0-隐藏，1-显示
        int newStatus = comment.getStatus() == 1 ? 0 : 1;
        comment.setStatus(newStatus);
        comment.setUpdateTime(new Date());
        
        courseCommentDao.update(comment);
        
        String statusText = newStatus == 1 ? "显示" : "隐藏";
        log.info("评论{}状态已切换为{}", commentId, statusText);
        return statusText;
    }

    /**
     * 管理员批量隐藏评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchHideComments(CommentBatchOperationDTO dto) {
        log.info("管理员批量隐藏评论, commentIds: {}", dto.getCommentIds());
        return batchUpdateStatus(dto.getCommentIds(), 0);
    }

    /**
     * 管理员批量显示评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchShowComments(CommentBatchOperationDTO dto) {
        log.info("管理员批量显示评论, commentIds: {}", dto.getCommentIds());
        return batchUpdateStatus(dto.getCommentIds(), 1);
    }

    /**
     * 管理员删除评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Integer commentId) {
        log.info("管理员删除评论, commentId: {}", commentId);
        
        CourseComment comment = courseCommentDao.queryById(commentId);
        if (comment == null) {
            throw new MyException("评论不存在");
        }
        
        if (courseCommentDao.deleteById(commentId) <= 0) {
            throw new MyException("评论删除失败");
        }
        
        log.info("管理员删除评论{}成功", commentId);
    }

    /**
     * 管理员批量删除评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchDeleteComments(CommentBatchOperationDTO dto) {
        log.info("管理员批量删除评论, commentIds: {}", dto.getCommentIds());
        
        int count = 0;
        for (Integer id : dto.getCommentIds()) {
            try {
                if (courseCommentDao.deleteById(id) > 0) {
                    count++;
                }
            } catch (Exception e) {
                log.error("批量删除评论失败: id={}", id, e);
            }
        }
        
        log.info("批量删除评论完成，成功删除{}条", count);
        return count;
    }

    /**
     * 管理员获取评论统计信息
     */
    @Override
    public CommentStatisticsVO getCommentStatisticsForAdmin() {
        log.info("管理员获取评论统计信息");
        
        List<CourseComment> allComments = courseCommentDao.queryAll();
        
        CommentStatisticsVO vo = new CommentStatisticsVO();
        vo.setTotalComments((long) allComments.size());
        vo.setVisibleComments(allComments.stream().filter(c -> c.getStatus() == 1).count());
        vo.setHiddenComments(allComments.stream().filter(c -> c.getStatus() == 0).count());
            
            // 今日新增评论
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date todayStart = todayCal.getTime();
        
            long todayCount = allComments.stream()
                    .filter(c -> c.getCreateTime() != null && c.getCreateTime().after(todayStart))
                    .count();
        vo.setTodayComments(todayCount);

            // 平均评分
            double averageRating = allComments.stream()
                    .filter(c -> c.getStatus() == 1) // 只统计可见评论
                    .mapToInt(CourseComment::getScore)
                    .average()
                    .orElse(0.0);
        vo.setAverageRating(Math.round(averageRating * 100.0) / 100.0);
        
        return vo;
    }

    /**
     * 管理员获取课程评分排行榜
     */
    @Override
    public List<Map<String, Object>> getCourseRatingRank(Integer limit) {
        log.info("管理员获取课程评分排行, limit: {}", limit);
        
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        return courseCommentDao.getCourseRatingRank(limit);
    }

    // ========== 公共API方法 ==========

    /**
     * 公开获取课程评论分页列表
     */
    @Override
    public PageInfo<CommentVO> getPublicCourseCommentsPage(CommentQueryDTO queryDTO) {
        log.info("公开获取课程评论列表, courseId: {}, page: {}, size: {}", 
                queryDTO.getCourseId(), queryDTO.getPage(), queryDTO.getSize());
        
        if (queryDTO.getCourseId() == null) {
            throw new MyException("课程ID不能为空");
        }
        
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
        
            Map<String, Object> params = new HashMap<>();
        params.put("courseId", queryDTO.getCourseId());
            params.put("status", 1); // 只显示可见的评论
        params.put("orderBy", convertOrderBy(queryDTO.getOrderBy()));
        
        List<CourseComment> list = courseCommentDao.queryByParams(params);
        PageInfo<CourseComment> pageInfo = new PageInfo<>(list);
        
        return convertPageInfo(pageInfo);
    }

    /**
     * 公开获取课程评分统计
     */
    @Override
    public CommentRatingVO getPublicCourseRatingStatistics(Integer courseId) {
        return getCourseRatingStatistics(courseId);
    }

    /**
     * 公开获取最新评论列表
     */
    @Override
    public List<CommentVO> getLatestCommentsForPublic(Integer courseId, Integer limit) {
        log.info("获取课程最新评论, courseId: {}, limit: {}", courseId, limit);
        
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID无效");
        }
        
        if (limit == null || limit <= 0) {
            limit = 5;
        }
        
            Map<String, Object> params = new HashMap<>();
            params.put("courseId", courseId);
            params.put("status", 1); // 只显示可见的评论
            params.put("limit", limit);
        params.put("orderBy", "create_time DESC");
        
        List<CourseComment> comments = courseCommentDao.queryByParams(params);
        return convertToVOList(comments);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 检查用户是否已评论
     */
    private boolean hasUserCommented(Integer userId, Integer courseId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("courseId", courseId);
        List<CourseComment> comments = courseCommentDao.queryByParams(params);
        return !comments.isEmpty();
    }

    /**
     * 检查评论权限
     */
    private boolean checkCommentPermission(Integer userId, Integer courseId) {
            Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
            params.put("courseId", courseId);
        List<MyCourse> myCourses = myCourseDao.queryByParams(params);
        return !myCourses.isEmpty();
    }

    /**
     * 检查教师课程权限
     */
    private boolean checkTeacherCoursePermission(Integer teacherId, Integer courseId) {
        Course course = courseDao.queryById(courseId);
        return course != null && course.getCreateUser().equals(teacherId);
    }

    /**
     * 批量更新评论状态
     */
    private int batchUpdateStatus(List<Integer> commentIds, Integer status) {
        int count = 0;
        for (Integer id : commentIds) {
            try {
                CourseComment comment = courseCommentDao.queryById(id);
                if (comment != null) {
                    comment.setStatus(status);
                    comment.setUpdateTime(new Date());
                    courseCommentDao.update(comment);
                    count++;
                }
        } catch (Exception e) {
                log.error("批量更新评论状态失败: id={}", id, e);
            }
        }
        return count;
    }

    /**
     * 转换排序方式
     */
    private String convertOrderBy(String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return "create_time DESC";
        }
        
        return switch (orderBy.toLowerCase()) {
            case "time" -> "create_time DESC";
            case "score" -> "score DESC, create_time DESC";
            default -> "create_time DESC";
        };
    }

    /**
     * 实体转VO
     */
    private CommentVO convertToVO(CourseComment entity) {
        if (entity == null) {
            return null;
        }
        CommentVO vo = BeanUtil.copyProperties(entity, CommentVO.class);
        
        // 统一使用最新的用户信息（从用户表 JOIN 查询来的）
        // 用最新的 userName 和 userAvatar 填充 nickname 和 avatar，保持一致性
        if (vo != null) {
            // 使用最新的 userName 填充 nickname
            if (vo.getUserName() != null && !vo.getUserName().isEmpty()) {
                vo.setNickname(vo.getUserName());
            } else {
                vo.setNickname("");
            }
            
            // 使用最新的 userAvatar 填充 avatar
            if (vo.getUserAvatar() != null && !vo.getUserAvatar().isEmpty()) {
                vo.setAvatar(vo.getUserAvatar());
            } else {
                vo.setAvatar("");
            }
        }
        
        return vo;
    }

    /**
     * 实体列表转VO列表
     */
    private List<CommentVO> convertToVOList(List<CourseComment> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 分页信息转换
     */
    private PageInfo<CommentVO> convertPageInfo(PageInfo<CourseComment> pageInfo) {
        PageInfo<CommentVO> voPageInfo = new PageInfo<>();
        BeanUtil.copyProperties(pageInfo, voPageInfo);
        voPageInfo.setList(convertToVOList(pageInfo.getList()));
        return voPageInfo;
    }
}
