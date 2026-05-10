package com.jieni.mqxq.service.impl.auth;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.enums.UserTypeEnum;
import com.jieni.mqxq.dao.UserDao;
import com.jieni.mqxq.domain.dto.user.*;
import com.jieni.mqxq.domain.entity.Role;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.domain.entity.UserRole;
import com.jieni.mqxq.domain.vo.user.UserDetailVO;
import com.jieni.mqxq.domain.vo.user.UserStatisticsVO;
import com.jieni.mqxq.domain.vo.user.UserVo;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.auth.RoleService;
import com.jieni.mqxq.service.auth.UserRoleService;
import com.jieni.mqxq.service.auth.UserService;
import com.jieni.mqxq.service.infrastructure.MinIOFileStorageService;
import com.jieni.mqxq.util.SaUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * 提供用户的完整业务逻辑实现，包括用户的增删改查、角色管理、状态管理、头像上传等功能。
 * 支持分页查询、按类型筛选、批量操作、密码重置等特性。集成MinIO文件存储和角色权限管理。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    /** 默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";
    
    /** 默认角色代码 */
    private static final String DEFAULT_ROLE_CODE = "USER";
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private MinIOFileStorageService minIOFileStorageService;
    
    @Resource
    private UserRoleService userRoleService;
    
    @Resource
    private RoleService roleService;

    /**
     * 分页查询用户列表
     * 
     * 支持关键词搜索和用户类型筛选的分页查询
     * DTO中已包含验证规则，无需手动验证
     * 
     * @param queryDTO 查询条件DTO
     * @return 分页用户列表
     */
    @Override
    public PageInfo<UserVo> getUserListPage(UserQueryDTO queryDTO) {
        log.debug("查询用户列表，条件：{}", queryDTO);
        
        // 启动分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 根据条件查询
        List<User> users;
        String keyword = queryDTO.getKeyword();
        String userType = queryDTO.getUserType();
        
        if (userType != null && !userType.trim().isEmpty()) {
            users = userDao.queryByKeywordAndType(keyword, userType);
        } else {
            users = userDao.queryByKeyword(keyword);
        }
        
        // 转换为VO并获取角色信息
        List<UserVo> userVos = users.stream()
                .map(this::convertToUserVo)
                .collect(Collectors.toList());
        
        return new PageInfo<>(userVos);
    }

    /**
     * 根据ID获取用户详情
     * 
     * 获取用户的完整信息，包括基本信息、角色列表、用户类型等
     * 
     * @param id 用户ID
     * @return 用户详情VO
     */
    @Override
    public UserDetailVO getUserDetailById(Integer id) {
        log.debug("获取用户详情，ID：{}", id);
        
        // 参数验证
        if (id == null || id <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        
        // 查询用户
        User user = userDao.queryById(id);
        if (user == null) {
            throw new MyException("用户不存在");
        }
        
        // 转换为详情VO
        return convertToUserDetailVO(user);
    }

    /**
     * 创建新用户
     * 
     * 根据DTO中的信息创建新用户，自动分配角色，设置默认密码（如果未提供）
     * 已在Controller层通过@Valid验证DTO，此处处理业务逻辑
     * 
     * @param createUserDTO 创建用户DTO
     * @return 创建成功的用户详情VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetailVO createUser(CreateUserDTO createUserDTO) {
        log.info("创建新用户，用户名：{}", createUserDTO.getUsername());
        
        // 检查用户名是否已存在
        User existingUser = userDao.selectByUsername(createUserDTO.getUsername());
        if (existingUser != null) {
            throw new MyException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        List<User> existingPhones = userDao.queryAllByLimit(new User() {{
            setPhone(createUserDTO.getPhone());
        }});
        if (existingPhones != null && !existingPhones.isEmpty()) {
            throw new MyException("手机号已被使用");
        }
        
        // 构建用户实体
        User user = new User();
        user.setUsername(createUserDTO.getUsername());
        user.setName(createUserDTO.getName());
        user.setPhone(createUserDTO.getPhone());
        user.setAvatar(createUserDTO.getAvatar());
        user.setStatus(1); // 默认启用
        
        // 设置密码（如果未提供则使用默认密码）
        String password = createUserDTO.getPassword();
        if (password == null || password.trim().isEmpty()) {
            password = DEFAULT_PASSWORD;
        }
        user.setPassword(SaUtil.toBcPassword(password));
        
        // 获取当前登录用户ID
        Integer currentUserId = SaUtil.getLoginId();
        
        // 设置创建时间和更新时间
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setCreateUser(currentUserId);
        user.setUpdateUser(currentUserId);
        
        // 插入用户
        userDao.insert(user);
        log.info("用户创建成功，ID：{}", user.getId());
        
        // 分配角色
        List<String> roleCodes = createUserDTO.getRoles();
        if (roleCodes == null || roleCodes.isEmpty()) {
            roleCodes = List.of(DEFAULT_ROLE_CODE);
        }
        
        assignRolesToUser(user.getId(), roleCodes);
        
        // 返回用户详情
        return getUserDetailById(user.getId());
    }

    /**
     * 更新用户信息
     * 
     * 更新用户的基本信息，支持部分字段更新，包括角色更新
     * 已在Controller层通过@Valid验证DTO
     * 
     * @param updateUserDTO 更新用户DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UpdateUserDTO updateUserDTO) {
        log.info("更新用户信息，ID：{}", updateUserDTO.getId());
        
        // 检查用户是否存在
        User user = userDao.queryById(updateUserDTO.getId());
        if (user == null) {
            throw new MyException("用户不存在");
        }
        
        // 更新字段（只更新非空字段）
        if (updateUserDTO.getName() != null) {
            user.setName(updateUserDTO.getName());
        }
        if (updateUserDTO.getPhone() != null) {
            user.setPhone(updateUserDTO.getPhone());
        }
        if (updateUserDTO.getAvatar() != null) {
            user.setAvatar(updateUserDTO.getAvatar());
        }
        
        // 设置更新时间和更新人
        user.setUpdateTime(new Date());
        user.setUpdateUser(SaUtil.getLoginId());
        
        // 更新用户
        userDao.update(user);
        
        // 更新角色（如果提供了角色列表）
        if (updateUserDTO.getRoles() != null) {
            log.info("更新用户角色，用户ID：{}，新角色：{}", updateUserDTO.getId(), updateUserDTO.getRoles());
            userRoleService.resetUserRoles(updateUserDTO.getId(), updateUserDTO.getRoles());
            log.info("用户角色更新成功，用户ID：{}", updateUserDTO.getId());
        }
        
        log.info("用户信息更新成功，ID：{}", updateUserDTO.getId());
    }

    /**
     * 删除用户
     * 
     * 删除指定ID的用户及其所有关联数据（包括角色关系）
     * 
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Integer id) {
        log.info("删除用户，ID：{}", id);
        
        // 参数验证
        if (id == null || id <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        
        // 检查用户是否存在
        User user = userDao.queryById(id);
        if (user == null) {
            throw new MyException("用户不存在");
        }
        
        // 先删除用户角色关联
        userRoleService.removeUserRolesByUserId(id);
        
        // 再删除用户
        int rows = userDao.deleteById(id);
        if (rows == 0) {
            throw new MyException("删除用户失败");
        }
        
        log.info("用户删除成功，ID：{}", id);
    }

    /**
     * 批量删除用户
     * 
     * 批量删除多个用户及其关联数据
     * 已在Controller层通过@Valid验证DTO
     * 
     * @param batchDeleteDTO 批量删除DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(BatchDeleteDTO batchDeleteDTO) {
        List<Integer> ids = batchDeleteDTO.getIds();
        log.info("批量删除用户，数量：{}", ids.size());
        
        // 验证所有ID是否有效
        for (Integer id : ids) {
            if (id == null || id <= 0) {
                throw new MyException("用户ID列表中包含无效的ID");
            }
        }
        
        // 先删除用户角色关联
        userRoleService.batchRemoveUserRolesByUserIds(ids);
        
        // 再批量删除用户
        int rows = userDao.deleteBatch(ids);
        log.info("批量删除用户成功，删除数量：{}", rows);
    }

    /**
     * 重置用户密码
     * 
     * 将用户密码重置为系统默认密码（123456）
     * 
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Integer id) {
        log.info("重置用户密码，ID：{}", id);
        
        // 参数验证
        if (id == null || id <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        
        // 检查用户是否存在
        User user = userDao.queryById(id);
        if (user == null) {
            throw new MyException("用户不存在");
        }
        
        // 重置密码
        String encryptedPassword = SaUtil.toBcPassword(DEFAULT_PASSWORD);
        user.setPassword(encryptedPassword);
        user.setUpdateTime(new Date());
        user.setUpdateUser(SaUtil.getLoginId());
        
        userDao.update(user);
        log.info("用户密码重置成功，ID：{}", id);
    }

    /**
     * 更新用户状态
     * 
     * 启用或禁用用户账户
     * 已在Controller层通过@Valid验证DTO
     * 
     * @param statusDTO 状态更新DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(UpdateUserStatusDTO statusDTO) {
        log.info("更新用户状态，ID：{}，状态：{}", statusDTO.getId(), statusDTO.getStatus());
        
        // 检查用户是否存在
        User user = userDao.queryById(statusDTO.getId());
        if (user == null) {
            throw new MyException("用户不存在");
        }
        
        // 更新状态
        user.setStatus(statusDTO.getStatus());
        user.setUpdateTime(new Date());
        user.setUpdateUser(SaUtil.getLoginId());
        
        userDao.update(user);
        log.info("用户状态更新成功，ID：{}，状态：{}", statusDTO.getId(), statusDTO.getStatus());
    }

    /**
     * 获取用户类型统计
     * 
     * 统计系统中各种用户类型的数量
     * 
     * @return 用户类型统计VO列表
     */
    @Override
    public List<UserStatisticsVO> getUserTypeStatisticsList() {
        log.debug("获取用户类型统计");
        
        // 从DAO获取原始统计数据
        List<Map<String, Object>> rawStatistics = userDao.getUserTypeStatistics();
        
        // 转换为VO
        return rawStatistics.stream()
                .map(map -> {
                    UserStatisticsVO vo = new UserStatisticsVO();
                    String userTypeCode = (String) map.get("userTypeCode");
                    Long count = ((Number) map.get("count")).longValue();
                    
                    // 设置类型代码和数量
                    vo.setUserTypeCode(userTypeCode);
                    vo.setCount(count);
                    
                    // 根据角色代码获取用户类型名称
                    UserTypeEnum roleEnum = UserTypeEnum.getByCode(userTypeCode);
                    if (roleEnum != null) {
                        vo.setUserTypeName(roleEnum.getName());
                    } else {
                        vo.setUserTypeName("未知");
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 上传用户头像
     * 
     * 上传并保存用户头像图片到MinIO对象存储
     * 
     * @param id 用户ID
     * @param bytes 头像图片字节数组
     * @return 头像访问URL
     */
    @Override
    public String uploadUserAvatar(String id, byte[] bytes) {
        log.info("上传用户头像，用户ID：{}", id);
        
        // 参数验证
        if (id == null || id.trim().isEmpty()) {
            throw new MyException("用户ID不能为空");
        }
        if (bytes == null || bytes.length == 0) {
            throw new MyException("头像图片数据不能为空");
        }
        
        // 上传到MinIO
        String url = "head/" + id + ".png";
        String avatarUrl = minIOFileStorageService.uploadCompressImg(url, bytes);
        
        log.info("用户头像上传成功，URL：{}", avatarUrl);
        return avatarUrl;
    }

    /**
     * 获取用户角色名称列表
     * 
     * 获取指定用户的所有角色名称
     * 
     * @param id 用户ID
     * @return 角色名称列表
     */
    @Override
    public List<String> getUserRoleNames(Integer id) {
        log.debug("获取用户角色，ID：{}", id);
        
        // 参数验证
        if (id == null || id <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        
        return userRoleService.getUserRoleNames(id);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将User实体转换为UserVo视图对象
     * 
     * @param user 用户实体对象
     * @return 用户视图对象
     */
    private UserVo convertToUserVo(User user) {
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        userVo.setStatus(user.getStatus());
        
        // 获取用户角色
        List<String> roles = userRoleService.getUserRoleNames(user.getId());
        userVo.setRoleList(roles);
        
        return userVo;
    }

    /**
     * 将User实体转换为UserDetailVO详情对象
     * 
     * @param user 用户实体对象
     * @return 用户详情VO
     */
    private UserDetailVO convertToUserDetailVO(User user) {
        UserDetailVO detailVO = new UserDetailVO();
        
        // 复制基本属性
        BeanUtils.copyProperties(user, detailVO);
        
        // 获取角色信息
        List<String> roleNames = userRoleService.getUserRoleNames(user.getId());
        if (roleNames != null && !roleNames.isEmpty()) {
            detailVO.setRoleList(roleNames);
            
            // 获取角色代码（暂时使用角色名称）
            detailVO.setRoleCodes(roleNames);
            
            // 确定用户类型
            UserTypeEnum.UserType userType = UserTypeEnum.getByRoles(roleNames);
            detailVO.setUserType(userType.getName());
            detailVO.setUserTypeCode(userType.getCode());
        } else {
            // 无角色时设置默认值
            detailVO.setRoleList(Collections.emptyList());
            detailVO.setRoleCodes(Collections.emptyList());
            detailVO.setUserType(UserTypeEnum.STUDENT.getName());
            detailVO.setUserTypeCode(UserTypeEnum.STUDENT.getCode());
        }
        
        return detailVO;
    }

    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleCodes 角色代码列表
     */
    private void assignRolesToUser(Integer userId, List<String> roleCodes) {
        Date now = new Date();
        
        for (String roleCode : roleCodes) {
            Role role = roleService.getRoleEntityByCode(roleCode);
            if (role != null) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(role.getId());
                userRole.setCreateTime(now);
                userRole.setUpdateTime(now);
                userRoleService.createUserRole(userRole);
                log.debug("为用户分配角色，用户ID：{}，角色：{}", userId, roleCode);
            } else {
                log.warn("角色不存在，跳过分配，角色代码：{}", roleCode);
            }
        }
    }

    /**
     * 根据ID查询用户
     * 
     * 根据用户ID查询用户实体对象
     * 
     * @param id 用户ID，必须大于0
     * @return 用户实体对象，不存在则返回null
     */
    @Override
    public User queryById(Integer id) {
        if (id == null || id <= 0) {
            log.warn("查询用户失败，ID参数无效：{}", id);
            return null;
        }
        return userDao.queryById(id);
    }
}
