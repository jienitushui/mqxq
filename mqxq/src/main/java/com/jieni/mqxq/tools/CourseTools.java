package com.jieni.mqxq.tools;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.jieni.mqxq.common.config.ToolResultHolder;
import com.jieni.mqxq.dao.CourseDao;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.vo.chat.CourseInfo;
import com.jieni.mqxq.domain.vo.chat.OrderInfo;
import com.jieni.mqxq.domain.vo.order.OrderVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.order.CoursePurchaseService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseTools {

    @Resource
    private CourseDao courseDao;

    @Resource
    private CoursePurchaseService coursePurchaseService;

    private static final String FIELD_NAME_FORMAT = "{}_{}";  // 提取格式字符串常量

    /**
     * 将Course实体转换为CourseInfo VO
     */
    private CourseInfo convertToCourseInfo(Course course) {
        if (course == null) {
            return null;
        }

        return CourseInfo.builder()
                .id(course.getId())
                .teacherId(course.getTeacherId())
                .subjectId(course.getSubjectId())
                .name(course.getTitle())  // title -> name
                .detail(course.getDescription())  // description -> detail
                .price(course.getPrice() != null ? course.getPrice().doubleValue() : null)  // BigDecimal -> Double
                .lessonNum(course.getLessonNum())
                .durationSum(course.getDurationSum())
                .cover(course.getCover())
                .buyCount(course.getBuyCount())
                .viewCount(course.getViewCount())
                .status(course.getStatus())
                .publishTime(course.getPublishTime() != null ? course.getPublishTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)  // Date -> LocalDateTime
                .createTime(course.getCreateTime() != null ? course.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)  // Date -> LocalDateTime
                .createUser(course.getCreateUser())
                .updateTime(course.getUpdateTime() != null ? course.getUpdateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)  // Date -> LocalDateTime
                .updateUser(course.getUpdateUser())
                .build();
    }
    /**
     * 根据课程id查询课程信息
     *
     * @param courseId 课程id
     * @return 课程信息
     */
    @Tool(description = "根据课程id查询课程详细信息")
    public CourseInfo queryCourseById(@ToolParam(description = "课程id") Integer courseId, ToolContext toolContext) {
        return Optional.ofNullable(courseId)
                .map(id -> courseDao.queryById(id))
                .map(course  -> {
                    // 将Course实体转换为CourseInfo VO
                    CourseInfo courseInfo = convertToCourseInfo(course);

                    // 存储数据的字段名
                    String field = StrUtil.format(FIELD_NAME_FORMAT,
                            StrUtil.lowerFirst(CourseInfo.class.getSimpleName()),
                            courseInfo.getId());
                    // 存储的key
                    var requestId = Convert.toStr(toolContext.getContext().get("requestId"));
                    ToolResultHolder.put(requestId, field, courseInfo);
                    return courseInfo;
                })
                .orElse(null);
    }

    /**
     * 预下单 - 创建课程购买订单
     * 根据课程ID创建订单，只支持单个课程
     *
     * @param courseId 课程ID
     * @param toolContext 工具上下文，包含requestId和conversationId
     * @return 订单信息
     */
    @Tool(description = "预下单功能，根据课程ID创建购买订单。只能购买单个课程。如果创建成功，返回的OrderInfo对象status为NOT_PAY（待支付）。如果创建失败（如用户已购买过该课程或存在未支付的订单），返回的OrderInfo对象status为ERROR，errorMessage字段包含具体的错误原因，请根据errorMessage告知用户具体情况并引导用户完成支付或取消现有订单。")
    public OrderInfo prePlaceOrder(
            @ToolParam(description = "课程ID") Integer courseId,
            ToolContext toolContext) {
        
        if (courseId == null || courseId <= 0) {
            log.warn("prePlaceOrder: 课程ID无效: {}", courseId);
            throw new MyException("课程ID不能为空且必须大于0");
        }

        // 从 conversationId 中解析用户ID
        String conversationId = Convert.toStr(toolContext.getContext().get("conversationId"));
        Integer userId = parseUserIdFromConversationId(conversationId);
        
        if (userId == null) {
            log.error("prePlaceOrder: 无法从conversationId中解析用户ID, conversationId: {}", conversationId);
            throw new MyException("无法获取用户信息，请重新登录");
        }

        String requestId = Convert.toStr(toolContext.getContext().get("requestId"));

        try {
            log.info("prePlaceOrder: 开始创建订单, userId: {}, courseId: {}", userId, courseId);

            // 调用购买服务创建订单
            OrderVO orderVO = coursePurchaseService.purchaseCourse(userId, courseId);
            
            // 转换为OrderInfo
            OrderInfo orderInfo = OrderInfo.builder()
                    .id(orderVO.getId())
                    .orderNo(orderVO.getOrderNo())
                    .courseId(orderVO.getGoodsId())
                    .courseName(orderVO.getGoodsName())
                    .price(orderVO.getGoodsPrice())
                    .status(orderVO.getStatus())
                    .createTime(orderVO.getCreateTime())
                    .build();

            // 存储订单信息到ToolResultHolder
            String field = StrUtil.format(FIELD_NAME_FORMAT,
                    StrUtil.lowerFirst(OrderInfo.class.getSimpleName()),
                    orderInfo.getId());
            ToolResultHolder.put(requestId, field, orderInfo);

            log.info("prePlaceOrder: 订单创建成功, orderNo: {}, courseId: {}", orderInfo.getOrderNo(), courseId);
            return orderInfo;

        } catch (MyException e) {
            // 业务异常（如已购买、未支付订单等），返回包含错误信息的OrderInfo对象
            // 不抛出异常，而是返回错误信息，让AI能够在回复中告知用户
            String errorMessage = e.getMessage();
            log.warn("prePlaceOrder: 创建订单失败: {}", errorMessage);
            
            // 创建一个包含错误信息的OrderInfo对象
            OrderInfo errorOrderInfo = OrderInfo.builder()
                    .courseId(courseId)
                    .status("ERROR")
                    .errorMessage(errorMessage)
                    .build();
            
            // 将错误信息存储到ToolResultHolder，让AI能够获取到
            String field = StrUtil.format(FIELD_NAME_FORMAT,
                    StrUtil.lowerFirst(OrderInfo.class.getSimpleName()) + "_error",
                    courseId);
            ToolResultHolder.put(requestId, field, errorOrderInfo);
            
            // 返回错误信息对象，AI可以根据status="ERROR"和errorMessage判断并告知用户
            return errorOrderInfo;
        } catch (Exception e) {
            log.error("prePlaceOrder: 创建订单异常, userId: {}, courseId: {}", userId, courseId, e);
            
            // 系统异常也返回错误信息对象
            OrderInfo errorOrderInfo = OrderInfo.builder()
                    .courseId(courseId)
                    .status("ERROR")
                    .errorMessage("创建订单时发生系统错误：" + e.getMessage())
                    .build();
            
            String field = StrUtil.format(FIELD_NAME_FORMAT,
                    StrUtil.lowerFirst(OrderInfo.class.getSimpleName()) + "_error",
                    courseId);
            ToolResultHolder.put(requestId, field, errorOrderInfo);
            
            return errorOrderInfo;
        }
    }

    /**
     * 从conversationId中解析用户ID
     * conversationId格式: {userId}_{sessionId}
     *
     * @param conversationId 对话ID
     * @return 用户ID
     */
    private Integer parseUserIdFromConversationId(String conversationId) {
        if (StrUtil.isBlank(conversationId)) {
            return null;
        }
        
        int idx = conversationId.indexOf('_');
        if (idx > 0) {
            try {
                String userIdStr = conversationId.substring(0, idx);
                return Integer.valueOf(userIdStr);
            } catch (NumberFormatException e) {
                log.error("解析用户ID失败, conversationId: {}", conversationId, e);
                return null;
            }
        }
        
        return null;
    }
}
