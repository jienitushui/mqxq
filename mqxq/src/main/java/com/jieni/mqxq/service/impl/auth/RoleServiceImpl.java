package com.jieni.mqxq.service.impl.auth;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.RoleDao;
import com.jieni.mqxq.domain.dto.role.RoleCreateDTO;
import com.jieni.mqxq.domain.dto.role.RoleQueryDTO;
import com.jieni.mqxq.domain.dto.role.RoleUpdateDTO;
import com.jieni.mqxq.domain.entity.Role;
import com.jieni.mqxq.domain.vo.role.RoleVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.auth.RoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 
 * 提供角色管理的完整业务逻辑实现，包括角色的增删改查、分页查询、权限验证等功能。
 * 实现系统内置角色保护机制，防止误删核心角色。
 * 支持 Entity 与 VO 之间的数据转换，为上层提供清晰的数据接口。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    
    @Resource
    private RoleDao roleDao;

    /** 系统内置角色代码列表，不允许删除 */
    private static final List<String> SYSTEM_ROLE_CODES = Arrays.asList("ADMIN", "USER", "TEACHER");

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID，必须大于0
     * @return 角色视图对象
     */
    @Override
    public RoleVO getRoleById(Integer id) {
        log.debug("根据ID查询角色详情，ID: {}", id);
        
        Role role = roleDao.queryById(id);
        if (role == null) {
            log.warn("角色不存在，ID: {}", id);
            throw new MyException("角色不存在");
        }
        
        return convertToVO(role);
    }

    /**
     * 创建新角色
     *
     * @param createDTO 角色创建数据传输对象
     * @return 创建成功的角色视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(RoleCreateDTO createDTO) {
        log.info("创建新角色，角色名称: {}, 角色代码: {}", createDTO.getRoleName(), createDTO.getRoleCode());
        
        // 验证角色代码是否已存在
        Role existingRole = roleDao.queryByRoleCode(createDTO.getRoleCode());
        if (existingRole != null) {
            log.warn("角色代码已存在: {}", createDTO.getRoleCode());
            throw new MyException("角色代码已存在");
        }
        
        // 创建角色实体
        Role role = new Role();
        BeanUtils.copyProperties(createDTO, role);
        Date now = new Date();
        role.setCreateTime(now);
        role.setUpdateTime(now);
        
        // 保存到数据库
        roleDao.insert(role);
        log.info("角色创建成功，ID: {}", role.getId());
        
        return convertToVO(role);
    }

    /**
     * 更新角色信息
     *
     * @param id 角色ID，必须大于0
     * @param updateDTO 角色更新数据传输对象
     * @return 更新后的角色视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(Integer id, RoleUpdateDTO updateDTO) {
        log.info("更新角色信息，ID: {}", id);
        
        // 验证角色是否存在
        Role existingRole = roleDao.queryById(id);
        if (existingRole == null) {
            log.warn("角色不存在，ID: {}", id);
            throw new MyException("角色不存在");
        }
        
        // 如果修改了角色代码，检查是否已存在
        if (updateDTO.getRoleCode() != null && 
            !updateDTO.getRoleCode().equals(existingRole.getRoleCode())) {
            Role codeCheck = roleDao.queryByRoleCode(updateDTO.getRoleCode());
            if (codeCheck != null) {
                log.warn("角色代码已存在: {}", updateDTO.getRoleCode());
                throw new MyException("角色代码已存在");
            }
        }
        
        // 更新角色信息
        Role role = new Role();
        role.setId(id);
        if (updateDTO.getRoleName() != null) {
            role.setRoleName(updateDTO.getRoleName());
        }
        if (updateDTO.getRoleCode() != null) {
            role.setRoleCode(updateDTO.getRoleCode());
        }
        role.setUpdateTime(new Date());
        
        roleDao.update(role);
        log.info("角色更新成功，ID: {}", id);
        
        // 返回更新后的角色信息
        return getRoleById(id);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID，必须大于0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Integer id) {
        log.info("删除角色，ID: {}", id);
        
        // 验证角色是否存在
        Role role = roleDao.queryById(id);
        if (role == null) {
            log.warn("角色不存在，ID: {}", id);
            throw new MyException("角色不存在");
        }
        
        // 检查是否为系统内置角色，防止删除
        if (isSystemRole(role.getRoleCode())) {
            log.warn("尝试删除系统内置角色: {}", role.getRoleCode());
            throw new MyException("系统内置角色不能删除");
        }
        
        int result = roleDao.deleteById(id);
        if (result <= 0) {
            log.error("角色删除失败，ID: {}", id);
            throw new MyException("角色删除失败");
        }
        
        log.info("角色删除成功，ID: {}", id);
    }

    /**
     * 根据角色代码获取角色
     *
     * @param roleCode 角色代码
     * @return 角色视图对象，如果不存在返回null
     */
    @Override
    public RoleVO getRoleByCode(String roleCode) {
        log.debug("根据角色代码查询角色，角色代码: {}", roleCode);
        
        Role role = roleDao.queryByRoleCode(roleCode);
        if (role == null) {
            log.warn("角色不存在，角色代码: {}", roleCode);
            return null;
        }
        
        return convertToVO(role);
    }

    /**
     * 获取所有角色列表
     *
     * @return 角色视图对象列表
     */
    @Override
    public List<RoleVO> getAllRoles() {
        log.debug("查询所有角色列表");
        
        List<Role> roles = roleDao.queryAll();
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询角色列表
     *
     * @param queryDTO 查询条件和分页参数
     * @return 角色分页结果
     */
    @Override
    public PageInfo<RoleVO> getRolePage(RoleQueryDTO queryDTO) {
        log.debug("分页查询角色，页码: {}, 每页数量: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 构建查询条件
        Role queryCondition = new Role();
        if (queryDTO.getRoleName() != null && !queryDTO.getRoleName().trim().isEmpty()) {
            queryCondition.setRoleName(queryDTO.getRoleName().trim());
        }
        if (queryDTO.getRoleCode() != null && !queryDTO.getRoleCode().trim().isEmpty()) {
            queryCondition.setRoleCode(queryDTO.getRoleCode().trim());
        }
        
        // 执行分页查询
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Role> roles = roleDao.queryAllByCondition(queryCondition);
        PageInfo<Role> pageInfo = new PageInfo<>(roles);
        
        // 转换为VO
        List<RoleVO> voList = roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建分页结果
        PageInfo<RoleVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        
        return result;
    }

    /**
     * 根据角色代码获取角色实体（内部使用）
     * 
     * @param roleCode 角色代码
     * @return 角色实体对象，如果不存在返回null
     */
    @Override
    public Role getRoleEntityByCode(String roleCode) {
        log.debug("根据角色代码查询角色实体，角色代码: {}", roleCode);
        return roleDao.queryByRoleCode(roleCode);
    }

    // ========================= 私有辅助方法 =========================

    /**
     * 将 Role 实体转换为 RoleVO
     *
     * @param role 角色实体
     * @return 角色视图对象
     */
    private RoleVO convertToVO(Role role) {
        if (role == null) {
            return null;
        }
        
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    /**
     * 判断是否为系统内置角色
     *
     * @param roleCode 角色代码
     * @return 是否为系统内置角色
     */
    private boolean isSystemRole(String roleCode) {
        return SYSTEM_ROLE_CODES.contains(roleCode);
    }
}
