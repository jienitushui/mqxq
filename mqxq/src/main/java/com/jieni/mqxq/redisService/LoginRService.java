package com.jieni.mqxq.redisService;

import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.util.RedisUtil;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import static com.jieni.mqxq.common.constants.LoginConstants.CODE;
import static com.jieni.mqxq.common.constants.LoginConstants.CODE_TIME;

/**
 * 登录相关Redis服务类
 * 
 * 提供登录认证相关的Redis缓存服务，包括验证码管理、Token管理等
 * 支持验证码的生成、验证、失效管理以及Token的存储和验证
 * 实现对Redis操作的封装，提供统一的缓存管理能力
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Service
public class LoginRService {

    //时间
    private static final int DAY_TIME = 1000 * 60 * 60 * 24;
    @Resource
    RedisUtil redisUtil;
    /**
     * @Description 注册时发送验证码(如果redis中有验证码则报错)
     * @Param 1：【email】邮箱 2；【验证码】
     * @Return
     **/
    public void setCode(String email,String code)  {
        if(redisUtil.hasKey(CODE+email)){
            throw new MyException("不要重复发验证码");
        }
        redisUtil.add(CODE+email,code,CODE_TIME);
    }

    /**
     * @Description 验证注册信息和redis中的验证码是否相同
     * @Param
     **/
    public boolean checkCode(String email,String code){
        String code1 = redisUtil.getString(CODE + email);
        return code.equals(code1);
    }

    /**
     * @Description 验证完成后删除邮箱验证码
     * @Param 【email】邮箱
     **/
    public void deleteCode(String email){
        redisUtil.delete("CODE"+email);
    }


    /**
     * @Description 登录后记录token，并将token保留一天
     * @Param
     * @Return
     **/
    public void setToken(String id,String token){
        redisUtil.add(id,token,DAY_TIME);
    }

    /**
     * @Description 验证 redis是否有这个token
     * @Param
     * @Return
     **/
    public boolean checkToken(String id,String token){
        String s = redisUtil.getString(id);
        return s.equals(token);
    }
}
