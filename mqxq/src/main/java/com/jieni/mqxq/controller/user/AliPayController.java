package com.jieni.mqxq.controller.user;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.common.Constants;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.common.config.AliPayConfig;
import com.jieni.mqxq.domain.entity.Orders;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.service.course.CourseService;
import com.jieni.mqxq.service.order.OrdersService;
import com.jieni.mqxq.service.order.CoursePurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 支付宝支付控制器（用户端）
 * 
 * 提供用户端支付宝支付的完整功能，包括发起支付、异步通知处理和退款等操作
 * 集成支付宝沙箱环境，支持订单状态自动更新、课程购买数量统计和用户课程关系建立
 * 包含完善的签名验证、异常处理和日志记录机制
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/alipay")
@Slf4j
@SaCheckRole("用户")
@Tag(name = "用户-支付宝支付", description = "用户端支付宝支付接口")
public class AliPayController {

    /**
     * 支付宝配置信息
     * 包含应用ID、私钥、公钥等配置参数
     */
    @Resource
    private AliPayConfig aliPayConfig;

    /**
     * 订单服务
     * 用于处理订单相关的业务逻辑
     */
    @Resource
    private OrdersService ordersService;

    @Resource
    private CourseService courseService;


    @Resource
    private CoursePurchaseService coursePurchaseService;

    /**
     * 发起支付宝支付
     * 根据订单号创建支付宝支付页面并重定向到支付宝收银台
     *
     * @param orderNo 订单编号，用于查找对应的订单信息
     * @param httpResponse HTTP响应对象，用于输出支付表单并重定向到支付宝页面
     * @throws IOException 当输出支付表单失败时抛出IO异常
     */
    @Operation(summary = "发起支付宝支付", description = "根据订单号创建支付宝支付页面并重定向到支付宝收银台")
    @GetMapping("/pay")
    public void pay(String orderNo, HttpServletResponse httpResponse) throws IOException, MyException {
        // 根据订单号查询订单信息
        Orders orders = ordersService.selectByOrderNo(orderNo);
        if (ObjectUtil.isNull(orders)) {
            throw new MyException("未找到订单");
        }
        
        // 1. 创建支付宝客户端，用于调用支付宝API
        AlipayClient alipayClient = new DefaultAlipayClient(Constants.ALIPAY_GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), Constants.ALIPAY_FORMAT, Constants.ALIPAY_CHARSET, aliPayConfig.getAlipayPublicKey(), Constants.ALIPAY_SIGN_TYPE);

        // 2. 创建支付请求对象并设置请求参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 设置异步通知地址，支付完成后支付宝会调用此接口
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        
        // 构建业务参数
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", orders.getOrderNo());  // 商户订单号（我们系统生成的订单编号）
        bizContent.set("total_amount", orders.getGoodsPrice()); // 订单总金额
        bizContent.set("subject", orders.getGoodsName());   // 订单标题（商品名称）
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");  // 产品码，固定值
        request.setBizContent(bizContent.toString());
        
        // 设置同步跳转地址，支付完成后用户浏览器会跳转到此页面
        request.setReturnUrl("http://localhost:5173/orders/OrderList");
//        request.setReturnUrl("http://159.75.11.181:5173/orders/OrderList");
        // 3. 执行支付请求，生成支付表单
        String form = "";
        try {
            // 调用支付宝SDK生成支付表单HTML
            form = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付失败", e);
            throw new MyException("支付失败: " + e.getMessage());
        }
        
        // 4. 将支付表单输出到浏览器，自动跳转到支付宝收银台
        httpResponse.setContentType("text/html;charset=" + Constants.ALIPAY_CHARSET);
        httpResponse.getWriter().write(form); // 输出完整的支付表单HTML
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();

    }

    /**
     * 支付宝异步通知回调接口
     * 支付宝在用户支付完成后会异步调用此接口，通知支付结果
     * 注意：此接口必须是POST方法，且需要验证支付宝的签名
     * 
     * @param request HTTP请求对象，包含支付宝回调的参数
     * @throws Exception 当处理回调失败时抛出异常
     */
    @Operation(summary = "支付宝异步通知回调", description = "接收支付宝支付完成后的异步通知，验证签名并更新订单状态")
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        log.info("=========支付宝异步回调开始========");
        
        // 获取所有请求参数用于调试
        Map<String, String[]> requestParams = request.getParameterMap();
        log.info("回调参数: {}", requestParams);
        
        String tradeStatus = request.getParameter("trade_status");
        log.info("交易状态: {}", tradeStatus);
        
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            log.info("支付成功，开始处理订单");
            
            // 1. 获取支付宝回调参数
            Map<String, String> params = new HashMap<>();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            // 2. 验证支付宝签名，确保回调请求来自支付宝
            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = false;
            
            try {
                checkSignature = AlipaySignature.rsa256CheckContent(content, sign, aliPayConfig.getAlipayPublicKey(), "UTF-8");
                log.info("签名验证结果: {}", checkSignature);
            } catch (Exception e) {
                log.error("签名验证异常", e);
                return "fail";
            }
            
            // 3. 处理支付宝回调结果 - 必须验证签名通过
            if (checkSignature) {
                // 验签通过，记录支付信息
                log.info("交易名称: {}", params.get("subject"));
                log.info("交易状态: {}", params.get("trade_status"));
                log.info("支付宝交易凭证号: {}", params.get("trade_no"));
                log.info("商户订单号: {}", params.get("out_trade_no"));
                log.info("交易金额: {}", params.get("total_amount"));
                log.info("买家在支付宝唯一id: {}", params.get("buyer_id"));
                log.info("买家付款时间: {}", params.get("gmt_payment"));
                log.info("买家付款金额: {}", params.get("buyer_pay_amount"));

                // 提取关键支付信息
                String orderNo = params.get("out_trade_no");  // 我们自己的订单编号
                String gmtPayment = params.get("gmt_payment");  // 支付的时间
                String alipayTradeNo = params.get("trade_no");  // 支付订单号
                
                log.info("开始更新订单状态, orderNo: {}", orderNo);
                
                // 4. 更新订单状态为已完成，并记录支付信息
                Orders orders = ordersService.selectByOrderNo(orderNo);
                if (ObjectUtil.isNull(orders)) {
                    log.error("未找到订单: {}", orderNo);
                    return "fail";
                }
                
                log.info("找到订单，当前状态: {}", orders.getStatus());
                
                orders.setStatus("DONE");  // 直接使用字符串，避免枚举问题
                orders.setPayNo(alipayTradeNo);
                orders.setPayTime(gmtPayment);
                
                int updateResult = ordersService.updateById(orders);
                
                // 订单支付成功后，增加课程购买数量
                if (updateResult > 0) {
                    try {
                        Course course = courseService.queryById(orders.getGoodsId());
                        if (course != null) {
                            course.setBuyCount(course.getBuyCount() + 1);
                            courseService.update(course);
                            log.info("课程购买数量已自增，课程ID: {}, 新购买数量: {}", orders.getGoodsId(), course.getBuyCount());
                        }
                    } catch (Exception e) {
                        log.error("更新课程购买数量失败，课程ID: {}", orders.getGoodsId(), e);
                    }
                }
                log.info("订单更新结果: {}, 订单号: {}", updateResult, orderNo);
                
                // 处理支付成功后的业务逻辑（添加到我的课程等）
                coursePurchaseService.handlePaymentSuccess(orderNo);
                log.info("支付成功后业务处理完成");
                
                log.info("=========支付宝异步回调处理成功========");
                return "success";
            } else {
                log.error("支付宝签名验证失败，拒绝处理回调");
                return "fail";
            }
        } else {
            log.info("交易状态不是TRADE_SUCCESS，状态: {}", tradeStatus);
            return "success";  // 其他状态也返回success，避免支付宝重复回调
        }
    }

    /**
     * 支付宝退款接口
     * 根据订单号发起退款请求，将已支付的金额退回给用户
     * 
     * @param orderNo 订单编号，用于查找需要退款的订单
     * @return 退款结果，成功返回success
     */
    @Operation(summary = "支付宝退款", description = "根据订单号发起退款请求，将已支付的金额退回给用户")
    @GetMapping("/refund")
    public Result refund(String orderNo) {
        // 查询需要退款的订单
        Orders orders = ordersService.selectByOrderNo(orderNo);
        if (ObjectUtil.isNull(orders)) {
            throw new MyException("未找到订单");
        }
        
        // 1. 创建Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = new DefaultAlipayClient(Constants.ALIPAY_GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), Constants.ALIPAY_FORMAT, Constants.ALIPAY_CHARSET, aliPayConfig.getAlipayPublicKey(), Constants.ALIPAY_SIGN_TYPE);

        // 2. 创建 Request并设置Request参数
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", orders.getOrderNo());  // 我们自己生成的订单编号
        bizContent.set("refund_amount", orders.getGoodsPrice()); // 订单的总金额
        bizContent.set("trade_no", orders.getPayNo()); // 支付宝支付订单号
        bizContent.set("out_request_no", IdUtil.fastSimpleUUID());   // 随机数
        request.setBizContent(bizContent.toString());
        
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("订单号【{}】退款成功", orders.getOrderNo());
            }
            Orders dbOrder = ordersService.selectByOrderNo(orderNo);
            dbOrder.setStatus("REFUND_DONE");
            ordersService.updateById(dbOrder);
        } catch (AlipayApiException e) {
            log.error("退款失败", e);
            throw new MyException("退款失败: " + e.getMessage());
        }

        return Result.success();
    }

}
