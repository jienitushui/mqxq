package com.jieni.mqxq.service.content;

import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.domain.dto.content.CarouselCreateDTO;
import com.jieni.mqxq.domain.dto.content.CarouselPageQueryDTO;
import com.jieni.mqxq.domain.dto.content.CarouselUpdateDTO;
import com.jieni.mqxq.domain.vo.content.CarouselVO;

import java.util.List;

/**
 * 轮播图管理服务接口
 * 
 * 提供首页轮播图管理的完整服务功能，包括轮播图的CRUD操作、分页查询、状态管理等。
 * 支持启用状态管理、首页显示控制、数量限制等高级功能，为前端首页和其他页面的轮播图展示提供数据支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface CarouselService {

    /**
     * 创建轮播图
     *
     * @param createDTO 轮播图创建信息
     * @return 轮播图视图对象
     */
    CarouselVO createCarousel(CarouselCreateDTO createDTO);

    /**
     * 更新轮播图
     *
     * @param id 轮播图ID
     * @param updateDTO 轮播图更新信息
     * @return 轮播图视图对象
     */
    CarouselVO updateCarousel(Integer id, CarouselUpdateDTO updateDTO);

    /**
     * 根据ID获取轮播图详情
     *
     * @param id 轮播图ID
     * @return 轮播图视图对象
     */
    CarouselVO getCarouselById(Integer id);

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    void deleteCarousel(Integer id);

    /**
     * 分页查询轮播图
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageInfo<CarouselVO> pageQueryCarousels(CarouselPageQueryDTO queryDTO);

    /**
     * 获取所有轮播图列表
     *
     * @return 轮播图列表
     */
    List<CarouselVO> getAllCarousels();

    /**
     * 获取启用的轮播图列表（公共接口）
     * 按排序字段升序排列
     *
     * @return 启用的轮播图列表
     */
    List<CarouselVO> getEnabledCarouselList();

    /**
     * 获取首页轮播图（公共接口）
     * 限制返回数量
     *
     * @param limit 限制数量
     * @return 首页轮播图列表
     */
    List<CarouselVO> getHomepageCarousel(Integer limit);

}
