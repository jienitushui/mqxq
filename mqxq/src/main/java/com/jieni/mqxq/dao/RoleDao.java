package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Role;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 角色数据访问层接口
 * 
 * 提供角色相关的数据库操作接口，包括角色的增删改查、按代码查询、分页查询等功能。
 * 支持单条查询、批量操作、条件筛选等数据库操作。为角色权限管理系统提供数据层支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface RoleDao {

    /**
     * 根据ID查询单条角色数据
     *
     * @param id 主键ID
     * @return 角色实体对象
     */
    Role queryById(@Param("id") Integer id);

    /**
     * 根据角色代码查询角色
     *
     * @param roleCode 角色代码
     * @return 角色实体对象
     */
    Role queryByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> queryAll();

    /**
     * 根据条件查询角色列表（支持模糊查询）
     *
     * @param role 查询条件
     * @return 角色列表
     */
    List<Role> queryAllByCondition(@Param("role") Role role);

    /**
     * 统计符合条件的角色总数
     *
     * @param role 查询条件
     * @return 总数
     */
    long count(@Param("role") Role role);

    /**
     * 新增角色数据
     *
     * @param role 角色实体对象
     * @return 影响行数
     */
    int insert(@Param("role") Role role);

    /**
     * 批量新增角色数据
     *
     * @param entities 角色实体列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Role> entities);

    /**
     * 批量新增或按主键更新角色数据
     *
     * @param entities 角色实体列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(@Param("entities") List<Role> entities);

    /**
     * 更新角色数据
     *
     * @param role 角色实体对象
     * @return 影响行数
     */
    int update(@Param("role") Role role);

    /**
     * 根据ID删除角色
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 批量删除角色
     *
     * @param ids ID列表
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") List<Integer> ids);
}
