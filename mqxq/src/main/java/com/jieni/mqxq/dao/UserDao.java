package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 用户数据访问层接口
 * 提供用户表的CRUD操作和复杂查询功能
 * 
 * 支持的操作类型：
 * - 基本的增删改查操作
 * - 根据用户名、邮箱、用户类型等条件查询
 * - 批量操作和统计查询
 * - 关键词搜索功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface UserDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    User queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param user 查询条件
     * @return 对象列表
     */
    List<User> queryAllByLimit(User user);

    /**
     * 统计总行数
     *
     * @param user 查询条件
     * @return 总行数
     */
    long count(User user);

    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<User> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<User> entities);

    /**
     * 修改数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    int update(User user);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 批量删除数据
     *
     * @param ids 主键列表
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") List<Integer> ids);

    /**
     * 根据关键词查询用户
     *
     * @param keyword 搜索关键词(用户名/邮箱)
     * @return 用户列表
     */
    List<User> queryByKeyword(@Param("keyword") String keyword);

    /**
     * 根据关键词和用户类型查询用户
     *
     * @param keyword 搜索关键词(用户名/邮箱)
     * @param userType 用户类型
     * @return 用户列表
     */
    List<User> queryByKeywordAndType(@Param("keyword") String keyword, @Param("userType") String userType);

    /**
     * 根据用户类型查询用户
     *
     * @param userType 用户类型
     * @return 用户列表
     */
    List<User> queryByUserType(@Param("userType") String userType);

    /**
     * 获取用户类型统计
     *
     * @return 用户类型统计信息
     */
    List<Map<String, Object>> getUserTypeStatistics();

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(@Param("username") String username);


    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User selectById(@Param("id") Integer id);

    /**
     * 根据ID更新用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int updateById(User user);

}

