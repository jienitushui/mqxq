package com.jieni.mqxq.controller.publicapi;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.vo.content.AnnouncementCategoryVO;
import com.jieni.mqxq.service.content.AnnouncementCategoriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告分类公共接口控制器
 * 
 * 提供无需登录即可访问的公告分类查询功能，包括分类列表查询、分类详情查看等。
 * 为公告分类下拉选择、筛选查询等场景提供基础数据支持，确保所有用户都可以正常查看公告分类信息。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Tag(name = "公告分类查看", description = "查看公告分类信息")
@Slf4j
@RestController
@Validated
@RequestMapping("/api/public/announcement-categories")
@SaCheckRole("用户")
public class AnnouncementCategoriesPublicController {

    @Resource
    private AnnouncementCategoriesService announcementCategoriesService;

    /**
     * 获取所有公告分类列表
     * 用于公告分类下拉选择、筛选等场景
     */
    @Operation(summary = "获取所有公告分类", description = "获取所有可用的公告分类列表")
    @GetMapping("/all")
    public Result<List<AnnouncementCategoryVO>> getAllCategories() {
        List<AnnouncementCategoryVO> categories = announcementCategoriesService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 根据ID查询公告分类详情
     */
    @Operation(summary = "查询公告分类详情", description = "根据ID查询单个公告分类的详细信息")
    @GetMapping("/{id}")
    public Result<AnnouncementCategoryVO> getCategoryById(
            @Parameter(description = "分类ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "分类ID必须大于0") Integer id) {
        AnnouncementCategoryVO vo = announcementCategoriesService.getCategoryById(id);
        return Result.success(vo);
    }
}
