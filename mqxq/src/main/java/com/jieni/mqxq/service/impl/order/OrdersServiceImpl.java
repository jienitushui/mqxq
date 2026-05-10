package com.jieni.mqxq.service.impl.order;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CourseDao;
import com.jieni.mqxq.dao.OrdersDao;
import com.jieni.mqxq.domain.dto.course.MyCourseCreateDTO;
import com.jieni.mqxq.domain.dto.order.OrderPaymentDTO;
import com.jieni.mqxq.domain.dto.order.OrderQueryDTO;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.entity.Orders;
import com.jieni.mqxq.domain.vo.order.OrderStatisticsVO;
import com.jieni.mqxq.domain.vo.order.OrderVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.course.MyCourseService;
import com.jieni.mqxq.service.order.OrdersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 
 * 提供订单的完整业务逻辑实现，包括订单的创建、支付、取消、退款等生命周期管理。
 * 支持用户端、管理员端、教师端不同角色的订单管理需求。集成课程购买、统计分析等功能。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    @Resource
    private OrdersDao ordersDao;

    @Resource
    private CourseDao courseDao;

    @Resource
    private MyCourseService myCourseService;

    // ========== 基础CRUD方法 ==========

    /**
     * 根据ID查询订单详情
     * 
     * 根据订单ID获取订单的详细信息，用于订单展示和状态查询功能
     * 支持用户端、管理员端、教师端的订单查看需求
     * 
     * @param id 订单主键ID，必须大于0
     * @return Orders 订单实体对象，如果不存在返回null
     */
    @Override
    public Orders queryById(Integer id) {
        validateOrderId(id);
        return ordersDao.queryById(id);
    }

    /**
     * 根据订单号查询订单信息
     * 
     * 根据订单号获取订单详细信息，主要用于支付回调和订单查询功能
     * 订单号是系统生成的唯一标识，支持支付平台的回调验证
     * 
     * @param orderNo 订单号，不能为空且必须是有效的订单编号格式
     * @return Orders 订单实体对象，如果不存在返回null
     */
    @Override
    public Orders queryByOrderNo(String orderNo) {
        validateOrderNo(orderNo);
        return ordersDao.selectByOrderNo(orderNo);
    }

    /**
     * 创建新订单
     * 
     * 创建新的订单记录，自动设置创建时间并持久化到数据库
     * 用于课程购买、商品下单等业务场景的订单生成功能
     * 
     * @param orders 订单实体对象，必须包含用户ID、商品信息、价格等必要数据
     * @return Orders 创建成功的订单对象，包含生成的订单ID和时间戳
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrder(Orders orders) {
        validateOrderForCreate(orders);
        
        // 设置创建时间
        orders.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        // 插入订单
        ordersDao.insert(orders);
        log.info("创建订单成功，订单号: {}", orders.getOrderNo());
        return orders;
    }

    /**
     * 更新订单信息
     * 
     * 更新订单的基本信息，支持订单状态变更、支付信息更新等操作
     * 自动刷新数据并返回最新的订单对象
     * 
     * @param orders 订单实体对象，必须包含有效的订单ID和待更新字段
     * @return Orders 更新后的订单对象，包含最新的数据库信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders updateOrder(Orders orders) {
        validateOrderForUpdate(orders);
        ordersDao.update(orders);
        return queryById(orders.getId());
    }

    /**
     * 删除订单
     * 
     * 根据订单ID删除订单记录，主要用于管理员清理无效订单
     * 注意：已支付的订单不建议直接删除，应使用取消功能
     * 
     * @param id 订单主键ID，必须大于0
     * @return boolean 删除成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Integer id) {
        validateOrderId(id);
        return ordersDao.deleteById(id) > 0;
    }

    /**
     * 插入订单数据
     * 
     * 直接插入订单数据到数据库，如果创建时间为空则自动设置当前时间
     * 返回影响的行数，用于批量插入或低级别的数据操作
     * 
     * @param orders 订单实体对象，必须包含必要的订单信息
     * @return int 插入成功的行数，通常为1
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(Orders orders) {
        if (orders == null) {
            throw new MyException("订单对象不能为空");
        }
        
        // 设置创建时间
        if (orders.getCreateTime() == null) {
            orders.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        return ordersDao.insert(orders);
    }

    /**
     * 根据订单号查询订单（别名方法）
     * 
     * 与queryByOrderNo方法功能相同，提供不同的方法名以保持接口兼容性
     * 主要用于支付回调和订单状态查询功能
     * 
     * @param orderNo 订单号，不能为空
     * @return Orders 订单实体对象，如果不存在返回null
     */
    @Override
    public Orders selectByOrderNo(String orderNo) {
        validateOrderNo(orderNo);
        return ordersDao.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID更新订单
     * 
     * 根据订单ID更新订单信息，返回受影响的行数
     * 用于低级别的订单更新操作，不包含业务逻辑验证
     * 
     * @param orders 订单实体对象，必须包含有效的订单ID
     * @return int 更新成功的行数，通常为1
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(Orders orders) {
        validateOrderForUpdate(orders);
        return ordersDao.updateById(orders);
    }

    /**
     * 根据用户ID查询所有订单
     * 
     * 查询指定用户的所有订单记录，不限制订单状态
     * 用于用户查看个人订单历史和管理员查看用户订单
     * 
     * @param userId 用户ID，必须大于0
     * @return List<Orders> 用户的订单列表，如果无订单返回空列表
     */
    @Override
    public List<Orders> queryByUserId(Integer userId) {
        validateUserId(userId);
        return ordersDao.queryByUserId(userId, null);
    }

    /**
     * 根据用户ID和状态查询订单
     * 
     * 查询指定用户指定状态的订单记录，支持按状态筛选
     * 用于用户查看特定状态的订单，如待支付、已完成等
     * 
     * @param userId 用户ID，必须大于0
     * @param status 订单状态，如"NOT_PAY"、"DONE"、"CANCEL"等，可以为空
     * @return List<Orders> 符合条件的订单列表，如果无匹配订单返回空列表
     */
    @Override
    public List<Orders> queryByUserId(Integer userId, String status) {
        validateUserId(userId);
        return ordersDao.queryByUserId(userId, status);
    }

    // ========== 用户端方法 ==========
    // 已移至新增方法实现部分

    // ========== 管理员端方法 ==========
    // 已移至新增方法实现部分

    // ========== 教师端方法 ==========


    // ========== 新增方法实现 ==========

    @Override
    public PageInfo<OrderVO> getUserOrderPage(OrderQueryDTO queryDTO) {
        validateQueryDTO(queryDTO);
        validateUserId(queryDTO.getUserId());
        
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Orders> orders = ordersDao.queryByUserId(queryDTO.getUserId(), queryDTO.getStatus());
        List<OrderVO> orderVOs = convertToOrderVOList(orders);
        
        return new PageInfo<>(orderVOs);
    }

    @Override
    public OrderVO getUserOrderByOrderNo(Integer userId, String orderNo) {
        validateUserId(userId);
        validateOrderNo(orderNo);
        
        Orders order = ordersDao.selectByOrderNo(orderNo);
        if (order == null) {
            throw new MyException("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new MyException("无权限查看此订单");
        }
        
        return convertToOrderVO(order);
    }

    @Override
    public OrderVO getUserOrderById(Integer userId, Integer orderId) {
        validateUserId(userId);
        validateOrderId(orderId);
        
        Orders order = ordersDao.queryById(orderId);
        if (order == null) {
            throw new MyException("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new MyException("无权限查看此订单");
        }
        
        return convertToOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processOrderPayment(OrderPaymentDTO paymentDTO) {
        if (paymentDTO == null) {
            throw new MyException("支付信息不能为空");
        }
        
        return payOrder(paymentDTO.getOrderNo(), paymentDTO.getPayNo(), paymentDTO.getPaymentTime());
    }

    @Override
    public PageInfo<OrderVO> getAdminOrderPage(OrderQueryDTO queryDTO) {
        validateQueryDTO(queryDTO);
        
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        Orders query = new Orders();
        query.setUserId(queryDTO.getUserId());
        query.setGoodsId(queryDTO.getGoodsId());
        query.setStatus(queryDTO.getStatus());
        query.setOrderNo(queryDTO.getOrderNo());
        
        List<Orders> orders = ordersDao.queryAllOrders(query);
        List<OrderVO> orderVOs = convertToOrderVOList(orders);
        
        return new PageInfo<>(orderVOs);
    }

    @Override
    public OrderVO getAdminOrderById(Integer orderId) {
        validateOrderId(orderId);
        
        Orders order = ordersDao.queryById(orderId);
        if (order == null) {
            throw new MyException("订单不存在");
        }
        
        return convertToOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrderById(Integer orderId) {
        return deleteOrder(orderId);
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        OrderStatisticsVO statistics = new OrderStatisticsVO();
        
        // 订单数量统计
        statistics.setTotalOrders(ordersDao.countAllOrders(null));
        statistics.setPendingOrders(ordersDao.countAllOrders("NOT_PAY"));
        statistics.setCompletedOrders(ordersDao.countAllOrders("DONE"));
        statistics.setCancelledOrders(ordersDao.countAllOrders("CANCEL"));
        
        // 总收入统计
        BigDecimal totalRevenue = ordersDao.getTotalRevenue();
        statistics.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // 最近订单
        List<Orders> recentOrders = ordersDao.getRecentOrders(10);
        statistics.setRecentOrders(convertToOrderVOList(recentOrders));
        
        return statistics;
    }

    @Override
    public PageInfo<OrderVO> getTeacherOrderPage(Integer teacherId, OrderQueryDTO queryDTO) {
        validateUserId(teacherId);
        validateQueryDTO(queryDTO);
        
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Orders> orders = ordersDao.queryByTeacherId(teacherId, queryDTO.getGoodsId(), queryDTO.getStatus());
        List<OrderVO> orderVOs = convertToOrderVOList(orders);
        return new PageInfo<>(orderVOs);
    }

    @Override
    public OrderVO getTeacherOrderById(Integer teacherId, Integer orderId) {
        validateUserId(teacherId);
        validateOrderId(orderId);
        
        Orders order = ordersDao.queryById(orderId);
            if (order == null) {
                throw new MyException("订单不存在");
            }

        // TODO: 验证订单是否属于该教师的课程
        
        return convertToOrderVO(order);
    }

    @Override
    public Map<String, Object> getTeacherRevenueStatistics(Integer teacherId) {
        validateUserId(teacherId);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 教师总收入
        BigDecimal totalRevenue = ordersDao.getRevenueByTeacherId(teacherId);
        statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // 教师订单数
        int totalOrders = ordersDao.countByTeacherId(teacherId, null);
        statistics.put("totalOrders", totalOrders);
        
        // 已完成订单数
        int completedOrders = ordersDao.countByTeacherId(teacherId, "DONE");
        statistics.put("completedOrders", completedOrders);
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getTeacherTopCourses(Integer teacherId, Integer limit) {
        validateUserId(teacherId);
        
        if (limit == null || limit <= 0 || limit > 50) {
            throw new MyException("限制数量必须在1-50之间");
        }
        
        return ordersDao.getTopCoursesByTeacherId(teacherId, limit);
    }

    // ========== 私有工具方法 ==========

    /**
     * 订单支付处理（内部方法）
     * 
     * 处理订单支付成功回调，更新订单状态并自动添加课程到用户的课程列表
     * 支持支付宝、微信等三方支付平台的回调处理，保证订单与课程的一致性
     * 
     * 注意：使用protected而非private，以确保@Transactional注解能够生效（Spring AOP代理要求）
     * 
     * @param orderNo 订单号
     * @param payNo 支付流水号
     * @param paymentTime 支付时间
     * @return boolean 支付处理成功返回true，失败返回false
     */
    @Transactional(rollbackFor = Exception.class)
    protected boolean payOrder(String orderNo, String payNo, String paymentTime) {
        validateOrderNo(orderNo);
        
        if (payNo == null || payNo.trim().isEmpty()) {
            throw new MyException("支付流水号不能为空");
        }
        if (paymentTime == null || paymentTime.trim().isEmpty()) {
            throw new MyException("支付时间不能为空");
        }
        
        try {
            Orders order = queryByOrderNo(orderNo);
            if (order == null) {
                throw new MyException("订单不存在");
            }

            if (!"NOT_PAY".equals(order.getStatus())) {
                throw new MyException("订单状态不正确");
            }

            // 更新订单状态
            order.setStatus("DONE");
            order.setPayNo(payNo);
            order.setPayTime(paymentTime);
            updateOrder(order);

            // 添加到我的课程
            myCourseService.joinCourse(order.getUserId(), new MyCourseCreateDTO(order.getGoodsId(), order.getId()));

            log.info("订单支付成功: orderNo={}, payNo={}", orderNo, payNo);
            return true;
        } catch (Exception e) {
            log.error("订单支付失败: orderNo={}", orderNo, e);
            return false;
        }
    }

    /**
     * 验证订单ID
     * 
     * @param orderId 订单ID
     */
    private void validateOrderId(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            throw new MyException("订单ID不能为空或小于等于0");
        }
    }

    /**
     * 验证订单号
     * 
     * @param orderNo 订单号
     */
    private void validateOrderNo(String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new MyException("订单号不能为空");
        }
    }

    /**
     * 验证用户ID
     * 
     * @param userId 用户ID
     */
    private void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
    }

    /**
     * 验证订单对象（创建时）
     * 
     * @param orders 订单对象
     */
    private void validateOrderForCreate(Orders orders) {
        if (orders == null) {
            throw new MyException("订单对象不能为空");
        }
        if (orders.getUserId() == null || orders.getUserId() <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        if (orders.getOrderNo() == null || orders.getOrderNo().trim().isEmpty()) {
            throw new MyException("订单号不能为空");
        }
    }

    /**
     * 验证订单对象（更新时）
     * 
     * @param orders 订单对象
     */
    private void validateOrderForUpdate(Orders orders) {
        if (orders == null) {
            throw new MyException("订单对象不能为空");
        }
        if (orders.getId() == null || orders.getId() <= 0) {
            throw new MyException("订单ID不能为空或小于等于0");
        }
    }

    /**
     * 验证查询DTO
     * 
     * @param queryDTO 查询DTO
     */
    private void validateQueryDTO(OrderQueryDTO queryDTO) {
        if (queryDTO == null) {
            throw new MyException("查询条件不能为空");
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() <= 0) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() <= 0 || queryDTO.getPageSize() > 100) {
            queryDTO.setPageSize(10);
        }
    }

    /**
     * 将订单实体转换为VO
     * 
     * @param order 订单实体
     * @return OrderVO
     */
    private OrderVO convertToOrderVO(Orders order) {
        if (order == null) {
            return null;
        }
        return BeanUtil.copyProperties(order, OrderVO.class);
    }

    /**
     * 将订单实体列表转换为VO列表
     * 
     * @param orders 订单实体列表
     * @return OrderVO列表
     */
    private List<OrderVO> convertToOrderVOList(List<Orders> orders) {
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        return orders.stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());
    }
}