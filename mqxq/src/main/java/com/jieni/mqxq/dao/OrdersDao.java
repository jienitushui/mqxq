package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Orders;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单数据访问层接口
 * 
 * 提供订单相关的数据库操作接口，包括订单的增删改查、统计分析、收入计算等功能。
 * 支持用户端、管理员端、教师端不同角色的数据查询需求。与订单服务层配合实现复杂的业务逻辑。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface OrdersDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Orders queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param orders 查询条件
     * @return 对象列表
     */
    List<Orders> queryAllByLimit(Orders orders);

    /**
     * 统计总行数
     *
     * @param orders 查询条件
     * @return 总行数
     */
    long count(Orders orders);

    /**
     * 新增数据
     *
     * @param orders 实例对象
     * @return 影响行数
     */
    int insert(Orders orders);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Orders> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Orders> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Orders> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Orders> entities);

    /**
     * 修改数据
     *
     * @param orders 实例对象
     * @return 影响行数
     */
    int update(Orders orders);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单对象
     */
    Orders selectByOrderNo(String orderNo);

    /**
     * 根据ID更新订单
     *
     * @param orders 订单对象
     * @return 影响行数
     */
    int updateById(Orders orders);

    // ========== 新增方法 ==========

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态（可为null）
     * @return 订单列表
     */
    List<Orders> queryByUserId(@Param("userId") Integer userId, @Param("status") String status);

    /**
     * 统计用户订单数量
     *
     * @param userId 用户ID
     * @param status 订单状态（可为null）
     * @return 订单数量
     */
    int countByUserId(@Param("userId") Integer userId, @Param("status") String status);

    /**
     * 查询所有订单
     *
     * @param orders 查询条件
     * @return 订单列表
     */
    List<Orders> queryAllOrders(Orders orders);

    /**
     * 统计所有订单数量
     *
     * @param status 订单状态（可为null）
     * @return 订单数量
     */
    int countAllOrders(@Param("status") String status);

    /**
     * 获取总收入
     *
     * @return 总收入
     */
    BigDecimal getTotalRevenue();

    /**
     * 获取最近订单
     *
     * @param limit 数量限制
     * @return 订单列表
     */
    List<Orders> getRecentOrders(@Param("limit") Integer limit);

    /**
     * 根据教师ID查询订单列表
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID（可为null）
     * @param status 订单状态（可为null）
     * @return 订单列表
     */
    List<Orders> queryByTeacherId(@Param("teacherId") Integer teacherId, 
                                  @Param("courseId") Integer courseId, 
                                  @Param("status") String status);

    /**
     * 根据课程ID查询订单列表
     *
     * @param courseId 课程ID
     * @param status 订单状态（可为null）
     * @return 订单列表
     */
    List<Orders> queryByCourseId(@Param("courseId") Integer courseId, @Param("status") String status);

    /**
     * 验证课程所有权
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 是否拥有
     */
    boolean verifyCourseOwnership(@Param("courseId") Integer courseId, @Param("teacherId") Integer teacherId);

    /**
     * 统计教师订单数量
     *
     * @param teacherId 教师ID
     * @param status 订单状态（可为null）
     * @return 订单数量
     */
    int countByTeacherId(@Param("teacherId") Integer teacherId, @Param("status") String status);

    /**
     * 获取教师收入
     *
     * @param teacherId 教师ID
     * @return 收入金额
     */
    BigDecimal getRevenueByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 获取教师热销课程
     *
     * @param teacherId 教师ID
     * @param limit 数量限制
     * @return 热销课程列表
     */
    List<Map<String, Object>> getTopCoursesByTeacherId(@Param("teacherId") Integer teacherId, @Param("limit") Integer limit);

    /**
     * 根据用户ID和课程ID查询已完成的订单
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 订单对象
     */
    Orders selectByUserIdAndCourseIdDone(@Param("userId") Integer userId, @Param("courseId") Integer courseId);
    
    /**
     * 获取教师收入趋势
     *
     * @param teacherId 教师ID
     * @param days 天数
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getTeacherRevenueTrend(@Param("teacherId") Integer teacherId, @Param("days") Integer days);
}

