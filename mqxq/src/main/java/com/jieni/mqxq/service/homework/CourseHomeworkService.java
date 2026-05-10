package com.jieni.mqxq.service.homework;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.homework.*;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkVO;
import com.jieni.mqxq.domain.vo.homework.CourseHomeworkSubmissionVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 课程作业服务接口
 * 
 * 提供课程作业的完整业务服务，包括作业管理、提交、批改等功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CourseHomeworkService {

    // ========== 基础CRUD方法 ==========

    /**
     * 根据ID获取作业详情
     *
     * @param id 作业ID
     * @return 作业VO
     */
    CourseHomeworkVO getHomeworkById(Integer id);

    /**
     * 创建作业
     *
     * @param createDTO 创建DTO
     * @param teacherId 教师ID
     * @return 作业VO
     */
    CourseHomeworkVO createHomework(CourseHomeworkCreateDTO createDTO, Integer teacherId);

    /**
     * 更新作业
     *
     * @param updateDTO 更新DTO
     * @param teacherId 教师ID
     * @return 作业VO
     */
    CourseHomeworkVO updateHomework(CourseHomeworkUpdateDTO updateDTO, Integer teacherId);

    /**
     * 删除作业
     *
     * @param id 作业ID
     * @param teacherId 教师ID（权限校验用）
     * @return 是否成功
     */
    boolean deleteHomework(Integer id, Integer teacherId);

    /**
     * 发布作业
     *
     * @param id 作业ID
     * @param teacherId 教师ID
     * @return 作业VO
     */
    CourseHomeworkVO publishHomework(Integer id, Integer teacherId);

    // ========== 查询方法 ==========

    /**
     * 分页查询作业列表
     *
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    PageInfo<CourseHomeworkVO> getHomeworkPage(CourseHomeworkQueryDTO queryDTO);

    /**
     * 获取课程的作业列表（用户端）
     *
     * @param courseId 课程ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<CourseHomeworkVO> getPublishedHomeworkByCourse(Integer courseId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的作业列表（用户端）
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 状态
     * @return 分页结果
     */
    PageInfo<CourseHomeworkVO> getMyHomeworkList(Integer userId, Integer pageNum, Integer pageSize, Integer status);

    // ========== 批量操作 ==========

    /**
     * 批量发布作业
     *
     * @param batchDTO 批量操作DTO
     * @param teacherId 教师ID
     * @return 成功数量
     */
    int batchPublishHomework(CourseHomeworkBatchOperationDTO batchDTO, Integer teacherId);

    /**
     * 批量删除作业
     *
     * @param batchDTO 批量操作DTO
     * @param teacherId 教师ID
     * @return 成功数量
     */
    int batchDeleteHomework(CourseHomeworkBatchOperationDTO batchDTO, Integer teacherId);

    /**
     * 批量取消发布（管理员）
     *
     * @param batchDTO 批量操作DTO
     * @return 成功数量
     */
    int batchUnpublishHomework(CourseHomeworkBatchOperationDTO batchDTO);

    // ========== 高级功能 ==========

    /**
     * 复制作业
     *
     * @param homeworkId 原作业ID
     * @param teacherId 教师ID
     * @return 新作业VO
     */
    CourseHomeworkVO copyHomework(Integer homeworkId, Integer teacherId);

    /**
     * 延长截止时间
     *
     * @param updateDTO 更新DTO
     * @param teacherId 教师ID
     * @return 作业VO
     */
    CourseHomeworkVO extendDeadline(CourseHomeworkUpdateDTO updateDTO, Integer teacherId);

    /**
     * 获取即将截止的作业
     *
     * @param teacherId 教师ID
     * @param days 提前天数
     * @return 作业列表
     */
    List<CourseHomeworkVO> getUpcomingDeadlineHomework(Integer teacherId, int days);

    // ========== 统计分析 ==========

    /**
     * 获取教师作业统计
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID（可选）
     * @return 统计数据
     */
    Map<String, Object> getTeacherHomeworkStatistics(Integer teacherId, Integer courseId);

    /**
     * 获取管理员作业统计
     *
     * @return 统计数据
     */
    Map<String, Object> getAdminHomeworkStatistics();

    /**
     * 获取教师作业排行
     *
     * @return 排行数据
     */
    List<Map<String, Object>> getTeacherHomeworkRank();

    /**
     * 获取课程作业排行
     *
     * @return 排行数据
     */
    List<Map<String, Object>> getCourseHomeworkRank();

    /**
     * 获取作业趋势分析
     *
     * @param days 天数
     * @return 趋势数据
     */
    List<Map<String, Object>> getHomeworkTrendAnalysis(int days);

    // ========== 作业提交 ==========

    /**
     * 提交作业
     *
     * @param submitDTO 提交DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean submitHomework(CourseHomeworkSubmitDTO submitDTO, Integer userId);

    /**
     * 更新作业提交
     *
     * @param submitDTO 提交DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateHomeworkSubmission(CourseHomeworkSubmitDTO submitDTO, Integer userId);

    /**
     * 撤回作业提交
     *
     * @param homeworkId 作业ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean withdrawHomeworkSubmission(Integer homeworkId, Integer userId);

    /**
     * 上传作业附件
     *
     * @param homeworkId 作业ID
     * @param userId 用户ID
     * @param file 文件
     * @return 文件URL
     */
    String uploadHomeworkAttachment(Integer homeworkId, Integer userId, MultipartFile file);

    /**
     * 获取用户的作业提交记录
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param courseId 课程ID（可选）
     * @return 分页结果
     */
    PageInfo<CourseHomeworkSubmissionVO> getUserSubmissions(Integer userId, Integer pageNum, Integer pageSize, Integer courseId);

    /**
     * 获取用户的作业提交详情
     *
     * @param homeworkId 作业ID
     * @param userId 用户ID
     * @return 提交详情
     */
    Map<String, Object> getUserSubmissionDetail(Integer homeworkId, Integer userId);

    // ========== 作业批改 ==========

    /**
     * 批改作业
     *
     * @param gradeDTO 批改DTO
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean gradeHomework(CourseHomeworkGradeDTO gradeDTO, Integer teacherId);

    /**
     * 获取作业的提交列表（教师端）
     *
     * @param homeworkId 作业ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 批改状态
     * @param studentName 学生姓名
     * @return 分页结果
     */
    PageInfo<CourseHomeworkSubmissionVO> getHomeworkSubmissions(Integer homeworkId, Integer pageNum, Integer pageSize, 
                                                               Integer status, String studentName);

    /**
     * 获取作业提交详情（教师端）
     *
     * @param submissionId 提交记录ID
     * @param teacherId 教师ID
     * @return 提交详情
     */
    Map<String, Object> getSubmissionDetailForTeacher(Integer submissionId, Integer teacherId);

    /**
     * 获取作业提交统计
     *
     * @param homeworkId 作业ID
     * @return 统计数据
     */
    Map<String, Object> getHomeworkSubmissionStatistics(Integer homeworkId);

    /**
     * 导出作业数据
     *
     * @param queryDTO 查询条件
     * @param response HTTP响应
     */
    void exportHomeworkData(CourseHomeworkQueryDTO queryDTO, HttpServletResponse response);

    /**
     * 导出作业提交记录
     *
     * @param homeworkId 作业ID
     * @param response HTTP响应
     */
    void exportHomeworkSubmissions(Integer homeworkId, HttpServletResponse response);

    // ========== 权限验证和辅助方法 ==========

    /**
     * 检查教师对作业的权限
     *
     * @param teacherId 教师ID
     * @param homeworkId 作业ID
     * @return 是否有权限
     */
    boolean checkTeacherHomeworkPermission(Integer teacherId, Integer homeworkId);

    /**
     * 检查用户是否可以提交作业
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否可以提交
     */
    boolean checkUserSubmissionPermission(Integer userId, Integer courseId);

    /**
     * 检查用户是否已提交作业
     *
     * @param userId 用户ID
     * @param homeworkId 作业ID
     * @return 是否已提交
     */
    boolean hasUserSubmitted(Integer userId, Integer homeworkId);

    /**
     * 根据ID获取作业实体
     *
     * @param homeworkId 作业ID
     * @return 作业实体
     */
    com.jieni.mqxq.domain.entity.CourseHomework getById(Integer homeworkId);

    /**
     * 上传作业文件
     *
     * @param userId 用户ID
     * @param homeworkId 作业ID
     * @param file 文件
     * @return 文件URL
     */
    String uploadHomeworkFile(Integer userId, Integer homeworkId, MultipartFile file);

    /**
     * 批改作业（兼容旧方法签名）
     *
     * @param homeworkId 作业ID
     * @param userId 用户ID
     * @param score 分数
     * @param feedback 反馈
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean gradeHomework(Integer homeworkId, Integer userId, Integer score, String feedback, Integer teacherId);

    /**
     * 批量批改作业（兼容旧方法签名）
     *
     * @param homeworkId 作业ID
     * @param gradings 批改列表
     * @param teacherId 教师ID
     * @return 成功数量
     */
    int batchGradeHomework(Integer homeworkId, List<Map<String, Object>> gradings, Integer teacherId);

    /**
     * 获取学生提交详情（教师端）
     *
     * @param homeworkId 作业ID
     * @param userId 用户ID
     * @return 提交详情
     */
    Map<String, Object> getStudentSubmissionDetail(Integer homeworkId, Integer userId);

    /**
     * 获取未提交学生列表
     *
     * @param homeworkId 作业ID
     * @return 未提交学生列表
     */
    List<Map<String, Object>> getUnsubmittedStudents(Integer homeworkId);
}
