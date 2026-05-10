package com.jieni.mqxq.service.impl.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.jieni.mqxq.common.constants.LoginConstants;
import com.jieni.mqxq.dao.UserDao;
import com.jieni.mqxq.dao.UserRoleDao;
import com.jieni.mqxq.domain.entity.Role;
import com.jieni.mqxq.domain.entity.UserRole;
import com.jieni.mqxq.service.auth.RoleService;
import com.jieni.mqxq.service.auth.UserService;
import com.jieni.mqxq.domain.dto.auth.LoginRequest;
import com.jieni.mqxq.domain.vo.auth.LoginResponse;
import com.jieni.mqxq.domain.dto.auth.RegisterRequest;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.util.EmailUtils;
import com.jieni.mqxq.util.RabbitMqHelper;
import com.jieni.mqxq.util.RedisUtil;
import com.jieni.mqxq.util.SaUtil;
import com.jieni.mqxq.domain.dto.auth.SendCodeDto;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 统一认证服务增强实现类
 * 
 * 【架构定位】
 * 这是认证系统的核心业务实现类，负责处理所有用户认证相关的业务逻辑
 * 作为Service层的一部分，专注于业务流程编排，不涉及HTTP请求处理
 * 
 * 【核心功能】
 * 1. 用户登录：验证码校验 → 用户认证 → 权限验证 → Token生成
 * 2. 用户注册：参数验证 → 唯一性检查 → 用户创建 → 角色分配
 * 3. 密码管理：修改密码、忘记密码、重置密码
 * 4. 验证码服务：发送验证码（注册、重置密码、更换用户名）
 * 5. 用户信息：获取当前用户信息、更新资料
 * 6. 用户名管理：检查可用性、更换用户名（绑定邮箱）
 * 
 * 【设计原则】
 * - 单一职责：只处理认证业务，不涉及权限策略（由LoginStrategy处理）
 * - 依赖注入：通过@Resource注入所需组件，便于测试和扩展
 * - 异常统一：使用MyException抛出业务异常，由全局异常处理器统一处理
 * - 日志记录：关键操作记录INFO日志，异常记录ERROR日志
 * - 事务管理：修改数据的方法由调用方添加@Transactional
 * 
 * 【依赖组件】
 * - UserDao/UserRoleDao：数据访问层，操作用户和角色数据
 * - UserService/RoleService：用户和角色管理服务
 * - RedisUtil：缓存工具，存储验证码、登录失败计数等
 * - EmailUtils：邮件工具，发送验证码邮件
 * - SaUtil：Sa-Token工具类，密码加密和Token管理
 * 
 * 【安全机制】
 * - 密码加密：BCrypt算法，10轮哈希
 * - 验证码：5分钟有效期，一次性使用
 * - 发送限制：同一用户名60秒内只能发送一次验证码
 * - 用户名格式：必须为邮箱格式（@Email验证）
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class UnifiedAuthServiceEnhanced {

    // ==================== 依赖注入 ====================
    
    /** 用户数据访问对象，操作user表 */
    @Resource
    private UserDao userDao;

    /** 用户服务，提供用户管理功能 */
    @Resource
    private UserService userService;
    
    /** 角色服务，提供角色查询功能 */
    @Resource
    private RoleService roleService;
    
    /** 用户角色关联数据访问对象，操作user_role表 */
    @Resource
    private UserRoleDao userRoleDao;
    
    /** Redis工具类，用于存储验证码、发送限制等临时数据 */
    @Resource
    private RedisUtil redisUtil;
    
    /** 邮件工具类，用于发送验证码邮件 */
    @Resource
    private EmailUtils emailUtils;
    
    /** RabbitMQ消息队列工具类，用于异步发送邮件 */
    @Resource
    private RabbitMqHelper rabbitMqHelper;
    
    /** 线程池执行器，用于异步发送邮件（MQ降级场景） */
    @Resource
    @Qualifier("threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    /** 默认头像路径，从配置文件读取 */
    @Value("${noFoundPath}")
    private String defaultAvatar;

    // ==================== 常量定义 ====================
    
    /** 验证码Redis key前缀 格式：code:{type}:{username} */
    private static final String CODE_PREFIX = "code:";
    
    /** 发送限制Redis key前缀 格式：send_limit:{username} */
    private static final String SEND_LIMIT_PREFIX = "send_limit:";
    
    /** 验证码有效期：5分钟（300秒） */
    private static final int CODE_EXPIRE_TIME = 300;
    
    /** 验证码发送频率限制：60秒 */
    private static final int SEND_LIMIT_TIME = 60;

    // ==================== 核心业务方法 ====================

    /**
     * 用户登录增强实现
     * 
     * 提供完整的用户登录功能，包括验证码检查、密码验证、角色权限检查等
     * 支持多角色登录限制、用户状态检查、登录日志记录等安全特性
     * 
     * 登录流程：
     * 1. 验证图形验证码的有效性
     * 2. 根据用户名查找用户信息
     * 3. 验证用户密码的正确性
     * 4. 检查用户账户状态
     * 5. 验证用户角色权限
     * 6. 执行系统登录并生成Token
     * 7. 构建并返回登录响应信息
     * 
     * @param request 登录请求对象，包含用户名、密码、验证码等信息
     * @param ipAddress 客户端IP地址，用于安全日志记录
     * @return LoginResponse 登录响应对象，包含用户信息、Token、角色等
     * @ 当登录失败时抛出，包括验证码错误、用户名错误、密码错误、权限不足等
     */
    public LoginResponse login(LoginRequest request, String ipAddress)  {
        try {
            // 验证验证码
            validateCaptcha(request.getCaptcha(), request.getCaptchaKey());
            
            // 查找用户
            User user = userDao.selectByUsername(request.getUsername());
            if (user == null) {
                throw new MyException("用户名错误");
            }
            
            // 验证密码
            if (!SaUtil.isBc(request.getPassword(), user.getPassword())) {
                throw new MyException("密码错误");
            }
            
            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new MyException("账户已被禁用");
            }

            // 验证角色
            if (StrUtil.isNotBlank(request.getRole())) {
                List<String> roles = userService.getUserRoleNames(user.getId());
                if (roles == null || !roles.contains(request.getRole())) {
                    throw new MyException("无权限访问");
                }
            }
            
            // 执行登录
            StpUtil.login(user.getId());
            
            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId().longValue());
            response.setUsername(user.getUsername());
            response.setNickname(user.getName());
            response.setPhone(user.getPhone());
            response.setAvatar(user.getAvatar() != null ? user.getAvatar() : defaultAvatar);
            response.setToken(StpUtil.getTokenValue());
            response.setExpiresIn(StpUtil.getTokenTimeout());
            response.setRoles(userService.getUserRoleNames(user.getId()));
            
            // 更新最后登录时间
            user.setUpdateTime(new Date());
            userDao.updateById(user);
            
            log.info("用户登录成功: userId={}, username={}, ip={}", user.getId(), user.getUsername(), ipAddress);
            
            return response;
            
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录异常", e);
            throw new MyException("登录失败");
        }
    }

    /**
     * 用户注册增强实现
     * 
     * 提供完整的用户注册功能，包括密码一致性检查、验证码验证、用户名唯一性检查等
     * 支持验证码验证、默认角色分配、安全密码加密等安全特性
     * 
     * 注册流程：
     * 1. 验证密码一致性（密码和确认密码）
     * 2. 验证图形验证码的有效性
     * 3. 验证用户名验证码的正确性
     * 4. 检查用户名是否已被注册
     * 5. 创建用户账户并加密密码
     * 6. 为新用户分配默认角色（根据userType分配USER或TEACHER角色）
     * 7. 清理验证码并记录注册日志
     * 
     * @param request 注册请求对象，包含用户名、密码、验证码等信息
     * @param ipAddress 客户端IP地址，用于安全日志记录
     * @ 当注册失败时抛出，包括密码不一致、验证码错误、用户名已存在等
     */
    public void register(RegisterRequest request, String ipAddress)  {
        try {
            // 验证密码一致性
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new MyException("两次输入的密码不一致");
            }
            
            // 验证图形验证码
            validateCaptcha(request.getCaptcha(), request.getCaptchaKey());
            
            // 验证用户名验证码
            validateCode(request.getUsername(), request.getCode(), "REGISTER");
            
            // 检查用户名是否存在
            if (!checkUsernameAvailability(request.getUsername())) {
                throw new MyException("用户名已被使用");
            }
            
            // 创建用户
            User user = new User();
            user.setPassword(SaUtil.toBcPassword(request.getPassword()));
            // 确保name不为null，如果为空则使用邮箱前缀作为默认名称
            String userName = request.getName();
            if (StrUtil.isBlank(userName)) {
                // 如果name为空，使用邮箱@前面的部分作为默认名称
                String username = request.getUsername();
                if (StrUtil.isNotBlank(username) && username.contains("@")) {
                    userName = username.substring(0, username.indexOf("@"));
                } else {
                    userName = "用户"; // 最后的默认值
                }
            }
            user.setName(userName);
            user.setUsername(request.getUsername());
            user.setPhone(request.getPhone());
            user.setStatus(1); // 启用状态
            user.setAvatar(defaultAvatar);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());

            
            // 保存用户
            userDao.insert(user);

            // 为新注册用户添加角色（根据userType决定）
            try {
                // 根据用户类型查找角色（USER或TEACHER）
                String roleCode = "USER".equals(request.getUserType()) ? "USER" : "TEACHER";
                Role role = roleService.getRoleEntityByCode(roleCode);
                if (role != null) {
                    // 创建用户角色关联
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(role.getId());
                    userRole.setCreateTime(new Date());
                    userRole.setUpdateTime(new Date());
                    
                    // 保存用户角色关联
                    userRoleDao.insert(userRole);
                    
                    log.info("为用户分配角色成功: userId={}, roleId={}, roleName={}, userType={}", 
                            user.getId(), role.getId(), role.getRoleName(), request.getUserType());
                } else {
                    log.warn("未找到角色({})，用户注册成功但未分配角色: userId={}", roleCode, user.getId());
                }
            } catch (Exception e) {
                log.error("为用户分配角色失败: userId={}, error={}", user.getId(), e.getMessage(), e);
                // 不抛出异常，避免影响用户注册流程
            }
            
            // 清除验证码
            String codeKey = CODE_PREFIX + "REGISTER:" + request.getUsername();
            redisUtil.delete(codeKey);
            
            log.info("用户注册成功: userId={}, username={}, userType={}, ip={}", 
                    user.getId(), user.getUsername(), request.getUserType(), ipAddress);
            
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("注册异常", e);
            throw new MyException("注册失败");
        }
    }

    /**
     * 获取当前登录用户信息
     * 
     * 根据当前登录的Token获取用户的完整信息，包括基本信息和角色权限
     * 用于页面初始化、用户信息展示、权限控制等功能
     * 
     * @return LoginResponse 用户信息响应对象，包含用户ID、用户名、昵称、头像、角色列表等
     * @ 当用户不存在或获取信息失败时抛出
     */
    public LoginResponse getCurrentUserInfo()  {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userDao.selectById(userId.intValue());
            if (user == null) {
                throw new MyException("用户不存在");
            }
            
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId().longValue());
            response.setUsername(user.getUsername());
            response.setNickname(user.getName());
            response.setPhone(user.getPhone());
            response.setAvatar(user.getAvatar() != null ? user.getAvatar() : defaultAvatar);
            response.setToken(StpUtil.getTokenValue());
            response.setExpiresIn(StpUtil.getTokenTimeout());
            response.setRoles(userService.getUserRoleNames(user.getId()));
            
            return response;
            
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            throw new MyException("获取用户信息失败");
        }
    }

    /**
     * 刷新用户登录Token
     * 
     * 刷新当前用户的登录Token有效期，延长用户的登录状态
     * 用于防止用户在操作过程中因Token过期而被迫重新登录
     * 
     * @return LoginResponse 更新后的用户信息，包含新的Token和有效期
     * @ 当Token刷新失败时抛出
     */
    public LoginResponse refreshToken()  {
        try {
            // 刷新Token
            StpUtil.renewTimeout(StpUtil.getTokenTimeout());
            
            // 返回新的用户信息
            return getCurrentUserInfo();
            
        } catch (Exception e) {
            log.error("刷新令牌异常", e);
            throw new MyException("刷新令牌失败");
        }
    }

    /**
     * 修改用户密码
     * 
     * 允许当前登录用户修改自己的登录密码，需要验证原密码的正确性
     * 修改成功后将强制用户重新登录以确保安全性
     * 
     * 修改流程：
     * 1. 验证新密码与确认密码的一致性
     * 2. 获取当前登录用户信息
     * 3. 验证原密码的正确性
     * 4. 更新密码并加密存储
     * 5. 强制用户重新登录
     * 
     * @param oldPassword 原密码，用于验证用户身份
     * @param newPassword 新密码，将被加密存储
     * @param confirmPassword 确认密码，必须与新密码一致
     * @ 当密码修改失败时抛出，包括原密码错误、新密码不一致等
     */
    public void changePassword(String oldPassword, String newPassword, String confirmPassword)  {
        try {
            // 验证新密码一致性
            if (!newPassword.equals(confirmPassword)) {
                throw new MyException("两次输入的新密码不一致");
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userDao.selectById(userId.intValue());
            if (user == null) {
                throw new MyException("用户不存在");
            }
            
            // 验证原密码
            if (!SaUtil.isBc(oldPassword, user.getPassword())) {
                throw new MyException("原密码错误");
            }
            
            // 更新密码
            user.setPassword(SaUtil.toBcPassword(newPassword));
            user.setUpdateTime(new Date());
            userDao.updateById(user);
            
            log.info("用户修改密码成功: userId={}", userId);
            
            // 强制重新登录
            StpUtil.logout();
            
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改密码异常", e);
            throw new MyException("修改密码失败");
        }
    }

    /**
     * 发送验证码
     * 
     * 发送流程：
     * 1. 根据类型验证用户名状态（注册时检查是否已存在，重置密码时检查是否存在）
     * 2. 检查60秒发送频率限制，60秒内只能发送一次
     * 3. 如果超过60秒，允许重新发送（覆盖旧验证码，处理用户没收到邮件的情况）
     * 4. 生成6位数字验证码并存储到Redis（有效期5分钟）
     * 5. 设置60秒发送频率限制
     * 6. 将验证码信息发送到消息队列（异步处理，由EmailExchange监听器负责实际发送邮件）
     * 
     * @param username 用户名（邮箱格式）
     * @param type 验证码类型（REGISTER-注册，RESET_PASSWORD-重置密码，BIND_EMAIL-更换用户名）
     * @throws MyException 当用户名状态不符合要求、发送频率过快或发送失败时抛出
     */
    public void sendEmailCode(String username, String type)  {
        try {
            // 根据类型验证用户名状态
            if ("REGISTER".equals(type)) {
                // 注册时：检查用户名是否已存在
                if (!checkUsernameAvailability(username)) {
                    throw new MyException("该用户名已被注册，请使用其他用户名或直接登录");
                }
            } else if ("RESET_PASSWORD".equals(type)) {
                // 重置密码时：检查用户名是否存在
                if (checkUsernameAvailability(username)) {
                    throw new MyException("该用户名未注册，请先注册账号");
                }
            }
            
            // 检查发送频率限制（60秒内只能发送一次）
            String limitKey = SEND_LIMIT_PREFIX + username;
            if (redisUtil.get(limitKey) != null) {
                throw new MyException("验证码发送过于频繁，请60秒后再试");
            }
            
            // 如果超过60秒，允许重新发送（覆盖旧验证码）
            // 这样可以处理用户没收到邮件的情况
            String key = CODE_PREFIX + type + ":" + username;
            
            // 生成6位数字验证码
            String code = String.format("%06d", new Random().nextInt(999999));
            
            // 存储到Redis
            redisUtil.add(key, code, CODE_EXPIRE_TIME);
            
            // 设置发送频率限制
            redisUtil.add(limitKey, "1", SEND_LIMIT_TIME);
            
            // 构建邮件主题和内容
            String subject = "REGISTER".equals(type) ? "码趣星球 - 注册验证码" 
                    : "RESET_PASSWORD".equals(type) ? "码趣星球 - 密码重置验证码"
                    : "码趣星球 - 绑定邮箱验证码";
            String content = String.format("您的验证码是：%s，有效期5分钟。如非本人操作，请忽略此邮件。", code);
            
            // 尝试发送到消息队列（异步处理）
            try {
                SendCodeDto sendCodeDto = new SendCodeDto(
                        username, code, subject, content
                );
                rabbitMqHelper.sendMessage("mqxq.email", "sendCode", sendCodeDto);
                log.info("验证码已发送到消息队列: username={}, type={}", username, type);
            } catch (Exception mqException) {
                // MQ发送失败，降级为线程池异步发送邮件
                log.warn("消息队列发送失败，降级为线程池异步发送邮件: username={}, error={}", 
                        username, mqException.getMessage());
                final String finalUsername = username;
                final String finalSubject = subject;
                final String finalContent = content;
                final String finalType = type;
                
                // 使用线程池异步发送邮件
                threadPoolTaskExecutor.execute(() -> {
                    try {
                        emailUtils.sendSimpleMail(finalUsername, finalSubject, finalContent);
                        log.info("验证码已通过线程池异步发送成功: username={}, type={}", finalUsername, finalType);
                    } catch (Exception emailException) {
                        log.error("线程池异步发送邮件失败: username={}", finalUsername, emailException);
                        // 异步发送失败不抛出异常，避免影响主流程
                    }
                });
                log.info("验证码发送任务已提交到线程池: username={}, type={}", username, type);
            }
            
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            throw new MyException("发送验证码失败");
        }
    }

    /**
     * 忘记密码
     */
    public void forgotPassword(String username, String code, String newPassword, String confirmPassword)  {
        try {
            // 验证新密码一致性
            if (!newPassword.equals(confirmPassword)) {
                throw new MyException("两次输入的新密码不一致");
            }
            
            // 验证验证码
            validateCode(username, code, "RESET_PASSWORD");
            
            // 查找用户
            User user = userDao.selectByUsername(username);
            if (user == null) {
                throw new MyException("用户名未注册");
            }
            
            // 更新密码
            user.setPassword(SaUtil.toBcPassword(newPassword));
            user.setUpdateTime(new Date());
            userDao.updateById(user);
            
            // 清除验证码
            String key = CODE_PREFIX + "RESET_PASSWORD:" + username;
            redisUtil.delete(key);
            
            log.info("用户重置密码成功: userId={}, username={}", user.getId(), username);
            
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("重置密码异常", e);
            throw new MyException("重置密码失败");
        }
    }

    /**
     * 更新用户资料
     */
    public void updateProfile(String name, String phone, String avatar)  {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userDao.selectById(userId.intValue());
            if (user == null) {
                throw new MyException("用户不存在");
            }
            
            // 更新用户信息
            if (StrUtil.isNotBlank(name)) {
                user.setName(name);
            }
            if (StrUtil.isNotBlank(phone)) {
                user.setPhone(phone);
            }
            if (StrUtil.isNotBlank(avatar)) {
                user.setAvatar(avatar);
            }
            user.setUpdateTime(new Date());
            
            userDao.updateById(user);
            
            log.info("用户更新资料成功: userId={} name={} phone={} avatar={}", userId, name, phone, avatar);
            
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户资料异常", e);
            throw new MyException("更新用户资料失败");
        }
    }

    /**
     * 更换用户名
     */
    public void bindEmail(String username, String code)  {
        try {
            // 验证验证码
            validateCode(username, code, "BIND_EMAIL");
            
            // 检查用户名是否已被使用
            if (!checkUsernameAvailability(username)) {
                throw new MyException("用户名已被使用");
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userDao.selectById(userId.intValue());
            if (user == null) {
                throw new MyException("用户不存在");
            }
            
            // 更新用户名
            user.setUsername(username);
            user.setUpdateTime(new Date());
            userDao.updateById(user);
            
            // 清除验证码
            String key = CODE_PREFIX + "BIND_EMAIL:" + username;
            redisUtil.delete(key);
            
            log.info("用户更换用户名成功: userId={}, newUsername={}", userId, username);
            
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("更换用户名异常", e);
            throw new MyException("更换用户名失败");
        }
    }

    /**
     * 检查用户名是否可用（用户名即邮箱）
     */
    public boolean checkUsernameAvailability(String username) {
        try {
            User user = userDao.selectByUsername(username);
            return user == null;
        } catch (Exception e) {
            log.error("检查用户名异常", e);
            return false;
        }
    }
    


    /**
     * 检查登录状态
     */
    public LoginResponse checkLoginStatus() {
        try {
            if (StpUtil.isLogin()) {
                return getCurrentUserInfo();
            }
            return null;
        } catch (Exception e) {
            log.debug("检查登录状态异常", e);
            return null;
        }
    }

    // 私有辅助方法

    private void validateCaptcha(String captcha, String captchaKey)  {
        if (StrUtil.isBlank(captcha) || StrUtil.isBlank(captchaKey)) {
            throw new MyException("验证码不能为空");
        }
        
        String key = LoginConstants.LOGIN_CAPTCHA + captchaKey;
        String storedCaptcha = (String) redisUtil.get(key);
        if (storedCaptcha == null) {
            throw new MyException("验证码已过期");
        }
        
        if (!captcha.equalsIgnoreCase(storedCaptcha)) {
            throw new MyException("验证码错误");
        }
        
        // 验证成功后删除验证码
        redisUtil.delete(key);
    }

    /**
     * 验证验证码
     */
    private void validateCode(String username, String code, String type)  {
        // 前置校验，避免空指针并给出明确提示
        if (StrUtil.isBlank(username) || StrUtil.isBlank(type)) {
            throw new MyException("请求参数缺失");
        }
        if (StrUtil.isBlank(code)) {
            throw new MyException("验证码不能为空");
        }

        String key = CODE_PREFIX + type + ":" + username;
        String storedCode = (String) redisUtil.get(key);
        if (storedCode == null) {
            throw new MyException("验证码已过期");
        }
        
        if (!code.trim().equals(storedCode)) {
            throw new MyException("验证码错误");
        }
    }

}