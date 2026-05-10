package com.jieni.mqxq.util;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 封装Redis常用操作，提供统一的缓存管理接口
 * 
 * 主要功能：
 * - 字符串缓存的增删改查操作
 * - 支持设置过期时间
 * - 异常处理和日志记录
 * - 缓存存在性判断和过期时间查询
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Component
public class RedisUtil {

    /** Redis模板，用于执行Redis操作 */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     * 将键值对存入Redis，并设置过期时间
     * 
     * @param key   缓存键，不能为null
     * @param value 缓存值，可以是任意可序列化对象
     * @param time  过期时间(秒)，小于等于0表示永不过期
     */
    public void add(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.error("Redis设置缓存失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     * 从 Redis 中获取指定键的值
     * 
     * @param key 缓存键，不能为null
     * @return Object 缓存值，不存在或发生异常时返回null
     */
    public Object get(String key) {
        try {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis获取缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 获取字符串缓存
     * 从 Redis 中获取指定键的值并转换为字符串
     * 
     * @param key 缓存键，不能为null
     * @return String 字符串值，不存在或发生异常时返回null
     */
    public String getString(String key) {
        try {
            Object value = get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Redis获取字符串缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis删除缓存失败: key={}", key, e);
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis判断key存在失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : 0;
        } catch (Exception e) {
            log.error("Redis获取过期时间失败: key={}", key, e);
            return 0;
        }
    }
}