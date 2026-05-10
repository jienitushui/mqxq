package com.jieni.mqxq.service.auth;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.role.RoleCreateDTO;
import com.jieni.mqxq.domain.dto.role.RoleQueryDTO;
import com.jieni.mqxq.domain.dto.role.RoleUpdateDTO;
import com.jieni.mqxq.domain.entity.Role;
import com.jieni.mqxq.domain.vo.role.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 * 
 * 提供角色管理的业务逻辑接口，包括角色的创建、查询、更新和删除等核心功能。
 * 支持分页查询、按条件搜索、按代码查询等多种查询方式。
 * 为系统的角色权限管理提供完整的业务服务支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface RoleService {

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID，必须大于0
     * @return 角色视图对象
     */
    RoleVO getRoleById(Integer id);

    /**
     * 创建新角色
     * 
     * @param createDTO 角色创建数据传输对象
     * @return 创建成功的角色视图对象
     */
    RoleVO createRole(RoleCreateDTO createDTO);

    /**
     * 更新角色信息
     *
     * @param id 角色ID，必须大于0
     * @param updateDTO 角色更新数据传输对象
     * @return 更新后的角色视图对象
     */
    RoleVO updateRole(Integer id, RoleUpdateDTO updateDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID，必须大于0
     */
    void deleteRole(Integer id);

    /**
     * 根据角色代码获取角色
     *
     * @param roleCode 角色代码
     * @return 角色视图对象，如果不存在返回null
     */
    RoleVO getRoleByCode(String roleCode);

    /**
     * 获取所有角色列表
     *
     * @return 角色视图对象列表
     */
    List<RoleVO> getAllRoles();

    /**
     * 分页查询角色列表
     *
     * @param queryDTO 查询条件和分页参数
     * @return 角色分页结果
     */
    PageInfo<RoleVO> getRolePage(RoleQueryDTO queryDTO);

    /**
     * 根据角色代码获取角色实体（内部使用）
     * 
     * @param roleCode 角色代码
     * @return 角色实体对象，如果不存在返回null
     */
    Role getRoleEntityByCode(String roleCode);
}
