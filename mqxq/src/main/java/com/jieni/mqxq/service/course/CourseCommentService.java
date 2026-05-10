package com.jieni.mqxq.service.course;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.comment.*;
import com.jieni.mqxq.domain.vo.comment.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 课程评论服务接口
 *
 * 提供课程评论系统的完整业务功能，支持多角色评论管理和统计分析。
 * 采用DTO/VO模式，实现业务逻辑与表现层解耦。
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseCommentService {

    // ========== 用户端方法 ==========

    /**
     * 用户发表课程评论
     * 包含权限验证、重复评论检查
     *
     * @param userId 用户ID
     * @param dto 评论创建DTO
     * @return 评论VO
     */
    CommentVO createComment(Integer userId, CommentCreateDTO dto);

    /**
     * 用户修改自己的评论
     * 包含权限验证
     *
     * @param userId 用户ID
     * @param commentId 评论ID
     * @param dto 评论更新DTO
     * @return 评论VO
     */
    CommentVO updateCommentByUser(Integer userId, Integer commentId, CommentUpdateDTO dto);

    /**
     * 用户删除自己的评论
     * 包含权限验证
     *
     * @param userId 用户ID
     * @param commentId 评论ID
     */
    void deleteCommentByUser(Integer userId, Integer commentId);

    /**
     * 获取课程评论分页列表（用户可见）
     *
     * @param queryDTO 查询条件
     * @return 评论分页列表
     */
    PageInfo<CommentVO> getCourseCommentsPage(CommentQueryDTO queryDTO);

    /**
     * 获取用户的评论分页列表
     *
     * @param userId 用户ID
     * @param queryDTO 查询条件
     * @return 评论分页列表
     */
    PageInfo<CommentVO> getUserCommentsPage(Integer userId, CommentQueryDTO queryDTO);

    /**
     * 获取课程评分统计
     *
     * @param courseId 课程ID
     * @return 评分统计VO
     */
    CommentRatingVO getCourseRatingStatistics(Integer courseId);

    /**
     * 检查用户评论状态
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 包含hasCommented和comment的Map
     */
    Map<String, Object> checkUserCommentStatus(Integer userId, Integer courseId);

    // ========== 教师端方法 ==========

    /**
     * 教师获取自己所有课程的评论分页列表
     *
     * @param teacherId 教师ID
     * @param queryDTO 查询条件
     * @return 评论分页列表
     */
    PageInfo<CommentVO> getTeacherCoursesCommentsPage(Integer teacherId, CommentQueryDTO queryDTO);

    /**
     * 教师获取指定课程的评论分页列表
     * 包含权限验证
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param queryDTO 查询条件
     * @return 评论分页列表
     */
    PageInfo<CommentVO> getTeacherCourseCommentsPage(Integer teacherId, Integer courseId, CommentQueryDTO queryDTO);

    /**
     * 教师获取评论详情
     * 包含权限验证
     *
     * @param teacherId 教师ID
     * @param commentId 评论ID
     * @return 评论VO
     */
    CommentVO getCommentDetailForTeacher(Integer teacherId, Integer commentId);

    /**
     * 教师获取自己所有课程的评分统计
     *
     * @param teacherId 教师ID
     * @return 评分统计VO
     */
    CommentStatisticsVO getTeacherCoursesRatingStatistics(Integer teacherId);

    /**
     * 教师获取指定课程的评分统计
     * 包含权限验证
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 评分统计VO
     */
    CommentRatingVO getTeacherCourseRatingStatistics(Integer teacherId, Integer courseId);

    /**
     * 教师获取最新评论列表
     *
     * @param teacherId 教师ID
     * @param limit 数量限制
     * @return 最新评论列表
     */
    List<CommentVO> getLatestCommentsForTeacher(Integer teacherId, Integer limit);

    /**
     * 教师获取评论趋势分析
     *
     * @param teacherId 教师ID
     * @param queryDTO 查询条件（包含courseId和days）
     * @return 趋势分析数据
     */
    Map<String, Object> getCommentTrendAnalysisForTeacher(Integer teacherId, CommentQueryDTO queryDTO);

    /**
     * 教师导出课程评论
     * 包含权限验证
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param response HTTP响应
     */
    void exportCourseCommentsForTeacher(Integer teacherId, Integer courseId, HttpServletResponse response);

    // ========== 管理员端方法 ==========

    /**
     * 管理员获取评论分页列表
     *
     * @param queryDTO 查询条件
     * @return 评论分页列表
     */
    PageInfo<CommentVO> getCommentsPageForAdmin(CommentQueryDTO queryDTO);

    /**
     * 管理员获取评论详情
     *
     * @param commentId 评论ID
     * @return 评论VO
     */
    CommentVO getCommentDetailForAdmin(Integer commentId);

    /**
     * 管理员切换评论状态（显示/隐藏）
     *
     * @param commentId 评论ID
     * @return 新状态的文本描述
     */
    String toggleCommentStatus(Integer commentId);

    /**
     * 管理员批量隐藏评论
     *
     * @param dto 批量操作DTO
     * @return 成功数量
     */
    Integer batchHideComments(CommentBatchOperationDTO dto);

    /**
     * 管理员批量显示评论
     *
     * @param dto 批量操作DTO
     * @return 成功数量
     */
    Integer batchShowComments(CommentBatchOperationDTO dto);

    /**
     * 管理员删除评论
     *
     * @param commentId 评论ID
     */
    void deleteCommentByAdmin(Integer commentId);

    /**
     * 管理员批量删除评论
     *
     * @param dto 批量操作DTO
     * @return 成功数量
     */
    Integer batchDeleteComments(CommentBatchOperationDTO dto);

    /**
     * 管理员获取评论统计信息
     *
     * @return 评论统计VO
     */
    CommentStatisticsVO getCommentStatisticsForAdmin();

    /**
     * 管理员获取课程评分排行榜
     *
     * @param limit 数量限制
     * @return 评分排行列表
     */
    List<Map<String, Object>> getCourseRatingRank(Integer limit);

    // ========== 公共API方法 ==========

    /**
     * 公开获取课程评论分页列表
     *
     * @param queryDTO 查询条件
     * @return 评论分页列表
     */
    PageInfo<CommentVO> getPublicCourseCommentsPage(CommentQueryDTO queryDTO);

    /**
     * 公开获取课程评分统计
     *
     * @param courseId 课程ID
     * @return 评分统计VO
     */
    CommentRatingVO getPublicCourseRatingStatistics(Integer courseId);

    /**
     * 公开获取最新评论列表
     *
     * @param courseId 课程ID
     * @param limit 数量限制
     * @return 最新评论列表
     */
    List<CommentVO> getLatestCommentsForPublic(Integer courseId, Integer limit);
}
