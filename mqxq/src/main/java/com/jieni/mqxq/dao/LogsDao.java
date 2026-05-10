package com.jieni.mqxq.dao;

import com.jieni.mqxq.domain.entity.Logs;

import java.util.List;

public interface LogsDao {

    /**
     * 新增
     */
    void insert(Logs logs);

    /**
     * 删除
     */
    void deleteById(Integer id);

    /**
     * 更新
     */
    void updateById(Logs logs);

    /**
     * 根据ID查询
     */
    Logs selectById(Integer id);

    /**
     * 条件查询
     */
    List<Logs> selectAll(Logs logs);

}
