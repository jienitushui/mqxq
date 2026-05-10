package com.jieni.mqxq.service.dashboard;

import com.jieni.mqxq.domain.vo.dashboard.AdminDashboardOverviewVO;
import com.jieni.mqxq.domain.vo.dashboard.DistributionDataVO;
import com.jieni.mqxq.domain.vo.dashboard.RankingDataVO;
import com.jieni.mqxq.domain.vo.dashboard.TeacherDashboardOverviewVO;
import com.jieni.mqxq.domain.vo.dashboard.TrendDataVO;

import java.util.List;

/**
 * 数据大屏服务接口
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public interface DashboardService {
    
    // ========== 教师端接口 ==========
    
    /**
     * 获取教师端数据大屏概览
     * 
     * @param teacherId 教师ID
     * @return 概览数据
     */
    TeacherDashboardOverviewVO getTeacherOverview(Integer teacherId);
    
    /**
     * 获取课程状态分布
     * 
     * @param teacherId 教师ID
     * @return 分布数据
     */
    List<DistributionDataVO> getTeacherCourseStatusDistribution(Integer teacherId);
    
    /**
     * 获取学员学习状态分布
     * 
     * @param teacherId 教师ID
     * @return 分布数据
     */
    List<DistributionDataVO> getTeacherStudentStatusDistribution(Integer teacherId);
    
    /**
     * 获取课程浏览量趋势
     * 
     * @param teacherId 教师ID
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    List<TrendDataVO> getTeacherCourseViewTrend(Integer teacherId, Integer days);
    
    /**
     * 获取收入趋势
     * 
     * @param teacherId 教师ID
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    List<TrendDataVO> getTeacherRevenueTrend(Integer teacherId, Integer days);
    
    /**
     * 获取学员增长趋势
     * 
     * @param teacherId 教师ID
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    List<TrendDataVO> getTeacherStudentGrowthTrend(Integer teacherId, Integer days);
    
    /**
     * 获取课程销售排行
     * 
     * @param teacherId 教师ID
     * @param limit 数量限制
     * @return 排行数据
     */
    List<RankingDataVO> getTeacherTopCourses(Integer teacherId, Integer limit);
    
    /**
     * 获取课程完成率排行
     * 
     * @param teacherId 教师ID
     * @param limit 数量限制
     * @return 排行数据
     */
    List<RankingDataVO> getTeacherCourseCompletionRanking(Integer teacherId, Integer limit);
    
    // ========== 管理员端接口 ==========
    
    /**
     * 获取管理员端数据大屏概览
     * 
     * @return 概览数据
     */
    AdminDashboardOverviewVO getAdminOverview();
    
    /**
     * 获取用户角色分布
     * 
     * @return 分布数据
     */
    List<DistributionDataVO> getAdminUserRoleDistribution();
    
    /**
     * 获取用户状态分布
     * 
     * @return 分布数据
     */
    List<DistributionDataVO> getAdminUserStatusDistribution();
    
    /**
     * 获取课程分类分布
     * 
     * @return 分布数据
     */
    List<DistributionDataVO> getAdminCourseCategoryDistribution();
    
    /**
     * 获取课程状态分布
     * 
     * @return 分布数据
     */
    List<DistributionDataVO> getAdminCourseStatusDistribution();
    
    /**
     * 获取订单状态分布
     * 
     * @return 分布数据
     */
    List<DistributionDataVO> getAdminOrderStatusDistribution();
    
    /**
     * 获取学习状态分布
     * 
     * @return 分布数据
     */
    List<DistributionDataVO> getAdminLearningStatusDistribution();
    
    /**
     * 获取用户注册趋势
     * 
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    List<TrendDataVO> getAdminUserRegistrationTrend(Integer days);
    
    /**
     * 获取订单金额趋势
     * 
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    List<TrendDataVO> getAdminOrderRevenueTrend(Integer days);
    
    /**
     * 获取订单数量趋势
     * 
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    List<TrendDataVO> getAdminOrderCountTrend(Integer days);
    
    /**
     * 获取课程创建趋势
     * 
     * @param days 天数（默认30天）
     * @return 趋势数据
     */
    List<TrendDataVO> getAdminCourseCreationTrend(Integer days);
    
    /**
     * 获取课程浏览量排行
     * 
     * @param limit 数量限制
     * @return 排行数据
     */
    List<RankingDataVO> getAdminTopCoursesByViews(Integer limit);
    
    /**
     * 获取课程销售额排行
     * 
     * @param limit 数量限制
     * @return 排行数据
     */
    List<RankingDataVO> getAdminTopCoursesByRevenue(Integer limit);
    
    /**
     * 获取教师收入排行
     * 
     * @param limit 数量限制
     * @return 排行数据
     */
    List<RankingDataVO> getAdminTopTeachers(Integer limit);
}

