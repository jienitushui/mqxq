package com.jieni.mqxq.service.auth;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联服务接口
 * 
 * 提供用户与角色关联关系的完整管理服务，包括权限分配、角色继承等
 * 支持用户角色的增删改查、批量操作以及角色信息查询
 * 实现基于RBAC模型的权限控制和用户权限管理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface UserRoleService {

    // ==================== 基础CRUD操作 ====================

    /**
     * 根据关联ID查询用户角色关联详情
     * 
     * @param id 用户角色关联记录ID
     * @return UserRole 用户角色关联实体对象
     */
    UserRole findUserRoleById(Integer id);

    /**
     * 分页查询用户角色关联记录
     * 
     * @param userRole 筛选条件对象
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @return PageInfo<UserRole> 分页查询结果
     */
    PageInfo<UserRole> findUserRolesByPage(UserRole userRole, Integer pageNum, Integer pageSize);

    /**
     * 创建用户角色关联记录
     * 
     * @param userRole 用户角色关联对象
     * @return UserRole 创建成功的关联对象
     */
    UserRole createUserRole(UserRole userRole);

    /**
     * 更新用户角色关联记录
     * 
     * @param userRole 要更新的关联对象
     * @return UserRole 更新后的关联对象
     */
    UserRole updateUserRole(UserRole userRole);

    /**
     * 删除指定的用户角色关联记录
     * 
     * @param id 要删除的关联记录ID
     * @return boolean 删除是否成功
     */
    boolean removeUserRoleById(Integer id);

    // ==================== 批量操作 ====================

    /**
     * 批量创建用户角色关联记录
     * 
     * @param userRoles 用户角色关联对象列表
     * @return boolean 创建是否成功
     */
    boolean batchCreateUserRoles(List<UserRole> userRoles);

    /**
     * 删除指定用户的所有角色关联
     * 
     * @param userId 用户ID
     * @return boolean 删除是否成功
     */
    boolean removeUserRolesByUserId(Integer userId);

    /**
     * 批量删除多个用户的角色关联
     * 
     * @param userIds 用户ID列表
     * @return boolean 删除是否成功
     */
    boolean batchRemoveUserRolesByUserIds(List<Integer> userIds);

    // ==================== 角色权限查询 ====================

    /**
     * 获取用户的角色名称列表
     * 
     * @param userId 用户ID
     * @return List<String> 角色名称列表
     */
    List<String> getUserRoleNames(Integer userId);

    /**
     * 获取用户的角色代码列表
     * 
     * @param userId 用户ID
     * @return List<String> 角色代码列表
     */
    List<String> getUserRoleCodes(Integer userId);

    /**
     * 检查用户是否拥有指定角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return boolean 是否拥有该角色
     */
    boolean hasUserRole(Integer userId, String roleCode);

    /**
     * 根据用户ID查询用户角色关联列表
     * 
     * @param userId 用户ID
     * @return List<UserRole> 用户角色关联列表
     */
    List<UserRole> findUserRolesByUserId(Integer userId);

    /**
     * 根据角色ID查询用户角色关联列表
     * 
     * @param roleId 角色ID
     * @return List<UserRole> 用户角色关联列表
     */
    List<UserRole> findUserRolesByRoleId(Integer roleId);

    // ==================== 角色分配管理 ====================

    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return boolean 分配是否成功
     */
    boolean assignRoleToUser(Integer userId, String roleCode);

    /**
     * 为用户分配多个角色
     * 
     * @param userId 用户ID
     * @param roleCodes 角色代码列表
     * @return boolean 分配是否成功
     */
    boolean assignRolesToUser(Integer userId, List<String> roleCodes);

    /**
     * 撤销用户的指定角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return boolean 撤销是否成功
     */
    boolean revokeRoleFromUser(Integer userId, String roleCode);

    /**
     * 重置用户角色（先删除所有角色，再分配新角色）
     * 
     * @param userId 用户ID
     * @param roleCodes 新的角色代码列表
     * @return boolean 重置是否成功
     */
    boolean resetUserRoles(Integer userId, List<String> roleCodes);
}
