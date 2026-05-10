package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.content.*;
import com.jieni.mqxq.domain.vo.content.AnnouncementSimpleVO;
import com.jieni.mqxq.domain.vo.content.AnnouncementVO;
import com.jieni.mqxq.service.content.AnnouncementsService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理控制器
 * 
 * 提供管理员端公告的全面管理功能，包括公告的创建、查询、更新、删除等CRUD操作
 * 支持公告发布/撤回、分页查询、按分类筛选和批量操作等高级功能
 * 确保只有管理员角色可以访问，包含完善的参数验证和异常处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Tag(name = "公告管理", description = "公告的增删改查、发布撤回等操作")
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/announcements")
@SaCheckRole("管理员")
public class AnnouncementsController {

    @Resource
    private AnnouncementsService announcementsService;

    /**
     * 创建公告
     */
    @Operation(summary = "创建公告", description = "管理员创建新的公告")
    @PostMapping
    public Result<AnnouncementVO> createAnnouncement(@Valid @RequestBody AnnouncementCreateDTO createDTO) {
        Integer createUserId = Integer.valueOf(SaUtil.getLoginId());
        AnnouncementVO vo = announcementsService.createAnnouncement(createDTO, createUserId);
        return Result.success(vo);
    }

    /**
     * 更新公告
     */
    @Operation(summary = "更新公告", description = "管理员更新现有公告信息")
    @PutMapping("/{id}")
    public Result<AnnouncementVO> updateAnnouncement(
            @Parameter(description = "公告ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "公告ID必须大于0") Integer id,
            @Valid @RequestBody AnnouncementUpdateDTO updateDTO) {
        Integer updateUserId = Integer.valueOf(SaUtil.getLoginId());
        AnnouncementVO vo = announcementsService.updateAnnouncement(id, updateDTO, updateUserId);
        return Result.success(vo);
    }

    /**
     * 删除公告
     */
    @Operation(summary = "删除公告", description = "管理员删除指定公告")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(
            @Parameter(description = "公告ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "公告ID必须大于0") Integer id) {
        announcementsService.deleteAnnouncement(id);
        return Result.success();
    }

    /**
     * 根据ID查询公告
     */
    @Operation(summary = "查询公告", description = "根据ID查询单个公告详情")
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncement(
            @Parameter(description = "公告ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "公告ID必须大于0") Integer id) {
        AnnouncementVO vo = announcementsService.getAnnouncementById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询公告列表
     */
    @Operation(summary = "分页查询公告", description = "分页查询所有公告，支持按分类、标题等条件筛选")
    @GetMapping("/page")
    public Result<PageInfo<AnnouncementSimpleVO>> getAnnouncementPage(@Valid AnnouncementPageQueryDTO queryDTO) {
        PageInfo<AnnouncementSimpleVO> pageInfo = announcementsService.getAnnouncementPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 发布公告
     */
    @Operation(summary = "发布公告", description = "管理员发布公告，设置发布时间")
    @PostMapping("/{id}/publish")
    public Result<AnnouncementVO> publishAnnouncement(
            @Parameter(description = "公告ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "公告ID必须大于0") Integer id,
            @Valid @RequestBody(required = false) AnnouncementPublishDTO publishDTO) {
        if (publishDTO == null) {
            publishDTO = new AnnouncementPublishDTO();
        }
        AnnouncementVO vo = announcementsService.publishAnnouncement(id, publishDTO);
        return Result.success(vo);
    }

    /**
     * 撤回公告
     */
    @Operation(summary = "撤回公告", description = "管理员撤回已发布的公告")
    @PostMapping("/{id}/withdraw")
    public Result<AnnouncementVO> withdrawAnnouncement(
            @Parameter(description = "公告ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "公告ID必须大于0") Integer id) {
        AnnouncementVO vo = announcementsService.withdrawAnnouncement(id);
        return Result.success(vo);
    }

    /**
     * 批量删除公告
     */
    @Operation(summary = "批量删除公告", description = "管理员批量删除多个公告")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteAnnouncements(@Valid @RequestBody AnnouncementBatchDeleteDTO batchDeleteDTO) {
        announcementsService.batchDeleteAnnouncements(batchDeleteDTO.getIds());
        return Result.success();
    }
}
