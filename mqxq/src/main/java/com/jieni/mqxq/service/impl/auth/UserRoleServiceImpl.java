package com.jieni.mqxq.service.impl.auth;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.entity.UserRole;
import com.jieni.mqxq.dao.UserRoleDao;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.auth.UserRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联服务实现类
 * 
 * 提供用户与角色关联关系的完整管理服务，包括权限分配、角色继承等
 * 支持用户角色的增删改查、批量操作以及角色信息查询
 * 实现基于RBAC模型的权限控制和用户权限管理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class UserRoleServiceImpl implements UserRoleService {
    @Resource
    private UserRoleDao userRoleDao;

    // ==================== 基础CRUD操作 ====================

    /**
     * 根据关联ID查询用户角色关联详情
     * 
     * @param id 用户角色关联记录ID
     * @return UserRole 用户角色关联实体对象
     */
    @Override
    public UserRole findUserRoleById(Integer id) {
        validateId(id, "用户角色关联记录ID");
        return this.userRoleDao.queryById(id);
    }

    /**
     * 分页查询用户角色关联记录
     * 
     * @param userRole 筛选条件对象
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @return PageInfo<UserRole> 分页查询结果
     */
    @Override
    public PageInfo<UserRole> findUserRolesByPage(UserRole userRole, Integer pageNum, Integer pageSize) {
        validatePageParams(pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        List<UserRole> list = this.userRoleDao.queryAllByLimit(userRole);
        return new PageInfo<>(list);
    }

    /**
     * 创建用户角色关联记录
     * 
     * @param userRole 用户角色关联对象
     * @return UserRole 创建成功的关联对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRole createUserRole(UserRole userRole) {
        validateUserRoleForCreate(userRole);
        
        // 设置创建和更新时间
        Date now = new Date();
        userRole.setCreateTime(now);
        userRole.setUpdateTime(now);
        
        this.userRoleDao.insert(userRole);
        log.info("创建用户角色关联成功，用户ID：{}，角色ID：{}", userRole.getUserId(), userRole.getRoleId());
        return userRole;
    }

    /**
     * 更新用户角色关联记录
     * 
     * @param userRole 要更新的关联对象
     * @return UserRole 更新后的关联对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRole updateUserRole(UserRole userRole) {
        validateUserRoleForUpdate(userRole);
        
        // 设置更新时间
        userRole.setUpdateTime(new Date());
        
        this.userRoleDao.update(userRole);
        log.info("更新用户角色关联成功，关联ID：{}", userRole.getId());
        return this.findUserRoleById(userRole.getId());
    }

    /**
     * 删除指定的用户角色关联记录
     * 
     * @param id 要删除的关联记录ID
     * @return boolean 删除是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserRoleById(Integer id) {
        validateId(id, "用户角色关联记录ID");
        
        boolean result = this.userRoleDao.deleteById(id) > 0;
        if (result) {
            log.info("删除用户角色关联成功，关联ID：{}", id);
        }
        return result;
    }

    // ==================== 批量操作 ====================

    /**
     * 批量创建用户角色关联记录
     * 
     * @param userRoles 用户角色关联对象列表
     * @return boolean 创建是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchCreateUserRoles(List<UserRole> userRoles) {
        if (CollectionUtils.isEmpty(userRoles)) {
            throw new MyException("用户角色关联列表不能为空");
        }
        
        // 验证每个对象
        for (UserRole userRole : userRoles) {
            validateUserRoleForCreate(userRole);
        }
        
        // 设置时间戳
        Date now = new Date();
        userRoles.forEach(userRole -> {
            userRole.setCreateTime(now);
            userRole.setUpdateTime(now);
        });
        
        int result = this.userRoleDao.insertBatch(userRoles);
        log.info("批量创建用户角色关联成功，创建数量：{}", result);
        return result > 0;
    }

    /**
     * 删除指定用户的所有角色关联
     * 
     * @param userId 用户ID
     * @return boolean 删除是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserRolesByUserId(Integer userId) {
        validateId(userId, "用户ID");
        
        boolean result = this.userRoleDao.deleteByUserId(userId) > 0;
        if (result) {
            log.info("删除用户所有角色关联成功，用户ID：{}", userId);
        }
        return result;
    }

    /**
     * 批量删除多个用户的角色关联
     * 
     * @param userIds 用户ID列表
     * @return boolean 删除是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRemoveUserRolesByUserIds(List<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new MyException("用户ID列表不能为空");
        }
        
        // 验证每个用户ID
        for (Integer userId : userIds) {
            validateId(userId, "用户ID");
        }
        
        int result = this.userRoleDao.deleteByUserIds(userIds);
        log.info("批量删除用户角色关联成功，删除数量：{}", result);
        return result > 0;
    }

    // ==================== 角色权限查询 ====================

    /**
     * 获取用户的角色名称列表
     * 
     * @param userId 用户ID
     * @return List<String> 角色名称列表
     */
    @Override
    public List<String> getUserRoleNames(Integer userId) {
        validateId(userId, "用户ID");
        return this.userRoleDao.getUserRoleNames(userId);
    }

    /**
     * 获取用户的角色代码列表
     * 
     * @param userId 用户ID
     * @return List<String> 角色代码列表
     */
    @Override
    public List<String> getUserRoleCodes(Integer userId) {
        validateId(userId, "用户ID");
        return this.userRoleDao.selectRolesByUserId(userId);
    }

    /**
     * 检查用户是否拥有指定角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return boolean 是否拥有该角色
     */
    @Override
    public boolean hasUserRole(Integer userId, String roleCode) {
        validateId(userId, "用户ID");
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new MyException("角色代码不能为空");
        }
        
        List<String> userRoles = getUserRoleCodes(userId);
        return userRoles.contains(roleCode);
    }

    /**
     * 根据用户ID查询用户角色关联列表
     * 
     * @param userId 用户ID
     * @return List<UserRole> 用户角色关联列表
     */
    @Override
    public List<UserRole> findUserRolesByUserId(Integer userId) {
        validateId(userId, "用户ID");
        
        UserRole queryCondition = new UserRole();
        queryCondition.setUserId(userId);
        return this.userRoleDao.queryAllByLimit(queryCondition);
    }

    /**
     * 根据角色ID查询用户角色关联列表
     * 
     * @param roleId 角色ID
     * @return List<UserRole> 用户角色关联列表
     */
    @Override
    public List<UserRole> findUserRolesByRoleId(Integer roleId) {
        validateId(roleId, "角色ID");
        
        UserRole queryCondition = new UserRole();
        queryCondition.setRoleId(roleId);
        return this.userRoleDao.queryAllByLimit(queryCondition);
    }

    // ==================== 角色分配管理 ====================

    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return boolean 分配是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Integer userId, String roleCode) {
        validateId(userId, "用户ID");
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new MyException("角色代码不能为空");
        }
        
        // 检查是否已经拥有该角色
        if (hasUserRole(userId, roleCode)) {
            log.warn("用户已拥有该角色，用户ID：{}，角色代码：{}", userId, roleCode);
            return true;
        }
        
        int result = this.userRoleDao.insertUserRole(userId, roleCode);
        if (result > 0) {
            log.info("为用户分配角色成功，用户ID：{}，角色代码：{}", userId, roleCode);
        }
        return result > 0;
    }

    /**
     * 为用户分配多个角色
     * 
     * @param userId 用户ID
     * @param roleCodes 角色代码列表
     * @return boolean 分配是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Integer userId, List<String> roleCodes) {
        validateId(userId, "用户ID");
        if (CollectionUtils.isEmpty(roleCodes)) {
            throw new MyException("角色代码列表不能为空");
        }
        
        boolean allSuccess = true;
        for (String roleCode : roleCodes) {
            if (!assignRoleToUser(userId, roleCode)) {
                allSuccess = false;
            }
        }
        
        log.info("为用户批量分配角色完成，用户ID：{}，成功：{}", userId, allSuccess);
        return allSuccess;
    }

    /**
     * 撤销用户的指定角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return boolean 撤销是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeRoleFromUser(Integer userId, String roleCode) {
        validateId(userId, "用户ID");
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new MyException("角色代码不能为空");
        }
        
        // 查找用户角色关联记录
        List<UserRole> userRoles = findUserRolesByUserId(userId);
        List<UserRole> toDelete = userRoles.stream()
                .filter(ur -> roleCode.equals(getRoleCodeByRoleId(ur.getRoleId())))
                .collect(Collectors.toList());
        
        if (toDelete.isEmpty()) {
            log.warn("用户不拥有该角色，用户ID：{}，角色代码：{}", userId, roleCode);
            return true;
        }
        
        // 删除角色关联
        boolean result = true;
        for (UserRole userRole : toDelete) {
            if (!removeUserRoleById(userRole.getId())) {
                result = false;
            }
        }
        
        log.info("撤销用户角色完成，用户ID：{}，角色代码：{}，成功：{}", userId, roleCode, result);
        return result;
    }

    /**
     * 重置用户角色（先删除所有角色，再分配新角色）
     * 
     * @param userId 用户ID
     * @param roleCodes 新的角色代码列表
     * @return boolean 重置是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserRoles(Integer userId, List<String> roleCodes) {
        validateId(userId, "用户ID");
        
        // 先删除所有角色关联
        removeUserRolesByUserId(userId);
        
        // 再分配新角色
        if (!CollectionUtils.isEmpty(roleCodes)) {
            return assignRolesToUser(userId, roleCodes);
        }
        
        log.info("重置用户角色完成，用户ID：{}", userId);
        return true;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证ID参数
     */
    private void validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new MyException(fieldName + "不能为空或小于等于0");
        }
    }

    /**
     * 验证分页参数
     */
    private void validatePageParams(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            throw new MyException("页码必须大于0");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new MyException("每页记录数必须在1-100之间");
        }
    }

    /**
     * 验证创建用户角色关联的参数
     */
    private void validateUserRoleForCreate(UserRole userRole) {
        if (userRole == null) {
            throw new MyException("用户角色关联对象不能为空");
        }
        validateId(userRole.getUserId(), "用户ID");
        validateId(userRole.getRoleId(), "角色ID");
    }

    /**
     * 验证更新用户角色关联的参数
     */
    private void validateUserRoleForUpdate(UserRole userRole) {
        if (userRole == null) {
            throw new MyException("用户角色关联对象不能为空");
        }
        validateId(userRole.getId(), "用户角色关联记录ID");
    }

    /**
     * 根据角色ID获取角色代码（需要调用RoleService，这里暂时返回空字符串）
     * TODO: 需要注入RoleService来获取角色代码
     */
    private String getRoleCodeByRoleId(Integer roleId) {
        // 这里需要调用RoleService来获取角色代码
        // 暂时返回空字符串，实际实现时需要注入RoleService
        return "";
    }
}
