package com.jieni.mqxq.controller.publicapi;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.content.AnnouncementPageQueryDTO;
import com.jieni.mqxq.domain.dto.content.AnnouncementSearchDTO;
import com.jieni.mqxq.domain.vo.content.AnnouncementSimpleVO;
import com.jieni.mqxq.domain.vo.content.AnnouncementVO;
import com.jieni.mqxq.service.content.AnnouncementsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告公共接口控制器
 * 
 * 提供无需登录即可访问的公告查询功能，包括公告列表查询、公告详情查看、最新公告、搜索等。
 * 只显示已发布状态的公告，支持分页查询、按分类筛选、关键词搜索等功能。为平台的公告信息展示提供支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Tag(name = "公共公告查看", description = "查看已发布的公告信息")
@Slf4j
@RestController
@Validated
@RequestMapping("/api/public/announcements")
@SaCheckRole("用户")
public class AnnouncementsPublicController {

    @Resource
    private AnnouncementsService announcementsService;

    /**
     * 分页查询已发布的公告列表
     */
    @Operation(summary = "分页查询已发布公告", description = "分页查询所有已发布的公告，支持按分类、标题等条件筛选")
    @GetMapping("/page")
    public Result<PageInfo<AnnouncementSimpleVO>> getPublishedAnnouncementPage(@Valid AnnouncementPageQueryDTO queryDTO) {
        PageInfo<AnnouncementSimpleVO> pageInfo = announcementsService.getPublishedAnnouncementPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID查询已发布的公告详情
     */
    @Operation(summary = "查询已发布公告详情", description = "根据ID查询单个已发布公告的详细信息")
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getPublishedAnnouncement(
            @Parameter(description = "公告ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "公告ID必须大于0") Integer id) {
        AnnouncementVO vo = announcementsService.getPublishedAnnouncementById(id);
        return Result.success(vo);
    }

    /**
     * 获取最新公告列表
     */
    @Operation(summary = "获取最新公告", description = "获取最新发布的几条公告")
    @GetMapping("/latest")
    public Result<List<AnnouncementSimpleVO>> getLatestAnnouncements(
            @Parameter(description = "获取数量", example = "5") 
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "获取数量必须大于0") @Max(value = 20, message = "获取数量不能超过20") Integer count) {
        List<AnnouncementSimpleVO> announcements = announcementsService.getLatestAnnouncements(count);
        return Result.success(announcements);
    }

    /**
     * 根据分类获取公告列表
     */
    @Operation(summary = "按分类获取公告", description = "根据分类ID获取该分类下的所有已发布公告")
    @GetMapping("/category/{categoryId}")
    public Result<List<AnnouncementSimpleVO>> getAnnouncementsByCategory(
            @Parameter(description = "分类ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "分类ID必须大于0") Integer categoryId) {
        List<AnnouncementSimpleVO> announcements = announcementsService.getPublishedAnnouncementsByCategory(categoryId);
        return Result.success(announcements);
    }

    /**
     * 搜索公告
     */
    @Operation(summary = "搜索公告", description = "根据关键词搜索已发布的公告")
    @GetMapping("/search")
    public Result<PageInfo<AnnouncementSimpleVO>> searchAnnouncements(@Valid AnnouncementSearchDTO searchDTO) {
        PageInfo<AnnouncementSimpleVO> pageInfo = announcementsService.searchPublishedAnnouncements(searchDTO);
        return Result.success(pageInfo);
    }
}
