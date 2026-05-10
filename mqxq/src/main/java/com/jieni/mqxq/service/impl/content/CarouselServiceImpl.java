package com.jieni.mqxq.service.impl.content;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jieni.mqxq.dao.CarouselDao;
import com.jieni.mqxq.domain.dto.content.CarouselCreateDTO;
import com.jieni.mqxq.domain.dto.content.CarouselPageQueryDTO;
import com.jieni.mqxq.domain.dto.content.CarouselUpdateDTO;
import com.jieni.mqxq.domain.entity.Carousel;
import com.jieni.mqxq.domain.vo.content.CarouselVO;
import com.jieni.mqxq.exception.MyException;
import com.jieni.mqxq.service.content.CarouselService;
import com.jieni.mqxq.util.SaUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 轮播图管理服务实现类
 * 
 * 提供首页轮播图的完整管理功能，包括轮播图的增删改查、排序管理等
 * 支持轮播图的启用状态控制、首页展示数量限制等业务逻辑
 * 实现轮播图的自动排序和展示优化功能
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CarouselServiceImpl implements CarouselService {
    
    @Resource
    private CarouselDao carouselDao;

    /**
     * 创建轮播图
     *
     * @param createDTO 轮播图创建信息
     * @return 轮播图视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarouselVO createCarousel(CarouselCreateDTO createDTO) {
        log.info("创建轮播图, 图片地址: {}", createDTO.getCarouselUrl());
        
        // 构建轮播图实体
        Carousel carousel = new Carousel();
        BeanUtils.copyProperties(createDTO, carousel);
        
        // 设置默认值
        carousel.setIsDeleted(0);
        if (carousel.getSort() == null) {
            carousel.setSort(0);
        }
        
        // 设置创建和更新时间
        Date now = new Date();
        carousel.setCreateTime(now);
        carousel.setUpdateTime(now);
        
        // 设置创建用户和更新用户
        Integer currentUserId = SaUtil.getLoginId();
        carousel.setCreateUser(currentUserId);
        carousel.setUpdateUser(currentUserId);
        
        // 保存到数据库
        carouselDao.insert(carousel);
        
        log.info("轮播图创建成功, ID: {}", carousel.getId());
        return convertToVO(carousel);
    }

    /**
     * 更新轮播图
     *
     * @param id 轮播图ID
     * @param updateDTO 轮播图更新信息
     * @return 轮播图视图对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarouselVO updateCarousel(Integer id, CarouselUpdateDTO updateDTO) {
        log.info("更新轮播图, ID: {}", id);
        
        // 验证轮播图是否存在
        Carousel existingCarousel = carouselDao.queryById(id);
        if (existingCarousel == null) {
            throw new MyException("轮播图不存在");
        }
        
        // 构建更新实体
        Carousel carousel = new Carousel();
        carousel.setId(id);
        BeanUtils.copyProperties(updateDTO, carousel);
        
        // 设置更新时间和更新用户
        carousel.setUpdateTime(new Date());
        carousel.setUpdateUser(SaUtil.getLoginId());
        
        // 更新数据库
        carouselDao.update(carousel);
        
        log.info("轮播图更新成功, ID: {}", id);
        return getCarouselById(id);
    }

    /**
     * 根据ID获取轮播图详情
     *
     * @param id 轮播图ID
     * @return 轮播图视图对象
     */
    @Override
    public CarouselVO getCarouselById(Integer id) {
        log.info("查询轮播图详情, ID: {}", id);
        
        Carousel carousel = carouselDao.queryById(id);
        if (carousel == null) {
            throw new MyException("轮播图不存在");
        }
        
        return convertToVO(carousel);
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCarousel(Integer id) {
        log.info("删除轮播图, ID: {}", id);
        
        // 验证轮播图是否存在
        Carousel carousel = carouselDao.queryById(id);
        if (carousel == null) {
            throw new MyException("轮播图不存在");
        }
        
        // 执行删除
        int result = carouselDao.deleteById(id);
        if (result <= 0) {
            throw new MyException("轮播图删除失败");
        }
        
        log.info("轮播图删除成功, ID: {}", id);
    }

    /**
     * 分页查询轮播图
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageInfo<CarouselVO> pageQueryCarousels(CarouselPageQueryDTO queryDTO) {
        log.info("分页查询轮播图, 页码: {}, 每页数量: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 构建查询条件
        Carousel condition = new Carousel();
        if (queryDTO.getIsDeleted() != null) {
            condition.setIsDeleted(queryDTO.getIsDeleted());
        } else {
            // 默认只查询未删除的
            condition.setIsDeleted(0);
        }
        
        // 执行分页查询
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Carousel> list = carouselDao.queryAllByLimit(condition);
        PageInfo<Carousel> pageInfo = new PageInfo<>(list);
        
        // 转换为VO
        PageInfo<CarouselVO> voPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, voPageInfo);
        voPageInfo.setList(list.stream().map(this::convertToVO).collect(Collectors.toList()));
        
        return voPageInfo;
    }

    /**
     * 获取所有轮播图列表
     *
     * @return 轮播图列表
     */
    @Override
    public List<CarouselVO> getAllCarousels() {
        log.info("查询所有轮播图");
        
        List<Carousel> carousels = carouselDao.queryAllByLimit(new Carousel());
        return carousels.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取启用的轮播图列表（公共接口）
     *
     * @return 启用的轮播图列表
     */
    @Override
    public List<CarouselVO> getEnabledCarouselList() {
        log.info("查询启用的轮播图列表");
        
        Carousel condition = new Carousel();
        condition.setIsDeleted(0);
        List<Carousel> carousels = carouselDao.queryAllByLimit(condition);
        
        // 按排序字段升序排列
        carousels.sort((c1, c2) -> {
            Integer sort1 = c1.getSort() != null ? c1.getSort() : 0;
            Integer sort2 = c2.getSort() != null ? c2.getSort() : 0;
            return sort1.compareTo(sort2);
        });
        
        return carousels.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取首页轮播图（公共接口）
     *
     * @param limit 限制数量
     * @return 首页轮播图列表
     */
    @Override
    public List<CarouselVO> getHomepageCarousel(Integer limit) {
        log.info("查询首页轮播图, 限制数量: {}", limit);
        
        List<CarouselVO> carousels = getEnabledCarouselList();
        
        // 如果指定了限制数量且大于0，并且结果数量超过限制，则截取前面的数据
        if (limit != null && limit > 0 && carousels.size() > limit) {
            return carousels.subList(0, limit);
        }
        
        return carousels;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将Carousel实体转换为CarouselVO
     *
     * @param carousel 轮播图实体
     * @return 轮播图视图对象
     */
    private CarouselVO convertToVO(Carousel carousel) {
        if (carousel == null) {
            return null;
        }
        
        CarouselVO vo = new CarouselVO();
        BeanUtils.copyProperties(carousel, vo);
        return vo;
    }

}
