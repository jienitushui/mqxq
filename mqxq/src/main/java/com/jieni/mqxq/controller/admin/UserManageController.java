package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.user.*;
import com.jieni.mqxq.domain.vo.user.UserDetailVO;
import com.jieni.mqxq.domain.vo.user.UserStatisticsVO;
import com.jieni.mqxq.domain.vo.user.UserVo;
import com.jieni.mqxq.service.auth.UserService;
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
 * 用户管理控制器
 * 
 * 提供管理员端用户的全面管理功能，包括用户的创建、查询、更新、删除等CRUD操作
 * 支持按角色类型筛选、关键词搜索、分页查询、批量操作等高级功能
 * 包含用户状态管理、密码重置、角色分配等安全管理功能
 * 确保只有管理员角色可以访问，包含完善的参数验证和异常处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "用户管理", description = "管理员端用户管理接口")
@CrossOrigin
@SaCheckRole("管理员")
@Validated
public class UserManageController {

    @Resource
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "分页获取所有用户信息，支持关键词搜索和用户类型筛选")
    @GetMapping("/list")
    public Result<PageInfo<UserVo>> getUserListPage(@Valid UserQueryDTO queryDTO) {
        log.debug("查询用户列表：{}", queryDTO);
        PageInfo<UserVo> pageInfo = userService.getUserListPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息，包含角色和用户类型")
    @GetMapping("/{id}")
    public Result<UserDetailVO> getUserDetail(
            @Parameter(description = "用户ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "用户ID必须大于0") Integer id) {
        log.debug("获取用户详情，ID：{}", id);
        UserDetailVO userDetail = userService.getUserDetailById(id);
        return Result.success(userDetail);
    }

    /**
     * 创建新用户
     */
    @Operation(summary = "创建新用户", description = "管理员创建新用户，可指定角色，支持设置初始密码")
    @PostMapping("/create")
    public Result<UserDetailVO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        log.info("创建新用户：{}", createUserDTO.getUsername());
        UserDetailVO userDetail = userService.createUser(createUserDTO);
        return Result.success("用户创建成功", userDetail);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "管理员更新用户基本信息，包括角色更新，不包括密码（密码需要专门的接口）")
    @PutMapping("/update")
    public Result<Void> updateUser(@Valid @RequestBody UpdateUserDTO updateUserDTO) {
        log.info("更新用户信息，ID：{}", updateUserDTO.getId());
        userService.updateUserInfo(updateUserDTO);
        return Result.success("用户信息更新成功");
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "管理员删除指定用户及其关联数据")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "用户ID必须大于0") Integer id) {
        log.info("删除用户，ID：{}", id);
        userService.deleteUser(id);
        return Result.success("用户删除成功");
    }

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户", description = "管理员批量删除多个用户")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteUsers(@Valid @RequestBody BatchDeleteDTO batchDeleteDTO) {
        log.info("批量删除用户，数量：{}", batchDeleteDTO.getIds().size());
        userService.batchDeleteUsers(batchDeleteDTO);
        return Result.success("批量删除用户成功");
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码", description = "管理员将用户密码重置为默认密码(123456)")
    @PutMapping("/reset-password/{id}")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "用户ID必须大于0") Integer id) {
        log.info("重置用户密码，ID：{}", id);
        userService.resetUserPassword(id);
        return Result.success("密码重置成功，新密码为：123456");
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "管理员启用或禁用用户账户")
    @PutMapping("/status")
    public Result<Void> updateUserStatus(@Valid @RequestBody UpdateUserStatusDTO statusDTO) {
        log.info("更新用户状态，ID：{}，状态：{}", statusDTO.getId(), statusDTO.getStatus());
        userService.updateUserStatus(statusDTO);
        String message = statusDTO.getStatus() == 1 ? "用户启用成功" : "用户禁用成功";
        return Result.success(message);
    }

    /**
     * 获取用户类型统计
     */
    @Operation(summary = "获取用户类型统计", description = "获取系统中各类用户类型的数量统计")
    @GetMapping("/statistics")
    public Result<List<UserStatisticsVO>> getUserTypeStatistics() {
        log.debug("获取用户类型统计");
        List<UserStatisticsVO> statistics = userService.getUserTypeStatisticsList();
        return Result.success(statistics);
    }

    /**
     * 获取用户角色列表
     */
    @Operation(summary = "获取用户角色", description = "获取指定用户的角色信息")
    @GetMapping("/roles/{id}")
    public Result<List<String>> getUserRoles(
            @Parameter(description = "用户ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "用户ID必须大于0") Integer id) {
        log.debug("获取用户角色，ID：{}", id);
        List<String> roles = userService.getUserRoleNames(id);
        return Result.success(roles);
    }
}
