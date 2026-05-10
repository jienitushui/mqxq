package com.jieni.mqxq.service.impl.order;

import cn.hutool.core.bean.BeanUtil;
import com.jieni.mqxq.dao.MyCourseDao;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.entity.MyCourse;
import com.jieni.mqxq.domain.entity.Orders;
import com.jieni.mqxq.domain.vo.order.OrderVO;
import com.jieni.mqxq.domain.vo.order.PurchaseStatusVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.order.CoursePurchaseService;
import com.jieni.mqxq.service.course.CourseService;
import com.jieni.mqxq.service.order.OrdersService;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 课程购买服务实现类
 * 
 * 提供完整的课程购买业务功能，包括购买流程、支付处理、订单管理等核心功能。
 * 实现购买前的权限校验、重复购买检查、订单状态管理等安全机制。
 * 支持订单取消、支付成功回调处理、课程加入等完整业务链路。
 * 
 * 主要功能：
 * - 课程购买流程与订单创建
 * - 购买状态检查与权限验证
 * - 订单管理（取消、支付回调）
 * - 课程学习权限管理
 * - 课程销量统计更新
 * 
 * 安全特性：
 * - 防止重复购买检查
 * - 订单所有权验证
 * - 事务一致性保证
 * - 异常情况处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CoursePurchaseServiceImpl implements CoursePurchaseService {

    @Resource
    private CourseService courseService;

    @Resource
    private OrdersService ordersService;

    @Resource
    private MyCourseDao myCourseDao;

    /**
     * 购买课程
     * 
     * 处理用户购买课程的完整流程，包括课程验证、重复购买检查、订单创建等步骤。
     * 自动验证课程可用性、用户购买权限、未支付订单状态，确保购买流程的安全性。
     * 
     * @param userId 用户ID，必须大于0
     * @param courseId 课程ID，必须大于0
     * @return OrderVO 创建成功的订单VO，包含订单号和订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO purchaseCourse(Integer userId, Integer courseId) {
        // 参数验证
        if (userId == null || userId <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID不能为空或小于等于0");
        }
        
        try {
            log.info("开始处理课程购买, userId: {}, courseId: {}", userId, courseId);

            // 1. 检查课程是否存在且已发布
            Course course = courseService.queryById(courseId);
            if (course == null) {
                throw new MyException("课程不存在");
            }

            if (course.getStatus() == null || course.getStatus() != 1) {
                throw new MyException("该课程暂未发布");
            }

            // 2. 检查用户是否已购买过该课程
            if (canStudyCourse(userId, courseId)) {
                throw new MyException("您已购买过该课程");
            }

            // 3. 检查是否有未支付的订单
            List<Orders> existingOrders = ordersService.queryByUserId(userId, (String) null);
            boolean hasUnpaidOrder = existingOrders.stream()
                    .anyMatch(order -> courseId.equals(order.getGoodsId()) && "NOT_PAY".equals(order.getStatus()));

            if (hasUnpaidOrder) {
                throw new MyException("您有该课程的未支付订单，请先完成支付或取消订单");
            }

            // 4. 创建订单
            Orders order = new Orders();
            order.setGoodsId(courseId);
            order.setGoodsName(course.getTitle());
            order.setGoodsImg(course.getCover());
            order.setGoodsPrice(BigDecimal.valueOf(course.getPrice() != null ? course.getPrice().doubleValue() : 0.0));
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            order.setStatus("NOT_PAY");

            int result = ordersService.insert(order);
            
            if (result > 0) {
                log.info("课程购买订单创建成功, orderNo: {}", order.getOrderNo());
                return convertToOrderVO(order);
            } else {
                throw new MyException("订单创建失败");
            }

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("购买课程失败, userId: {}, courseId: {}, 错误详情: {}", userId, courseId, e.getMessage(), e);
            
            // 根据异常类型提供更具体的错误信息
            String errorMessage = "购买课程失败";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Duplicate")) {
                    errorMessage = "订单创建失败：订单号重复，请重试";
                } else if (e.getMessage().contains("constraint")) {
                    errorMessage = "数据约束错误，请检查输入参数";
                } else if (e.getMessage().contains("timeout")) {
                    errorMessage = "操作超时，请稍后重试";
                } else {
                    errorMessage = "购买课程失败：" + e.getMessage();
                }
            }
            
            throw new MyException(errorMessage);
        }
    }

    /**
     * 检查课程购买状态
     * 
     * 查询用户对指定课程的购买状态，包括订单状态和支付情况。
     * 返回详细的购买状态信息，用于前端展示和权限控制。
     * 
     * @param userId 用户ID，必须大于0
     * @param courseId 课程ID，必须大于0
     * @return PurchaseStatusVO 购买状态VO，包含购买状态、订单信息和状态描述
     */
    @Override
    public PurchaseStatusVO checkPurchaseStatus(Integer userId, Integer courseId) {
        // 参数验证
        if (userId == null || userId <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        if (courseId == null || courseId <= 0) {
            throw new MyException("课程ID不能为空或小于等于0");
        }
        
        try {
            log.info("检查课程购买状态, userId: {}, courseId: {}", userId, courseId);

            // 检查用户是否已购买过该课程
            List<Orders> orders = ordersService.queryByUserId(userId, (String) null);

            // 查找该课程的订单
            Orders courseOrder = orders.stream()
                    .filter(order -> courseId.equals(order.getGoodsId()))
                    .findFirst()
                    .orElse(null);

            if (courseOrder == null) {
                return new PurchaseStatusVO(false, null, "未购买");
            }

            String status = courseOrder.getStatus();
            boolean isPurchased = "DONE".equals(status) || "COMMENT_DONE".equals(status);
            String statusDesc = getOrderStatusDesc(status);

            return new PurchaseStatusVO(isPurchased, convertToOrderVO(courseOrder), statusDesc);

        } catch (Exception e) {
            log.error("检查课程购买状态失败, userId: {}, courseId: {}", userId, courseId, e);
            throw new MyException("检查购买状态失败");
        }
    }

    /**
     * 取消订单
     * 
     * 用户取消指定的未支付订单，只能取消自己的且状态为未支付的订单。
     * 包含订单所有权验证和状态检查，确保操作的安全性。
     * 
     * @param userId 用户ID，必须大于0
     * @param orderNo 订单号，不能为空
     * @return boolean true表示取消成功，false表示取消失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Integer userId, String orderNo) {
        // 参数验证
        if (userId == null || userId <= 0) {
            throw new MyException("用户ID不能为空或小于等于0");
        }
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new MyException("订单号不能为空");
        }
        
        try {
            log.info("用户取消订单, userId: {}, orderNo: {}", userId, orderNo);

            // 查找订单
            Orders order = ordersService.selectByOrderNo(orderNo);
            if (order == null) {
                throw new MyException("订单不存在");
            }

            // 验证订单所有权
            if (!userId.equals(order.getUserId())) {
                throw new MyException("无权操作此订单");
            }

            // 只能取消未支付的订单
            if (!"NOT_PAY".equals(order.getStatus())) {
                throw new MyException("只能取消未支付的订单");
            }

            // 更新订单状态为已取消
            order.setStatus("CANCEL");
            ordersService.updateById(order);

            log.info("订单取消成功, orderNo: {}", orderNo);

            return true;

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消订单失败, userId: {}, orderNo: {}", userId, orderNo, e);
            throw new MyException("取消订单失败");
        }
    }

    /**
     * 处理支付成功回调
     * 
     * 处理第三方支付平台的支付成功通知，完成订单状态更新和课程开通。
     * 包括订单状态检查、课程关联、销量统计等完整流程。
     * 采用容错设计，即使部分操作失败也不影响核心扩程开通。
     * 
     * @param orderNo 订单号，不能为空
     * @return boolean true表示处理成功，false表示处理失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(String orderNo) {
        // 参数验证
        if (orderNo == null || orderNo.trim().isEmpty()) {
            log.warn("订单号为空，无法处理支付回调");
            return false;
        }
        
        try {
            log.info("处理支付成功回调, orderNo: {}", orderNo);

            // 查找订单
            Orders order = ordersService.selectByOrderNo(orderNo);
            if (order == null) {
                log.warn("支付回调处理失败，订单不存在: {}", orderNo);
                return false;
            }

            // 检查订单状态
            // 检查订单状态
            if (!"NOT_PAY".equals(order.getStatus())) {
                log.warn("订单状态不是待支付，无需处理: orderNo={}, status={}", orderNo, order.getStatus());
                return true;
            }

            // 更新订单状态为已完成
            order.setStatus("DONE");
            order.setPayTime(new Date().toString());
            ordersService.updateById(order);

            // 添加到我的课程
            addToMyCourse(order.getUserId(), order.getGoodsId(), order.getId());

            // 更新课程销量
            updateCourseSales(order.getGoodsId());

            log.info("支付成功处理完成, orderNo: {}", orderNo);

            return true;

        } catch (Exception e) {
            log.error("处理支付成功回调失败, orderNo: {}", orderNo, e);
            return false;
        }
    }

    /**
     * 检查用户是否可以学习课程
     * 
     * 综合判断用户是否有权限学习指定课程，包括以下条件：
     * 1. 用户已成功购买且支付完成的课程
     * 2. 课程为免费课程（价格为0）
     * 
     * @param userId 用户ID，必须大于0
     * @param courseId 课程ID，必须大于0
     * @return boolean true表示可以学习，false表示不可以学习
     */
    @Override
    public boolean canStudyCourse(Integer userId, Integer courseId) {
        // 参数验证
        if (userId == null || userId <= 0 || courseId == null || courseId <= 0) {
            log.warn("参数无效: userId={}, courseId={}", userId, courseId);
            return false;
        }
        
        try {
            // 检查是否有已完成的订单
            List<Orders> orders = ordersService.queryByUserId(userId);
            boolean hasPaidOrder = orders.stream()
                    .anyMatch(order -> courseId.equals(order.getGoodsId()) && 
                             ("DONE".equals(order.getStatus()) || "COMMENT_DONE".equals(order.getStatus())));

            if (hasPaidOrder) {
                return true;
            }

            // 检查课程是否免费
            Course course = courseService.queryById(courseId);
            if (course != null && course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("检查用户是否可以学习课程失败, userId: {}, courseId: {}", userId, courseId, e);
            return false;
        }
    }

    /**
     * 添加到我的课程
     * 
     * 在用户成功支付后，将课程添加到用户的学习列表中。
     * 检查重复添加，防止数据重复。即使失败也不影响主流程。
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param orderId 订单ID
     */
    private void addToMyCourse(Integer userId, Integer courseId, Integer orderId) {
        try {
            // 检查是否已存在
            MyCourse existing = myCourseDao.queryByUserIdAndCourseId(userId, courseId);
            if (existing != null) {
                log.info("用户课程关系已存在, userId: {}, courseId: {}", userId, courseId);
                return;
            }

            // 创建我的课程记录
            MyCourse myCourse = new MyCourse();
            myCourse.setUserId(userId);
            myCourse.setCourseId(courseId);
            myCourse.setOrderId(orderId);
            myCourse.setCreateTime(new Date());
            myCourse.setUpdateTime(new Date());

            myCourseDao.insert(myCourse);

            log.info("添加到我的课程成功, userId: {}, courseId: {}", userId, courseId);

        } catch (Exception e) {
            log.error("添加到我的课程失败, userId: {}, courseId: {}", userId, courseId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 更新课程销量
     * 
     * 在用户成功购买课程后，自动增加课程的购买次数统计。
     * 采用容错设计，统计失败不影响主业务流程。
     * 
     * @param courseId 课程ID
     */
    private void updateCourseSales(Integer courseId) {
        try {
            Course course = courseService.queryById(courseId);
            if (course != null) {
                course.setBuyCount(course.getBuyCount() == null ? 1 : course.getBuyCount() + 1);
                courseService.update(course);
                log.info("更新课程销量成功, courseId: {}, buyCount: {}", courseId, course.getBuyCount());
            }
        } catch (Exception e) {
            log.error("更新课程销量失败, courseId: {}", courseId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 生成订单号
     * 
     * 生成全局唯一的订单号，由以下部分组成：
     * - 系统前缀：MQXQ_ORDER_
     * - 当前时间戳（毫秒）
     * - 线程ID
     * - 8位随机UUID
     * 
     * @return String 全局唯一的订单号
     */
    private String generateOrderNo() {
        // 使用时间戳 + 随机数 + 线程ID 确保唯一性
        long timestamp = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();
        String uuid = IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
        return "MQXQ_" + "ORDER_" + timestamp + "_" + threadId + "_" + uuid;
    }

    /**
     * 获取订单状态描述
     * 
     * 将订单状态码转换为中文描述，方便前端展示和用户理解。
     * 支持所有定义的订单状态，未知状态返回默认描述。
     * 
     * @param status 订单状态码
     * @return String 订单状态的中文描述
     */
    private String getOrderStatusDesc(String status) {
        if (status == null) {
            return "未知状态";
        }
        
        switch (status) {
            case "NOT_PAY": return "待支付";
            case "DONE": return "已完成";
            case "CANCEL": return "已取消";
            case "REFUND_DONE": return "已退款";
            case "COMMENT_DONE": return "已评价";
            default: return "未知状态";
        }
    }

    /**
     * 将订单实体转换为订单VO
     * 
     * @param order 订单实体
     * @return OrderVO 订单VO
     */
    private OrderVO convertToOrderVO(Orders order) {
        if (order == null) {
            return null;
        }
        return BeanUtil.copyProperties(order, OrderVO.class);
    }
}