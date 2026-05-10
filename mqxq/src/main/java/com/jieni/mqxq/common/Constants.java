package com.jieni.mqxq.common;

/**
 * 系统常量类
 * 定义了系统中使用的各种常量，包括支付宝配置等
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public class Constants {

    /**
     * 支付宝网关地址（沙箱环境）
     */
    public static final String ALIPAY_GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    
    /**
     * 支付宝API数据格式
     */
    public static final String ALIPAY_FORMAT = "JSON";
    
    /**
     * 支付宝API字符编码
     */
    public static final String ALIPAY_CHARSET = "UTF-8";
    
    /**
     * 支付宝签名类型
     */
    public static final String ALIPAY_SIGN_TYPE = "RSA2";

}