package com.jieni.mqxq.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.dto.content.CarouselCreateDTO;
import com.jieni.mqxq.domain.dto.content.CarouselPageQueryDTO;
import com.jieni.mqxq.domain.dto.content.CarouselUpdateDTO;
import com.jieni.mqxq.domain.vo.content.CarouselVO;
import com.jieni.mqxq.service.content.CarouselService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图管理控制器
 * 
 * 提供管理员端轮播图的完整管理功能，包括创建、查询、更新和删除轮播图
 * 支持分页查询和详细信息查看，确保只有管理员角色可以访问
 * 使用统一的异常处理机制，通过抛出异常由全局异常处理器统一处理
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/carousel")
@Tag(name = "轮播图管理", description = "管理员端轮播图管理接口")
@CrossOrigin
@SaCheckRole("管理员")
public class CarouselController {

    @Resource
    private CarouselService carouselService;

    /**
     * 创建新轮播图
     */
    @Operation(summary = "创建轮播图", description = "创建新的轮播图")
    @PostMapping
    public Result<CarouselVO> createCarousel(@Valid @RequestBody CarouselCreateDTO createDTO) {
        log.info("管理员创建轮播图");
        CarouselVO carouselVO = carouselService.createCarousel(createDTO);
        return Result.success("轮播图创建成功", carouselVO);
    }

    /**
     * 更新轮播图信息
     */
    @Operation(summary = "更新轮播图", description = "更新轮播图信息")
    @PutMapping("/{id}")
    public Result<CarouselVO> updateCarousel(
            @Parameter(description = "轮播图ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "轮播图ID必须大于0") Integer id,
            @Valid @RequestBody CarouselUpdateDTO updateDTO) {
        log.info("管理员更新轮播图, ID: {}", id);
        CarouselVO carouselVO = carouselService.updateCarousel(id, updateDTO);
        return Result.success("轮播图更新成功", carouselVO);
    }

    /**
     * 根据ID获取轮播图详情
     */
    @Operation(summary = "获取轮播图详情", description = "根据轮播图ID获取详细信息")
    @GetMapping("/{id}")
    public Result<CarouselVO> getCarouselById(
            @Parameter(description = "轮播图ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "轮播图ID必须大于0") Integer id) {
        log.info("管理员查询轮播图详情, ID: {}", id);
        CarouselVO carouselVO = carouselService.getCarouselById(id);
        return Result.success(carouselVO);
    }

    /**
     * 删除轮播图
     */
    @Operation(summary = "删除轮播图", description = "删除指定的轮播图")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCarousel(
            @Parameter(description = "轮播图ID", required = true, in = ParameterIn.PATH, example = "1")
            @PathVariable @Min(value = 1, message = "轮播图ID必须大于0") Integer id) {
        log.info("管理员删除轮播图, ID: {}", id);
        carouselService.deleteCarousel(id);
        return Result.success("轮播图删除成功");
    }

    /**
     * 分页获取轮播图列表
     */
    @Operation(summary = "分页获取轮播图", description = "分页获取轮播图列表，支持按删除状态筛选")
    @GetMapping("/page")
    public Result<PageInfo<CarouselVO>> getCarouselsByPage(@Valid CarouselPageQueryDTO queryDTO) {
        log.info("管理员分页查询轮播图");
        PageInfo<CarouselVO> pageInfo = carouselService.pageQueryCarousels(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取所有轮播图列表
     */
    @Operation(summary = "获取所有轮播图", description = "获取系统中的所有轮播图列表")
    @GetMapping("/list")
    public Result<List<CarouselVO>> getAllCarousels() {
        log.info("管理员查询所有轮播图");
        List<CarouselVO> carousels = carouselService.getAllCarousels();
        return Result.success(carousels);
    }

}