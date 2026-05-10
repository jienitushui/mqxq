package com.jieni.mqxq.service.impl.homework;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CourseHomeworkDao;
import com.jieni.mqxq.dao.HomeworkSubmissionDao;
import com.jieni.mqxq.domain.dto.homework.*;
import com.jieni.mqxq.domain.entity.CourseHomework;
import com.jieni.mqxq.domain.entity.HomeworkSubmission;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionDetailVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionStatsVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.homework.HomeworkSubmissionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作业提交服务实现类
 * 
 * 提供作业提交系统的完整业务实现，包括学生提交、教师批改等功能
 * 支持提交状态管理、成绩统计、批量操作以及数据导出功能
 * 实现作业提交的全生命周期管理和质量监控
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class HomeworkSubmissionServiceImpl implements HomeworkSubmissionService {

    @Resource
    private HomeworkSubmissionDao homeworkSubmissionDao;

    @Resource
    private CourseHomeworkDao courseHomeworkDao;

    // ========== 学生端服务 ==========

    /**
     * 学生提交作业
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeworkSubmissionDetailVO submitHomework(HomeworkSubmissionCreateDTO createDTO, Integer studentId) {
        log.info("学生提交作业, studentId: {}, homeworkId: {}", studentId, createDTO.getHomeworkId());
        
        // 验证作业是否存在
        CourseHomework homework = courseHomeworkDao.queryById(createDTO.getHomeworkId());
        if (homework == null) {
            throw new MyException("作业不存在");
        }
        
        // 验证是否已提交
        HomeworkSubmission existing = homeworkSubmissionDao.queryByHomeworkAndStudent(
            createDTO.getHomeworkId(), studentId);
        if (existing != null) {
            throw new MyException("您已提交过该作业，请使用更新功能");
        }
        
        // 验证是否在提交期限内
        Date now = new Date();
        if (homework.getEndTime() != null && homework.getEndTime().before(now)) {
            throw new MyException("作业已截止，无法提交");
        }
        
        // 创建提交记录
        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomeworkId(createDTO.getHomeworkId());
        submission.setStudentId(studentId);
        submission.setContent(createDTO.getContent());
        submission.setAttachmentUrl(createDTO.getAttachmentUrl());
        submission.setSubmitTime(now);
        submission.setStatus(0); // 待批改
        submission.setMaxScore(homework.getScore());
        submission.setCreateTime(now);
        submission.setCreateUser(studentId);
        submission.setUpdateTime(now);
        submission.setUpdateUser(studentId);
        
        homeworkSubmissionDao.insert(submission);
        
        return convertToDetailVO(submission);
    }

    /**
     * 学生更新作业提交
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeworkSubmissionDetailVO updateHomeworkSubmission(Integer submissionId, 
                                                               HomeworkSubmissionUpdateDTO updateDTO, 
                                                               Integer studentId) {
        log.info("学生更新作业提交, studentId: {}, submissionId: {}", studentId, submissionId);
        
        // 验证提交记录是否存在
        HomeworkSubmission submission = homeworkSubmissionDao.queryById(submissionId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        // 验证权限
        if (!submission.getStudentId().equals(studentId)) {
            throw new MyException("无权限修改该提交");
        }
        
        // 验证是否已被批改
        if (submission.getStatus() == 1) {
            throw new MyException("作业已被批改，无法修改");
        }
        
        // 验证作业是否还在提交期内
        CourseHomework homework = courseHomeworkDao.queryById(submission.getHomeworkId());
        if (homework != null && homework.getEndTime() != null && homework.getEndTime().before(new Date())) {
            throw new MyException("作业已截止，无法修改");
        }
        
        // 更新提交内容
        submission.setContent(updateDTO.getContent());
        submission.setAttachmentUrl(updateDTO.getAttachmentUrl());
        submission.setSubmitTime(new Date());
        submission.setUpdateTime(new Date());
        submission.setUpdateUser(studentId);
        
        homeworkSubmissionDao.update(submission);
        
        return convertToDetailVO(submission);
    }

    /**
     * 学生撤回作业提交
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawHomeworkSubmission(Integer submissionId, Integer studentId) {
        log.info("学生撤回作业提交, studentId: {}, submissionId: {}", studentId, submissionId);
        
        // 验证提交记录是否存在
        HomeworkSubmission submission = homeworkSubmissionDao.queryById(submissionId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        // 验证权限
        if (!submission.getStudentId().equals(studentId)) {
            throw new MyException("无权限撤回该提交");
        }
        
        // 验证是否已被批改
        if (submission.getStatus() == 1) {
            throw new MyException("作业已被批改，无法撤回");
        }
        
        // 验证作业是否还在提交期内
        CourseHomework homework = courseHomeworkDao.queryById(submission.getHomeworkId());
        if (homework != null && homework.getEndTime() != null && homework.getEndTime().before(new Date())) {
            throw new MyException("作业已截止，无法撤回");
        }
        
        return homeworkSubmissionDao.deleteById(submissionId) > 0;
    }

    /**
     * 检查学生是否已提交作业
     */
    @Override
    public boolean checkStudentSubmitted(Integer studentId, Integer homeworkId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, studentId);
        return submission != null;
    }

    /**
     * 根据作业ID获取学生的提交详情
     */
    @Override
    public HomeworkSubmissionDetailVO getStudentSubmissionByHomework(Integer studentId, Integer homeworkId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(homeworkId, studentId);
        if (submission == null) {
            return null;
        }
        return convertToDetailVO(submission);
    }

    /**
     * 分页获取学生的提交列表
     */
    @Override
    public PageInfo<HomeworkSubmissionVO> getStudentSubmissionPage(HomeworkSubmissionQueryDTO queryDTO, Integer studentId) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        params.put("studentId", studentId);
        
        List<HomeworkSubmission> list = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        List<HomeworkSubmissionVO> voList = list.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageInfo<>(voList);
    }

    // ========== 教师端服务 ==========

    /**
     * 教师批改作业
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean gradeHomeworkSubmission(Integer submissionId, HomeworkSubmissionGradeDTO gradeDTO, Integer teacherId) {
        log.info("教师批改作业, teacherId: {}, submissionId: {}, score: {}", 
            teacherId, submissionId, gradeDTO.getScore());
        
        // 验证提交记录是否存在
        HomeworkSubmission submission = homeworkSubmissionDao.queryById(submissionId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        
        // 验证分数是否在有效范围内
        if (submission.getMaxScore() != null && 
            gradeDTO.getScore().compareTo(submission.getMaxScore()) > 0) {
            throw new MyException("分数不能超过最高分: " + submission.getMaxScore());
        }
        
        // 设置批改信息
        submission.setScore(gradeDTO.getScore());
        submission.setTeacherComment(gradeDTO.getTeacherComment());
        submission.setGradeTime(new Date());
        submission.setGradeUser(teacherId);
        submission.setStatus(1); // 已批改
        submission.setUpdateTime(new Date());
        submission.setUpdateUser(teacherId);
        
        return homeworkSubmissionDao.update(submission) > 0;
    }

    /**
     * 教师批量批改作业
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGradeHomeworkSubmissions(Integer homeworkId, 
                                            HomeworkSubmissionBatchGradeDTO batchGradeDTO, 
                                            Integer teacherId) {
        log.info("教师批量批改作业, teacherId: {}, homeworkId: {}, count: {}", 
            teacherId, homeworkId, batchGradeDTO.getGradings().size());
        
        int successCount = 0;
        Date now = new Date();
        
        for (HomeworkSubmissionBatchGradeDTO.GradeItem item : batchGradeDTO.getGradings()) {
            try {
                // 查询提交记录
                HomeworkSubmission submission = homeworkSubmissionDao.queryByHomeworkAndStudent(
                    homeworkId, item.getStudentId());
                
                if (submission == null) {
                    log.warn("学生未提交作业, studentId: {}, homeworkId: {}", item.getStudentId(), homeworkId);
                    continue;
                }
                
                // 验证分数
                if (submission.getMaxScore() != null && 
                    item.getScore().compareTo(submission.getMaxScore()) > 0) {
                    log.warn("分数超过最高分, studentId: {}, score: {}, maxScore: {}", 
                        item.getStudentId(), item.getScore(), submission.getMaxScore());
                    continue;
                }
                
                // 设置批改信息
                submission.setScore(item.getScore());
                submission.setTeacherComment(item.getTeacherComment());
                submission.setGradeTime(now);
                submission.setGradeUser(teacherId);
                submission.setStatus(1); // 已批改
                submission.setUpdateTime(now);
                submission.setUpdateUser(teacherId);
                
                if (homeworkSubmissionDao.update(submission) > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批改作业失败, studentId: {}", item.getStudentId(), e);
            }
        }
        
        return successCount;
    }

    /**
     * 教师分页查询作业的提交列表
     */
    @Override
    public PageInfo<HomeworkSubmissionVO> getHomeworkSubmissionPage(HomeworkSubmissionQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        
        List<HomeworkSubmission> list = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        List<HomeworkSubmissionVO> voList = list.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageInfo<>(voList);
    }

    /**
     * 获取作业提交统计信息
     */
    @Override
    public HomeworkSubmissionStatsVO getHomeworkSubmissionStatistics(Integer homeworkId) {
        Map<String, Object> stats = homeworkSubmissionDao.getSubmissionStats(homeworkId);
        
        HomeworkSubmissionStatsVO vo = new HomeworkSubmissionStatsVO();
        vo.setTotalSubmissions(getLongValue(stats, "totalSubmissions"));
        vo.setGradedCount(getLongValue(stats, "gradedCount"));
        vo.setUngradedCount(getLongValue(stats, "ungradedCount"));
        vo.setAvgScore(getBigDecimalValue(stats, "avgScore"));
        
        // 计算额外统计信息
        calculateAdditionalStats(homeworkId, vo);
        
        return vo;
    }

    /**
     * 分页获取待批改的提交列表
     */
    @Override
    public PageInfo<HomeworkSubmissionVO> getPendingGradeSubmissionPage(HomeworkSubmissionQueryDTO queryDTO, 
                                                                        Integer teacherId) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        List<HomeworkSubmission> list = homeworkSubmissionDao.getPendingGradeSubmissions(teacherId);
        List<HomeworkSubmissionVO> voList = list.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageInfo<>(voList);
    }

    // ========== 管理员端服务 ==========

    /**
     * 管理员分页查询所有提交
     */
    @Override
    public PageInfo<HomeworkSubmissionVO> getAllSubmissionPage(HomeworkSubmissionQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Map<String, Object> params = buildQueryParams(queryDTO);
        
        List<HomeworkSubmission> list = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        List<HomeworkSubmissionVO> voList = list.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageInfo<>(voList);
    }

    /**
     * 根据ID获取提交详情
     */
    @Override
    public HomeworkSubmissionDetailVO getSubmissionDetail(Integer submissionId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryById(submissionId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        return convertToDetailVO(submission);
    }

    /**
     * 管理员删除提交记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSubmission(Integer submissionId) {
        HomeworkSubmission submission = homeworkSubmissionDao.queryById(submissionId);
        if (submission == null) {
            throw new MyException("提交记录不存在");
        }
        return homeworkSubmissionDao.deleteById(submissionId) > 0;
    }

    /**
     * 管理员批量删除提交记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteSubmissions(HomeworkSubmissionBatchDeleteDTO deleteDTO) {
        if (deleteDTO.getIds() == null || deleteDTO.getIds().isEmpty()) {
            return 0;
        }
        return homeworkSubmissionDao.batchDeleteByIds(deleteDTO.getIds());
    }

    /**
     * 获取提交统计信息（支持时间范围）
     */
    @Override
    public HomeworkSubmissionStatsVO getSubmissionStatistics(Integer homeworkId, String startDate, String endDate) {
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        if (homeworkId != null) {
            params.put("homeworkId", homeworkId);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            params.put("startDate", startDate.trim());
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            params.put("endDate", endDate.trim());
        }
        
        // 查询提交列表
        List<HomeworkSubmission> submissions = homeworkSubmissionDao.queryByParams(params);
        
        // 计算统计信息
        HomeworkSubmissionStatsVO vo = new HomeworkSubmissionStatsVO();
        vo.setTotalSubmissions((long) submissions.size());
        
        long gradedCount = submissions.stream().filter(s -> s.getStatus() == 1).count();
        vo.setGradedCount(gradedCount);
        vo.setUngradedCount(vo.getTotalSubmissions() - gradedCount);
        
        // 计算平均分、最高分、最低分
        List<HomeworkSubmission> gradedSubmissions = submissions.stream()
            .filter(s -> s.getScore() != null)
            .collect(Collectors.toList());
        
        if (!gradedSubmissions.isEmpty()) {
            OptionalDouble avgScore = gradedSubmissions.stream()
                .mapToDouble(s -> s.getScore().doubleValue())
                .average();
            vo.setAvgScore(avgScore.isPresent() ? 
                BigDecimal.valueOf(avgScore.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
            
            Optional<BigDecimal> maxScore = gradedSubmissions.stream()
                .map(HomeworkSubmission::getScore)
                .max(BigDecimal::compareTo);
            vo.setMaxScore(maxScore.orElse(null));
            
            Optional<BigDecimal> minScore = gradedSubmissions.stream()
                .map(HomeworkSubmission::getScore)
                .min(BigDecimal::compareTo);
            vo.setMinScore(minScore.orElse(null));
            
            // 计算及格率和优秀率（假设60分及格，85分优秀）
            long passCount = gradedSubmissions.stream()
                .filter(s -> s.getScore().compareTo(new BigDecimal("60")) >= 0)
                .count();
            vo.setPassRate(BigDecimal.valueOf(passCount * 100.0 / gradedSubmissions.size())
                .setScale(2, RoundingMode.HALF_UP));
            
            long excellentCount = gradedSubmissions.stream()
                .filter(s -> s.getScore().compareTo(new BigDecimal("85")) >= 0)
                .count();
            vo.setExcellentRate(BigDecimal.valueOf(excellentCount * 100.0 / gradedSubmissions.size())
                .setScale(2, RoundingMode.HALF_UP));
        }
        
        return vo;
    }

    /**
     * 导出作业提交数据
     */
    @Override
    public void exportSubmissions(HomeworkSubmissionQueryDTO queryDTO, HttpServletResponse response) {
        log.info("导出作业提交数据, queryDTO: {}", queryDTO);
        
        // 1. 根据查询条件获取数据
        Map<String, Object> params = buildQueryParams(queryDTO);
        List<HomeworkSubmission> submissions = homeworkSubmissionDao.querySubmissionsWithStudentInfo(params);
        
        // 2. 转换为导出格式
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
            data.put("status", submission.getStatus() == 1 ? "已批改" : "待批改");
            data.put("submitTime", submission.getSubmitTime());
            data.put("gradeTime", submission.getGradeTime());
            return data;
        }).collect(Collectors.toList());
        
        // 3. 定义表头和数据键
        List<String> headers = Arrays.asList(
            "学生ID", "学生姓名", "作业标题", "提交内容", "附件URL", 
            "得分", "满分", "教师评语", "状态", "提交时间", "批改时间"
        );
        List<String> dataKeys = Arrays.asList(
            "studentId", "studentName", "homeworkTitle", "content", "attachmentUrl",
            "score", "maxScore", "teacherComment", "status", "submitTime", "gradeTime"
        );
        
        // 4. 生成Excel文件（保存到临时目录）
        String fileName = com.jieni.mqxq.util.ExcelExportUtil.generateFileName("作业提交记录");
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + fileName + ".xlsx";
        
        com.jieni.mqxq.util.ExcelExportUtil.exportToFile(
            filePath, "作业提交记录", headers, dataKeys, exportData
        );
        com.jieni.mqxq.util.ExcelExportUtil.writeFileToResponseAndDelete(response, filePath, fileName);
        
        log.info("作业提交数据导出成功并已触发下载, 文件路径: {}", filePath);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 构建查询参数Map
     */
    private Map<String, Object> buildQueryParams(HomeworkSubmissionQueryDTO queryDTO) {
        Map<String, Object> params = new HashMap<>();
        
        if (queryDTO.getHomeworkId() != null) {
            params.put("homeworkId", queryDTO.getHomeworkId());
        }
        if (queryDTO.getStudentId() != null) {
            params.put("studentId", queryDTO.getStudentId());
        }
        if (queryDTO.getStatus() != null) {
            params.put("status", queryDTO.getStatus());
        }
        if (queryDTO.getStudentName() != null && !queryDTO.getStudentName().trim().isEmpty()) {
            params.put("studentName", queryDTO.getStudentName().trim());
        }
        if (queryDTO.getHomeworkTitle() != null && !queryDTO.getHomeworkTitle().trim().isEmpty()) {
            params.put("homeworkTitle", queryDTO.getHomeworkTitle().trim());
        }
        
        return params;
    }

    /**
     * 将Entity转换为VO
     */
    private HomeworkSubmissionVO convertToVO(HomeworkSubmission submission) {
        if (submission == null) {
            return null;
        }
        
        HomeworkSubmissionVO vo = new HomeworkSubmissionVO();
        BeanUtil.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 将Entity转换为DetailVO
     */
    private HomeworkSubmissionDetailVO convertToDetailVO(HomeworkSubmission submission) {
        if (submission == null) {
            return null;
        }
        
        HomeworkSubmissionDetailVO vo = new HomeworkSubmissionDetailVO();
        BeanUtil.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 从Map中获取Long类型值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 从Map中获取BigDecimal类型值
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 计算额外的统计信息
     */
    private void calculateAdditionalStats(Integer homeworkId, HomeworkSubmissionStatsVO vo) {
        // 查询所有已批改的提交
        List<HomeworkSubmission> gradedSubmissions = homeworkSubmissionDao.queryByHomeworkId(homeworkId).stream()
            .filter(s -> s.getScore() != null)
            .collect(Collectors.toList());
        
        if (gradedSubmissions.isEmpty()) {
            return;
        }
        
        // 计算最高分和最低分
        Optional<BigDecimal> maxScore = gradedSubmissions.stream()
            .map(HomeworkSubmission::getScore)
            .max(BigDecimal::compareTo);
        vo.setMaxScore(maxScore.orElse(null));
        
        Optional<BigDecimal> minScore = gradedSubmissions.stream()
            .map(HomeworkSubmission::getScore)
            .min(BigDecimal::compareTo);
        vo.setMinScore(minScore.orElse(null));
        
        // 计算及格率（60分及格）
        long passCount = gradedSubmissions.stream()
            .filter(s -> s.getScore().compareTo(new BigDecimal("60")) >= 0)
            .count();
        vo.setPassRate(BigDecimal.valueOf(passCount * 100.0 / gradedSubmissions.size())
            .setScale(2, RoundingMode.HALF_UP));
        
        // 计算优秀率（85分优秀）
        long excellentCount = gradedSubmissions.stream()
            .filter(s -> s.getScore().compareTo(new BigDecimal("85")) >= 0)
            .count();
        vo.setExcellentRate(BigDecimal.valueOf(excellentCount * 100.0 / gradedSubmissions.size())
            .setScale(2, RoundingMode.HALF_UP));
    }
}
