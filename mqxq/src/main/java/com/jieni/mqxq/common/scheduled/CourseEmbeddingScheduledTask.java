//package com.jieni.mqxq.common.scheduled;
//
//import cn.hutool.core.collection.CollUtil;
//import com.jieni.mqxq.dao.CourseDao;
//import com.jieni.mqxq.domain.entity.Course;
//import com.jieni.mqxq.service.infrastructure.CourseVectorSyncService;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * 课程向量化定时任务
// *
// * 定期将数据库中的课程数据向量化并存储到向量库中
// * 支持全量同步和增量同步
// *
// * 注意：定时任务复用 CourseVectorSyncService 的同步逻辑，避免代码重复
// *
// * @author jieni
// * @version 1.0
// * @since 2025
// */
//@Slf4j
//@Component
//public class CourseEmbeddingScheduledTask {
//
//    @Resource
//    private CourseDao courseDao;
//
//    @Resource
//    private CourseVectorSyncService courseVectorSyncService;
//
//    /**
//     * 全量同步课程数据到向量库
//     * 每天凌晨2点执行一次
//     * Cron表达式：秒 分 时 日 月 周
//     */
//    @Scheduled(cron = "0 0 2 * * ?")
//    public void syncAllCoursesToVectorStore() {
//        log.info("========== 开始全量同步课程数据到向量库 ==========");
//
//        try {
//            // 查询所有已发布的课程
//            Course condition = new Course();
//            condition.setStatus(1); // 只同步已发布的课程
//            List<Course> courses = courseDao.queryAllByLimit(condition);
//
//            if (CollUtil.isEmpty(courses)) {
//                log.info("没有需要同步的课程数据");
//                return;
//            }
//
//            log.info("查询到 {} 门已发布课程，开始向量化", courses.size());
//
//            // 批量向量化（复用同步服务的逻辑）
//            int successCount = 0;
//            int failCount = 0;
//
//            for (Course course : courses) {
//                try {
//                    // 复用同步服务的添加方法（会自动处理已发布状态检查）
//                    courseVectorSyncService.addCourseToVectorStore(course);
//                    successCount++;
//                } catch (Exception e) {
//                    log.error("向量化课程失败, courseId: {}, courseName: {}",
//                            course.getId(), course.getTitle(), e);
//                    failCount++;
//                }
//            }
//
//            log.info("========== 全量同步完成 ==========");
//            log.info("成功: {} 门, 失败: {} 门, 总计: {} 门",
//                    successCount, failCount, courses.size());
//
//        } catch (Exception e) {
//            log.error("全量同步课程数据到向量库失败", e);
//        }
//    }
//
//    /**
//     * 增量同步课程数据到向量库
//     * 每小时执行一次，同步最近更新的课程
//     * Cron表达式：秒 分 时 日 月 周
//     */
//    @Scheduled(cron = "0 0 * * * ?")
//    // 增量同步 - 每分钟执行
////    @Scheduled(cron = "0 * * * * ?")
//    public void syncUpdatedCoursesToVectorStore() {
//        log.info("========== 开始增量同步课程数据到向量库 ==========");
//
//        try {
//            // 查询最近更新的已发布课程
//            // 注意：这里简化处理，实际可以根据update_time字段查询最近更新的课程
//            Course condition = new Course();
//            condition.setStatus(1); // 只同步已发布的课程
//            List<Course> courses = courseDao.queryAllByLimit(condition);
//
//            if (CollUtil.isEmpty(courses)) {
//                log.info("没有需要增量同步的课程数据");
//                return;
//            }
//
//            log.info("查询到 {} 门课程，开始增量向量化", courses.size());
//
//            int successCount = 0;
//            int failCount = 0;
//
//            for (Course course : courses) {
//                try {
//                    // 复用同步服务的更新方法（会自动处理删除旧数据并添加新数据）
//                    courseVectorSyncService.updateCourseInVectorStore(course);
//                    successCount++;
//                } catch (Exception e) {
//                    log.error("增量向量化课程失败, courseId: {}", course.getId(), e);
//                    failCount++;
//                }
//            }
//
//            log.info("========== 增量同步完成 ==========");
//            log.info("成功: {} 门, 失败: {} 门", successCount, failCount);
//
//        } catch (Exception e) {
//            log.error("增量同步课程数据到向量库失败", e);
//        }
//    }
//
//}
//
