package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.role.RoleCreateDTO;
import com.jieni.mqxq.domain.dto.role.RoleQueryDTO;
import com.jieni.mqxq.domain.dto.role.RoleUpdateDTO;
import com.jieni.mqxq.domain.vo.role.RoleVO;
import com.jieni.mqxq.service.auth.RoleService;
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
 * 角色管理控制器
 * 
 * 提供管理员端系统角色的全面管理功能，包括角色的创建、查询、更新和删除等CRUD操作。
 * 支持分页查询、按名称和代码搜索，保护系统内置角色不被误删。
 * 确保只有管理员角色可以访问，包含完善的参数验证和异常处理。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/role")
@Tag(name = "角色管理", description = "管理员端角色管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 获取所有角色列表
     */
    @Operation(summary = "获取所有角色", description = "获取系统中的所有角色列表")
    @GetMapping("/list")
    public Result<List<RoleVO>> getAllRoles() {
        List<RoleVO> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 分页查询角色列表
     */
    @Operation(summary = "分页查询角色", description = "支持按角色名称和代码搜索的分页查询")
    @GetMapping("/page")
    public Result<PageInfo<RoleVO>> getRolePage(@Valid RoleQueryDTO queryDTO) {
        PageInfo<RoleVO> pageInfo = roleService.getRolePage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID获取角色详情
     */
    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色详细信息")
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(
            @Parameter(description = "角色ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        RoleVO roleVO = roleService.getRoleById(id);
        return Result.success(roleVO);
    }

    /**
     * 创建新角色
     */
    @Operation(summary = "创建角色", description = "创建新的系统角色")
    @PostMapping("/add")
    public Result<RoleVO> createRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色创建信息")
            @Valid @RequestBody RoleCreateDTO createDTO) {
        RoleVO roleVO = roleService.createRole(createDTO);
        return Result.success("角色创建成功", roleVO);
    }

    /**
     * 更新角色信息
     */
    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping("/{id}")
    public Result<RoleVO> updateRole(
            @Parameter(description = "角色ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色更新信息")
            @Valid @RequestBody RoleUpdateDTO updateDTO) {
        RoleVO roleVO = roleService.updateRole(id, updateDTO);
        return Result.success("角色更新成功", roleVO);
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "删除指定的系统角色（系统内置角色不能删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "ID必须大于0") Integer id) {
        roleService.deleteRole(id);
        return Result.success("角色删除成功");
    }

    /**
     * 根据角色代码获取角色
     */
    @Operation(summary = "根据代码获取角色", description = "根据角色代码获取角色信息")
    @GetMapping("/code/{roleCode}")
    public Result<RoleVO> getRoleByCode(
            @Parameter(description = "角色代码", required = true, in = ParameterIn.PATH, example = "ADMIN")
            @PathVariable String roleCode) {
        RoleVO roleVO = roleService.getRoleByCode(roleCode);
        if (roleVO != null) {
            return Result.success(roleVO);
        } else {
            return Result.error("角色不存在");
        }
    }
}
