package com.jieni.mqxq.controller.publicapi;

import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.vo.content.CarouselVO;
import com.jieni.mqxq.service.content.CarouselService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图公共接口控制器
 * 
 * 提供无需登录即可访问的轮播图查询功能，包括轮播图列表查询、轮播图详情查看、首页轮播图等
 * 只显示启用状态的轮播图内容，支持排序显示、限制数量等功能
 * 为前端首页和其他页面的轮播图展示提供数据支持
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/public/carousel")
@Tag(name = "轮播图公共API", description = "提供轮播图展示相关的公共接口")
public class CarouselPublicController {

    @Resource
    private CarouselService carouselService;

    /**
     * 获取启用的轮播图列表
     * 按排序字段升序排列
     */
    @Operation(summary = "获取启用的轮播图列表", description = "获取所有启用状态的轮播图，按排序字段升序排列")
    @GetMapping("/list")
    public Result<List<CarouselVO>> getEnabledCarouselList() {
        log.info("公共接口：获取启用的轮播图列表");
        List<CarouselVO> carouselList = carouselService.getEnabledCarouselList();
        return Result.success(carouselList);
    }

    /**
     * 根据ID获取轮播图详情
     */
    @Operation(summary = "获取轮播图详情", description = "根据ID获取单个轮播图的详细信息")
    @GetMapping("/{id}")
    public Result<CarouselVO> getCarouselById(
            @Parameter(description = "轮播图ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "轮播图ID必须大于0") Integer id) {
        log.info("公共接口：获取轮播图详情, ID: {}", id);
        CarouselVO carouselVO = carouselService.getCarouselById(id);
        return Result.success(carouselVO);
    }

    /**
     * 获取首页轮播图
     * 限制返回数量，提高页面加载速度
     */
    @Operation(summary = "获取首页轮播图", description = "获取首页展示的轮播图，限制数量")
    @GetMapping("/homepage")
    public Result<List<CarouselVO>> getHomepageCarousel(
            @Parameter(description = "限制返回数量，默认5个", example = "5")
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "限制数量必须大于0") Integer limit) {
        log.info("公共接口：获取首页轮播图, 限制数量: {}", limit);
        List<CarouselVO> carouselList = carouselService.getHomepageCarousel(limit);
        return Result.success(carouselList);
    }
    
}