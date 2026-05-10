package com.jieni.mqxq.service.infrastructure;

import cn.hutool.core.util.StrUtil;
import com.jieni.mqxq.domain.entity.Course;
import com.jieni.mqxq.domain.vo.chat.CourseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程向量库同步服务
 * 
 * 负责课程数据与向量库的同步操作，包括新增、修改、删除课程时同步更新向量库
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class CourseVectorSyncService {

    @Resource
    private VectorStore vectorStore;

    /**
     * 将课程添加到向量库
     * 仅在课程已发布时添加到向量库
     * 
     * @param course 课程实体
     */
    public void addCourseToVectorStore(Course course) {
        if (course == null || course.getId() == null) {
            log.warn("课程信息为空，跳过向量库同步");
            return;
        }

        // 只同步已发布的课程到向量库
        if (course.getStatus() == null || course.getStatus() != 1) {
            log.debug("课程未发布，跳过向量库同步, courseId: {}", course.getId());
            return;
        }

        try {
            CourseInfo courseInfo = convertToCourseInfo(course);
            if (courseInfo == null || courseInfo.getId() == null) {
                log.warn("课程信息转换失败，跳过向量库同步, courseId: {}", course.getId());
                return;
            }

            embedCourse(courseInfo);
            log.info("课程已添加到向量库, courseId: {}, courseName: {}", course.getId(), course.getTitle());
        } catch (Exception e) {
            log.error("添加课程到向量库失败, courseId: {}, courseName: {}", 
                    course.getId(), course.getTitle(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 更新向量库中的课程
     * 如果课程已发布，则更新向量库；如果课程未发布，则从向量库删除
     * 
     * @param course 课程实体
     */
    public void updateCourseInVectorStore(Course course) {
        if (course == null || course.getId() == null) {
            log.warn("课程信息为空，跳过向量库同步");
            return;
        }

        try {
            String documentId = "course_" + course.getId();
            
            // 如果课程已发布，更新向量库
            if (course.getStatus() != null && course.getStatus() == 1) {
                // 先删除旧的向量数据
                vectorStore.delete(List.of(documentId));
                
                // 重新向量化
                CourseInfo courseInfo = convertToCourseInfo(course);
                if (courseInfo != null && courseInfo.getId() != null) {
                    embedCourse(courseInfo);
                    log.info("课程已更新到向量库, courseId: {}, courseName: {}", course.getId(), course.getTitle());
                }
            } else {
                // 如果课程未发布，从向量库删除
                vectorStore.delete(List.of(documentId));
                log.info("课程已从向量库删除（未发布状态）, courseId: {}", course.getId());
            }
        } catch (Exception e) {
            log.error("更新课程到向量库失败, courseId: {}, courseName: {}", 
                    course.getId(), course.getTitle(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 从向量库删除课程
     * 
     * @param courseId 课程ID
     */
    public void deleteCourseFromVectorStore(Integer courseId) {
        if (courseId == null || courseId <= 0) {
            log.warn("课程ID无效，跳过向量库同步");
            return;
        }

        try {
            String documentId = "course_" + courseId;
            vectorStore.delete(List.of(documentId));
            log.info("课程已从向量库删除, courseId: {}", courseId);
        } catch (Exception e) {
            log.error("从向量库删除课程失败, courseId: {}", courseId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 将Course实体转换为CourseInfo VO
     *
     * @param course 课程实体
     * @return CourseInfo
     */
    private CourseInfo convertToCourseInfo(Course course) {
        if (course == null) {
            return null;
        }

        return CourseInfo.builder()
                .id(course.getId())
                .teacherId(course.getTeacherId())
                .subjectId(course.getSubjectId())
                .name(course.getTitle())
                .detail(course.getDescription())
                .price(course.getPrice() != null ? course.getPrice().doubleValue() : null)
                .lessonNum(course.getLessonNum())
                .durationSum(course.getDurationSum())
                .cover(course.getCover())
                .buyCount(course.getBuyCount())
                .viewCount(course.getViewCount())
                .status(course.getStatus())
                .publishTime(course.getPublishTime() != null ? 
                        course.getPublishTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime() : null)
                .createTime(course.getCreateTime() != null ? 
                        course.getCreateTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime() : null)
                .createUser(course.getCreateUser())
                .updateTime(course.getUpdateTime() != null ? 
                        course.getUpdateTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime() : null)
                .updateUser(course.getUpdateUser())
                .build();
    }

    /**
     * 向量化单个课程
     *
     * @param courseInfo 课程信息
     */
    private void embedCourse(CourseInfo courseInfo) {
        // 将课程信息转换为结构化的文本描述
        String courseText = convertCourseToText(courseInfo);
        
        // 创建文档，包含课程ID作为元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("courseId", courseInfo.getId());
        metadata.put("courseName", courseInfo.getName());
        metadata.put("type", "course");
        
        Document document = Document.builder()
                .id("course_" + courseInfo.getId())
                .text(courseText)
                .metadata(metadata)
                .build();
        
        // 存储到向量库
        vectorStore.add(List.of(document));
    }

    /**
     * 将课程信息转换为结构化的文本描述
     * 与CourseEmbeddingScheduledTask和EmbeddingController中的方法保持一致
     *
     * @param courseInfo 课程信息
     * @return 文本描述
     */
    private String convertCourseToText(CourseInfo courseInfo) {
        StringBuilder text = new StringBuilder();
        
        // 课程ID以多种形式出现
        if (courseInfo.getId() != null) {
            text.append("课程ID：").append(courseInfo.getId()).append(" ");
            text.append("ID：").append(courseInfo.getId()).append(" ");
            text.append("编号：").append(courseInfo.getId()).append(" ");
            text.append("course_id：").append(courseInfo.getId()).append("\n");
        }
        
        // 课程标题多次出现
        if (StrUtil.isNotBlank(courseInfo.getName())) {
            text.append("课程标题：").append(courseInfo.getName()).append(" ");
            text.append("课程名称：").append(courseInfo.getName()).append(" ");
            text.append("标题：").append(courseInfo.getName()).append(" ");
            text.append("名称：").append(courseInfo.getName()).append(" ");
            text.append("课程：").append(courseInfo.getName()).append(" ");
            text.append("title：").append(courseInfo.getName()).append("\n");
        }
        
        // 课程描述
        if (StrUtil.isNotBlank(courseInfo.getDetail())) {
            text.append("课程简介：").append(courseInfo.getDetail()).append(" ");
            text.append("课程描述：").append(courseInfo.getDetail()).append(" ");
            text.append("简介：").append(courseInfo.getDetail()).append(" ");
            text.append("description：").append(courseInfo.getDetail()).append("\n");
        }
        
        // 价格信息
        if (courseInfo.getPrice() != null) {
            text.append("课程价格：").append(courseInfo.getPrice()).append("元 ");
            text.append("价格：").append(courseInfo.getPrice()).append("元 ");
            if (courseInfo.getPrice() == 0) {
                text.append("免费课程 ");
            }
            text.append("price：").append(courseInfo.getPrice()).append("\n");
        }
        
        // 课时信息
        if (courseInfo.getLessonNum() != null) {
            text.append("总课时：").append(courseInfo.getLessonNum()).append("课时 ");
            text.append("课时数：").append(courseInfo.getLessonNum()).append(" ");
            text.append("lesson_num：").append(courseInfo.getLessonNum()).append("\n");
        }
        
        // 时长信息
        if (courseInfo.getDurationSum() != null) {
            int hours = courseInfo.getDurationSum() / 3600;
            int minutes = (courseInfo.getDurationSum() % 3600) / 60;
            text.append("总时长：");
            if (hours > 0) {
                text.append(hours).append("小时");
            }
            if (minutes > 0) {
                text.append(minutes).append("分钟");
            }
            text.append(" ");
            text.append("视频总时长：").append(courseInfo.getDurationSum()).append("秒 ");
            text.append("duration_sum：").append(courseInfo.getDurationSum()).append("\n");
        }
        
        // 销售和浏览数据
        if (courseInfo.getBuyCount() != null) {
            text.append("购买人数：").append(courseInfo.getBuyCount()).append("人 ");
            text.append("销量：").append(courseInfo.getBuyCount()).append(" ");
            text.append("销售数量：").append(courseInfo.getBuyCount()).append(" ");
            text.append("buy_count：").append(courseInfo.getBuyCount()).append("\n");
        }
        
        if (courseInfo.getViewCount() != null) {
            text.append("浏览次数：").append(courseInfo.getViewCount()).append("次 ");
            text.append("浏览量：").append(courseInfo.getViewCount()).append(" ");
            text.append("浏览数量：").append(courseInfo.getViewCount()).append(" ");
            text.append("view_count：").append(courseInfo.getViewCount()).append("\n");
        }
        
        // 课程状态
        if (courseInfo.getStatus() != null) {
            String statusText = courseInfo.getStatus() == 1 ? "已发布" : "未发布";
            text.append("课程状态：").append(statusText).append(" ");
            text.append("状态：").append(statusText).append(" ");
            text.append("status：").append(courseInfo.getStatus()).append("\n");
        }
        
        // 教师和分类信息
        if (courseInfo.getTeacherId() != null) {
            text.append("教师ID：").append(courseInfo.getTeacherId()).append(" ");
            text.append("teacher_id：").append(courseInfo.getTeacherId()).append("\n");
        }
        
        if (courseInfo.getSubjectId() != null) {
            text.append("课程分类ID：").append(courseInfo.getSubjectId()).append(" ");
            text.append("分类ID：").append(courseInfo.getSubjectId()).append(" ");
            text.append("subject_id：").append(courseInfo.getSubjectId()).append("\n");
        }
        
        // 在文本末尾再次添加课程ID和标题
        if (courseInfo.getId() != null) {
            text.append("ID").append(courseInfo.getId()).append(" ");
        }
        if (StrUtil.isNotBlank(courseInfo.getName())) {
            text.append(courseInfo.getName());
        }
        
        return text.toString();
    }
}

