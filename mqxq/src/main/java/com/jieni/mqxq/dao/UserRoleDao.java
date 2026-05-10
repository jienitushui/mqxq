package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.UserRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 用户角色关联数据访问接口
 * 
 * 负责用户与角色之间的关联关系管理，包括角色分配、权限查询、关联关系维护等操作
 * 支持用户角色的增删改查以及基于用户ID的角色权限查询功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface UserRoleDao {

    // ==================== 基础CRUD操作 ====================

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserRole queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param userRole 查询条件
     * @return 对象列表
     */
    List<UserRole> queryAllByLimit(UserRole userRole);

    /**
     * 统计总行数
     *
     * @param userRole 查询条件
     * @return 总行数
     */
    long count(UserRole userRole);

    /**
     * 新增数据
     *
     * @param userRole 实例对象
     * @return 影响行数
     */
    int insert(UserRole userRole);

    /**
     * 修改数据
     *
     * @param userRole 实例对象
     * @return 影响行数
     */
    int update(UserRole userRole);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    // ==================== 批量操作 ====================

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<UserRole> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<UserRole> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<UserRole> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<UserRole> entities);

    /**
     * 批量删除用户角色关联记录
     *
     * @param ids 关联记录ID列表
     * @return 影响行数
     */
    int deleteBatchByIds(@Param("ids") List<Integer> ids);

    // ==================== 用户角色关联管理 ====================

    /**
     * 通过用户ID删除用户角色关联
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Integer userId);

    /**
     * 通过用户ID列表批量删除用户角色关联
     *
     * @param userIds 用户ID列表
     * @return 影响行数
     */
    int deleteByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * 通过角色ID删除用户角色关联
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Integer roleId);

    /**
     * 通过角色ID列表批量删除用户角色关联
     *
     * @param roleIds 角色ID列表
     * @return 影响行数
     */
    int deleteByRoleIds(@Param("roleIds") List<Integer> roleIds);

    // ==================== 角色权限查询 ====================

    /**
     * 查询用户的角色名称列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getUserRoleNames(@Param("userId") Integer userId);

    /**
     * 查询用户的角色代码列表
     *
     * @param userId 用户ID
     * @return 角色代码列表
     */
    List<String> selectRolesByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Integer> getUserRoleIds(@Param("userId") Integer userId);

    /**
     * 查询拥有指定角色的用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Integer> getUserIdsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 查询拥有指定角色代码的用户ID列表
     *
     * @param roleCode 角色代码
     * @return 用户ID列表
     */
    List<Integer> getUserIdsByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return 是否存在关联
     */
    boolean existsUserRole(@Param("userId") Integer userId, @Param("roleCode") String roleCode);

    /**
     * 统计用户拥有的角色数量
     *
     * @param userId 用户ID
     * @return 角色数量
     */
    int countUserRoles(@Param("userId") Integer userId);

    /**
     * 统计拥有指定角色的用户数量
     *
     * @param roleId 角色ID
     * @return 用户数量
     */
    int countUsersByRoleId(@Param("roleId") Integer roleId);

    // ==================== 角色分配操作 ====================

    /**
     * 插入用户角色关联（通过角色代码）
     *
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return 影响行数
     */
    int insertUserRole(@Param("userId") Integer userId, @Param("roleCode") String roleCode);

    /**
     * 批量插入用户角色关联（通过角色代码）
     *
     * @param userId 用户ID
     * @param roleCodes 角色代码列表
     * @return 影响行数
     */
    int insertUserRoles(@Param("userId") Integer userId, @Param("roleCodes") List<String> roleCodes);

    /**
     * 删除用户指定角色关联（通过角色代码）
     *
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return 影响行数
     */
    int deleteUserRole(@Param("userId") Integer userId, @Param("roleCode") String roleCode);

    /**
     * 删除用户多个角色关联（通过角色代码）
     *
     * @param userId 用户ID
     * @param roleCodes 角色代码列表
     * @return 影响行数
     */
    int deleteUserRoles(@Param("userId") Integer userId, @Param("roleCodes") List<String> roleCodes);

    // ==================== 高级查询 ====================

    /**
     * 分页查询用户角色关联详情（包含用户和角色信息）
     *
     * @param userRole 查询条件
     * @return 用户角色关联详情列表
     */
    List<UserRole> queryUserRoleDetails(UserRole userRole);

    /**
     * 查询用户角色关联统计信息
     *
     * @return 统计信息列表
     */
    List<Object> queryUserRoleStatistics();

    /**
     * 查询最近分配的角色关联记录
     *
     * @param limit 限制数量
     * @return 用户角色关联列表
     */
    List<UserRole> queryRecentUserRoles(@Param("limit") Integer limit);
}

