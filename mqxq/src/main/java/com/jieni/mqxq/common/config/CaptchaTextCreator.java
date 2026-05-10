package com.jieni.mqxq.common.config;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

import java.util.Random;

/**
 * 验证码文本生成器
 * 继承DefaultTextCreator，生成数学运算表达式作为验证码
 * 支持加法、减法、乘法、除法四种运算类型
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public class CaptchaTextCreator extends DefaultTextCreator {
    
    /** 数字字符串数组，用于生成运算表达式 */
    private static final String[] CNUMBERS = "0,1,2,3,4,5,6,7,8,9,10".split(",");

    /**
     * 生成验证码文本
     * 随机生成数学运算表达式，包括操作数、运算符和结果
     * 
     * @return 包含数学表达式和答案的字符串，格式：表达式=?@答案
     */
    @Override
    public String getText() {
        Integer result = 0;
        Random random = new Random();
        int x = random.nextInt(10);
        int y = random.nextInt(10);
        StringBuilder suChinese = new StringBuilder();
        
        // 随机选择运算类型：0-乘法，1-除法或加法，2-减法，其他-加法
        int randomoperands = (int) Math.round(Math.random() * 2);
        if (randomoperands == 0) {
            // 乘法运算
            result = x * y;
            suChinese.append(CNUMBERS[x]);
            suChinese.append("*");
            suChinese.append(CNUMBERS[y]);
        } else if (randomoperands == 1) {
            // 除法运算（当除数不为0且能整除时），否则使用加法
            if (!(x == 0) && y % x == 0) {
                result = y / x;
                suChinese.append(CNUMBERS[y]);
                suChinese.append("/");
                suChinese.append(CNUMBERS[x]);
            } else {
                result = x + y;
                suChinese.append(CNUMBERS[x]);
                suChinese.append("+");
                suChinese.append(CNUMBERS[y]);
            }
        } else if (randomoperands == 2) {
            // 减法运算（确保结果为正数）
            if (x >= y) {
                result = x - y;
                suChinese.append(CNUMBERS[x]);
                suChinese.append("-");
                suChinese.append(CNUMBERS[y]);
            } else {
                result = y - x;
                suChinese.append(CNUMBERS[y]);
                suChinese.append("-");
                suChinese.append(CNUMBERS[x]);
            }
        } else {
            // 默认加法运算
            result = x + y;
            suChinese.append(CNUMBERS[x]);
            suChinese.append("+");
            suChinese.append(CNUMBERS[y]);
        }
        
        // 添加等号和结果标识符
        suChinese.append("=?@" + result);
        return suChinese.toString();
    }
}
