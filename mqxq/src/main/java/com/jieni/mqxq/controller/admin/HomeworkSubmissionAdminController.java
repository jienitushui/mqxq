package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionBatchDeleteDTO;
import com.jieni.mqxq.domain.dto.homework.HomeworkSubmissionQueryDTO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionDetailVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionStatsVO;
import com.jieni.mqxq.domain.vo.homework.HomeworkSubmissionVO;
import com.jieni.mqxq.service.homework.HomeworkSubmissionService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 作业提交管理控制器（管理员端）
 * 
 * 提供管理员端作业提交记录的全面管理功能，包括提交记录的查询、删除和统计等操作
 * 支持按作业、学生、状态筛选，提供批量操作、统计分析和数据导出功能
 * 确保只有管理员角色可以访问，包含完善的日志记录和异常处理
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/homework-submission")
@Tag(name = "管理员-作业提交管理", description = "管理员端作业提交管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class HomeworkSubmissionAdminController {

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    /**
     * 分页查询所有作业提交
     */
    @Operation(summary = "作业提交列表", description = "管理员分页查询所有作业提交")
    @GetMapping("/list")
    public Result<PageInfo<HomeworkSubmissionVO>> getSubmissionPage(@Valid HomeworkSubmissionQueryDTO queryDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员查询作业提交列表, adminId: {}, queryDTO: {}", adminId, queryDTO);

        PageInfo<HomeworkSubmissionVO> pageInfo = homeworkSubmissionService.getAllSubmissionPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取提交详情
     */
    @Operation(summary = "提交详情", description = "管理员查看作业提交详情")
    @GetMapping("/{id}")
    public Result<HomeworkSubmissionDetailVO> getSubmissionDetail(
            @Parameter(description = "提交ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员查询提交详情, adminId: {}, submissionId: {}", adminId, id);

        HomeworkSubmissionDetailVO detailVO = homeworkSubmissionService.getSubmissionDetail(id);
        return Result.success(detailVO);
    }

    /**
     * 删除提交记录
     */
    @Operation(summary = "删除提交", description = "管理员删除作业提交记录")
    @DeleteMapping("/{id}")
    public Result<String> deleteSubmission(
            @Parameter(description = "提交ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员删除提交记录, adminId: {}, submissionId: {}", adminId, id);

        homeworkSubmissionService.deleteSubmission(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除提交记录
     */
    @Operation(summary = "批量删除提交", description = "管理员批量删除作业提交记录")
    @DeleteMapping("/batch")
    public Result<String> batchDeleteSubmissions(@Valid @RequestBody HomeworkSubmissionBatchDeleteDTO deleteDTO) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员批量删除提交记录, adminId: {}, ids: {}", adminId, deleteDTO.getIds());

        int count = homeworkSubmissionService.batchDeleteSubmissions(deleteDTO);
        return Result.success("成功删除 " + count + " 条记录");
    }

    /**
     * 获取作业提交统计
     */
    @Operation(summary = "提交统计", description = "管理员查看作业提交统计信息")
    @GetMapping("/statistics")
    public Result<HomeworkSubmissionStatsVO> getSubmissionStatistics(
            @Parameter(description = "作业ID", example = "1") 
            @RequestParam(required = false) @Min(value = 1, message = "作业ID必须大于0") Integer homeworkId,
            @Parameter(description = "开始日期", example = "2025-01-01") 
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期", example = "2025-12-31") 
            @RequestParam(required = false) String endDate) {
        
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员查询提交统计, adminId: {}, homeworkId: {}", adminId, homeworkId);

        HomeworkSubmissionStatsVO statistics = homeworkSubmissionService.getSubmissionStatistics(
                homeworkId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 导出作业提交数据
     */
    @Operation(summary = "导出提交数据", description = "管理员导出作业提交数据")
    @GetMapping("/export")
    public void exportSubmissions(@Valid HomeworkSubmissionQueryDTO queryDTO, HttpServletResponse response) {
        Integer adminId = SaUtil.getLoginId();
        log.info("管理员导出提交数据, adminId: {}, queryDTO: {}", adminId, queryDTO);

        homeworkSubmissionService.exportSubmissions(queryDTO, response);
    }
}
