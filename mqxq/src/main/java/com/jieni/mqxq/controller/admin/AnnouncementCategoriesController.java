package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryCreateDTO;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryPageQueryDTO;
import com.jieni.mqxq.domain.dto.content.AnnouncementCategoryUpdateDTO;
import com.jieni.mqxq.domain.vo.content.AnnouncementCategoryVO;
import com.jieni.mqxq.service.content.AnnouncementCategoriesService;
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

import java.util.List;

/**
 * 公告分类管理控制器
 * 
 * 提供管理员端公告分类的完整CRUD操作，包括创建、查询、更新和删除公告分类
 * 支持分页查询和条件搜索，确保只有管理员角色可以访问
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Tag(name = "公告分类管理", description = "公告分类的增删改查操作")
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/announcement-categories")
@SaCheckRole("管理员")
public class AnnouncementCategoriesController {

    @Resource
    private AnnouncementCategoriesService announcementCategoriesService;

    /**
     * 创建公告分类
     */
    @Operation(summary = "创建公告分类", description = "管理员创建新的公告分类")
    @PostMapping
    public Result<AnnouncementCategoryVO> createCategory(@Valid @RequestBody AnnouncementCategoryCreateDTO createDTO) {
        Integer createUserId = Integer.valueOf(SaUtil.getLoginId());
        AnnouncementCategoryVO vo = announcementCategoriesService.createCategory(createDTO, createUserId);
        return Result.success(vo);
    }

    /**
     * 更新公告分类
     */
    @Operation(summary = "更新公告分类", description = "管理员更新现有公告分类信息")
    @PutMapping("/{id}")
    public Result<AnnouncementCategoryVO> updateCategory(
            @Parameter(description = "分类ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "分类ID必须大于0") Integer id,
            @Valid @RequestBody AnnouncementCategoryUpdateDTO updateDTO) {
        Integer updateUserId = Integer.valueOf(SaUtil.getLoginId());
        AnnouncementCategoryVO vo = announcementCategoriesService.updateCategory(id, updateDTO, updateUserId);
        return Result.success(vo);
    }

    /**
     * 删除公告分类
     */
    @Operation(summary = "删除公告分类", description = "管理员删除指定公告分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "分类ID必须大于0") Integer id) {
        announcementCategoriesService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 根据ID查询公告分类
     */
    @Operation(summary = "查询公告分类", description = "根据ID查询单个公告分类")
    @GetMapping("/{id}")
    public Result<AnnouncementCategoryVO> getCategory(
            @Parameter(description = "分类ID", required = true, in = ParameterIn.PATH, example = "1") 
            @PathVariable @Min(value = 1, message = "分类ID必须大于0") Integer id) {
        AnnouncementCategoryVO vo = announcementCategoriesService.getCategoryById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询公告分类列表
     */
    @Operation(summary = "分页查询公告分类", description = "分页查询所有公告分类")
    @GetMapping("/page")
    public Result<PageInfo<AnnouncementCategoryVO>> getCategoryPage(@Valid AnnouncementCategoryPageQueryDTO queryDTO) {
        PageInfo<AnnouncementCategoryVO> pageInfo = announcementCategoriesService.getCategoryPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取所有公告分类（不分页）
     */
    @Operation(summary = "获取所有公告分类", description = "获取所有公告分类，用于下拉选择")
    @GetMapping("/all")
    public Result<List<AnnouncementCategoryVO>> getAllCategories() {
        List<AnnouncementCategoryVO> categories = announcementCategoriesService.getAllCategories();
        return Result.success(categories);
    }
}
