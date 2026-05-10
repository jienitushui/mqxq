package com.jieni.mqxq.service.homework;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.homework.*;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionDetailVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionStatsVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 作业提交服务接口
 * 
 * 提供作业提交的完整业务服务，包括学生提交管理、教师批改管理和统计分析
 * 所有方法都使用业务语义化命名，提高代码可读性和可维护性
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface HomeworkSubmissionService {

    // ========== 学生端服务 ==========

    /**
     * 学生提交作业
     * 
     * @param createDTO 作业提交创建DTO
     * @param studentId 学生ID
     * @return 提交详情VO
     */
    HomeworkSubmissionDetailVO submitHomework(HomeworkSubmissionCreateDTO createDTO, Integer studentId);

    /**
     * 学生更新作业提交
     * 
     * @param submissionId 提交记录ID
     * @param updateDTO 作业提交更新DTO
     * @param studentId 学生ID
     * @return 提交详情VO
     */
    HomeworkSubmissionDetailVO updateHomeworkSubmission(Integer submissionId, HomeworkSubmissionUpdateDTO updateDTO, Integer studentId);

    /**
     * 学生撤回作业提交
     * 
     * @param submissionId 提交记录ID
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean withdrawHomeworkSubmission(Integer submissionId, Integer studentId);

    /**
     * 检查学生是否已提交作业
     * 
     * @param studentId 学生ID
     * @param homeworkId 作业ID
     * @return 是否已提交
     */
    boolean checkStudentSubmitted(Integer studentId, Integer homeworkId);

    /**
     * 根据作业ID获取学生的提交详情
     * 
     * @param studentId 学生ID
     * @param homeworkId 作业ID
     * @return 提交详情VO，未提交返回null
     */
    HomeworkSubmissionDetailVO getStudentSubmissionByHomework(Integer studentId, Integer homeworkId);

    /**
     * 分页获取学生的提交列表
     * 
     * @param queryDTO 查询条件DTO
     * @param studentId 学生ID
     * @return 分页提交列表
     */
    PageInfo<HomeworkSubmissionVO> getStudentSubmissionPage(HomeworkSubmissionQueryDTO queryDTO, Integer studentId);

    // ========== 教师端服务 ==========

    /**
     * 教师批改作业
     * 
     * @param submissionId 提交记录ID
     * @param gradeDTO 批改DTO
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean gradeHomeworkSubmission(Integer submissionId, HomeworkSubmissionGradeDTO gradeDTO, Integer teacherId);

    /**
     * 教师批量批改作业
     * 
     * @param homeworkId 作业ID
     * @param batchGradeDTO 批量批改DTO
     * @param teacherId 教师ID
     * @return 成功批改的数量
     */
    int batchGradeHomeworkSubmissions(Integer homeworkId, HomeworkSubmissionBatchGradeDTO batchGradeDTO, Integer teacherId);

    /**
     * 教师分页查询作业的提交列表
     * 
     * @param queryDTO 查询条件DTO
     * @return 分页提交列表
     */
    PageInfo<HomeworkSubmissionVO> getHomeworkSubmissionPage(HomeworkSubmissionQueryDTO queryDTO);

    /**
     * 获取作业提交统计信息
     * 
     * @param homeworkId 作业ID
     * @return 统计信息VO
     */
    HomeworkSubmissionStatsVO getHomeworkSubmissionStatistics(Integer homeworkId);

    /**
     * 分页获取待批改的提交列表
     * 
     * @param queryDTO 查询条件DTO
     * @param teacherId 教师ID（可选，为null则查询所有）
     * @return 分页提交列表
     */
    PageInfo<HomeworkSubmissionVO> getPendingGradeSubmissionPage(HomeworkSubmissionQueryDTO queryDTO, Integer teacherId);

    // ========== 管理员端服务 ==========

    /**
     * 管理员分页查询所有提交
     * 
     * @param queryDTO 查询条件DTO
     * @return 分页提交列表
     */
    PageInfo<HomeworkSubmissionVO> getAllSubmissionPage(HomeworkSubmissionQueryDTO queryDTO);

    /**
     * 根据ID获取提交详情
     * 
     * @param submissionId 提交记录ID
     * @return 提交详情VO
     */
    HomeworkSubmissionDetailVO getSubmissionDetail(Integer submissionId);

    /**
     * 管理员删除提交记录
     * 
     * @param submissionId 提交记录ID
     * @return 是否成功
     */
    boolean deleteSubmission(Integer submissionId);

    /**
     * 管理员批量删除提交记录
     * 
     * @param deleteDTO 批量删除DTO
     * @return 成功删除的数量
     */
    int batchDeleteSubmissions(HomeworkSubmissionBatchDeleteDTO deleteDTO);

    /**
     * 获取提交统计信息（支持时间范围）
     * 
     * @param homeworkId 作业ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计信息VO
     */
    HomeworkSubmissionStatsVO getSubmissionStatistics(Integer homeworkId, String startDate, String endDate);

    /**
     * 导出作业提交数据
     * 
     * @param queryDTO 查询条件DTO
     * @param response HTTP响应
     */
    void exportSubmissions(HomeworkSubmissionQueryDTO queryDTO, HttpServletResponse response);
}
