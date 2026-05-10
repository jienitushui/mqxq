package com.jieni.mqxq.service.auth;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.user.*;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.domain.vo.user.UserDetailVO;
import com.jieni.mqxq.domain.vo.user.UserStatisticsVO;
import com.jieni.mqxq.domain.vo.user.UserVo;

import java.util.List;

/**
 * 用户管理服务接口
 * 
 * 提供用户管理的完整服务功能，包括用户的CRUD操作、角色分配、密码管理、状态控制等。
 * 支持多角色用户系统（管理员、教师、学生）、用户统计分析、头像上传等功能，为平台的用户管理和权限控制提供核心服务。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface UserService {

    /**
     * 分页查询用户列表
     * 
     * 支持关键词搜索和用户类型筛选的分页查询，为用户管理提供灵活的数据检索功能
     * 
     * @param queryDTO 查询条件DTO，包含分页参数、关键词和用户类型
     * @return 分页用户列表，包含用户基本信息和角色信息
     */
    PageInfo<UserVo> getUserListPage(UserQueryDTO queryDTO);

    /**
     * 根据ID获取用户详情
     * 
     * 获取用户的完整信息，包括基本信息、角色列表、用户类型等
     * 
     * @param id 用户ID，必须大于0
     * @return 用户详情VO，包含完整的用户信息和角色数据
     */
    UserDetailVO getUserDetailById(Integer id);

    /**
     * 创建新用户
     * 
     * 根据DTO中的信息创建新用户，自动分配角色，设置默认密码（如果未提供）
     * 
     * @param createUserDTO 创建用户DTO，包含用户基本信息和角色列表
     * @return 创建成功的用户详情VO
     */
    UserDetailVO createUser(CreateUserDTO createUserDTO);

    /**
     * 更新用户信息
     * 
     * 更新用户的基本信息，支持部分字段更新
     * 
     * @param updateUserDTO 更新用户DTO，包含待更新的字段
     */
    void updateUserInfo(UpdateUserDTO updateUserDTO);

    /**
     * 删除用户
     * 
     * 删除指定ID的用户及其所有关联数据（包括角色关系）
     * 
     * @param id 用户ID，必须大于0
     */
    void deleteUser(Integer id);

    /**
     * 批量删除用户
     * 
     * 批量删除多个用户及其关联数据
     * 
     * @param batchDeleteDTO 批量删除DTO，包含用户ID列表
     */
    void batchDeleteUsers(BatchDeleteDTO batchDeleteDTO);

    /**
     * 重置用户密码
     * 
     * 将用户密码重置为系统默认密码（123456）
     * 
     * @param id 用户ID，必须大于0
     */
    void resetUserPassword(Integer id);

    /**
     * 更新用户状态
     * 
     * 启用或禁用用户账户
     * 
     * @param statusDTO 状态更新DTO，包含用户ID和状态值
     */
    void updateUserStatus(UpdateUserStatusDTO statusDTO);

    /**
     * 获取用户类型统计
     * 
     * 统计系统中各种用户类型（管理员、教师、普通用户）的数量
     * 
     * @return 用户类型统计VO列表，包含类型名称和数量
     */
    List<UserStatisticsVO> getUserTypeStatisticsList();

    /**
     * 上传用户头像
     * 
     * 上传并保存用户头像图片到MinIO对象存储
     * 
     * @param id 用户ID
     * @param bytes 头像图片字节数组
     * @return 头像访问URL
     */
    String uploadUserAvatar(String id, byte[] bytes);

    /**
     * 获取用户角色名称列表
     * 
     * 获取指定用户的所有角色名称
     * 
     * @param id 用户ID，必须大于0
     * @return 角色名称列表
     */
    List<String> getUserRoleNames(Integer id);

    /**
     * 根据ID查询用户
     * 
     * 根据用户ID查询用户实体对象
     * 
     * @param id 用户ID，必须大于0
     * @return 用户实体对象，不存在则返回null
     */
    User queryById(Integer id);
}
