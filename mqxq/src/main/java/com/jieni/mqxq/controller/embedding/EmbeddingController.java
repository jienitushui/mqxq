package com.jieni.mqxq.controller.embedding;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.StrUtil;
import com.jieni.mqxq.domain.vo.chat.CourseInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/embedding")
@RequiredArgsConstructor
@Tag(name = "向量化", description = "向量化相关接口，包括向量存储、检索等功能")
public class EmbeddingController {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    @Operation(summary = "保存向量数据", description = "将消息列表向量化并存储到向量数据库中")
    @PostMapping
    public void saveVectorStore(
            @Parameter(description = "消息列表", required = true)
            @RequestParam("messages") List<String> messages) {
        log.info("保存到向量数据库中，消息数据：{}", messages);
        //构建文档
        List<Document> documents = CollStreamUtil.toList(messages, message -> Document.builder()
                .text(message)
                .build());
        //存储到向量数据库中
        this.vectorStore.add(documents);
        log.info("保存到向量数据库成功, 数量：{}", messages.size());
    }


    @Operation(summary = "文本向量化", description = "将文本转换为向量表示")
    @GetMapping
    public EmbeddingResponse embed(
            @Parameter(description = "待向量化的文本", required = true, example = "这是一个测试文本")
            @RequestParam("message") String message) {
        return this.embeddingModel.embedForResponse(List.of(message));
    }

    @Operation(summary = "删除向量数据", description = "根据ID列表删除向量数据库中的数据")
    @DeleteMapping
    public void deleteVectorStore(
            @Parameter(description = "文档ID列表", required = true)
            @RequestParam("ids") List<String> ids) {
        // 删除向量数据库中的数据
        this.vectorStore.delete(ids);
    }

    @Operation(summary = "相似度搜索", description = "根据查询文本进行相似度搜索，返回最相似的5个文档")
    @GetMapping("/search")
    public List<Document> search(
            @Parameter(description = "查询文本", required = true, example = "Java编程")
            @RequestParam("message") String message) {
        return this.vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(5).build());
    }

    @Operation(summary = "搜索全部向量数据", description = "获取向量数据库中的所有数据（最多999条）")
    @GetMapping("/search/all")
    public List<Document> searchAll() {
        // 搜索全部数据
        return this.vectorStore.similaritySearch(SearchRequest.builder().query("").topK(999).build());
    }


    /**
     * 将课程数据向量化并存储到向量库
     *
     * @param courseInfo 课程信息
     * @return 操作结果
     */
    @Operation(summary = "向量化单个课程", description = "将单个课程信息向量化并存储到向量库中")
    @PostMapping("/course")
    public Map<String, Object> embedCourse(
            @Parameter(description = "课程信息", required = true)
            @RequestBody CourseInfo courseInfo) {
        log.info("开始向量化课程数据, courseId: {}, courseName: {}", courseInfo.getId(), courseInfo.getName());

        // 将课程信息转换为结构化的文本描述
        String courseText = convertCourseToText(courseInfo);

        // 创建文档，包含课程ID作为元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("courseId", courseInfo.getId());
        metadata.put("courseName", courseInfo.getName());
        metadata.put("type", "course");

        Document document = Document.builder()
                .id("course_" + courseInfo.getId()) // 使用课程ID作为文档ID
                .text(courseText)
                .metadata(metadata)
                .build();

        // 存储到向量库
        this.vectorStore.add(List.of(document));

        log.info("课程数据向量化成功, courseId: {}, courseName: {}", courseInfo.getId(), courseInfo.getName());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "课程数据向量化成功");
        result.put("courseId", courseInfo.getId());
        result.put("documentId", document.getId());
        return result;
    }

    /**
     * 批量向量化课程数据
     *
     * @param courseInfos 课程信息列表
     * @return 操作结果
     */
    @Operation(summary = "批量向量化课程", description = "批量将课程信息向量化并存储到向量库中")
    @PostMapping("/courses")
    public Map<String, Object> embedCourses(
            @Parameter(description = "课程信息列表", required = true)
            @RequestBody List<CourseInfo> courseInfos) {
        log.info("开始批量向量化课程数据, 数量: {}", courseInfos.size());

        List<Document> documents = CollStreamUtil.toList(courseInfos, courseInfo -> {
            String courseText = convertCourseToText(courseInfo);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("courseId", courseInfo.getId());
            metadata.put("courseName", courseInfo.getName());
            metadata.put("type", "course");

            return Document.builder()
                    .id("course_" + courseInfo.getId())
                    .text(courseText)
                    .metadata(metadata)
                    .build();
        });

        // 批量存储到向量库
        this.vectorStore.add(documents);

        log.info("批量课程数据向量化成功, 数量: {}", courseInfos.size());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量课程数据向量化成功");
        result.put("count", courseInfos.size());
        return result;
    }

    /**
     * 将课程信息转换为结构化的文本描述
     * 用于向量化，包含课程的关键信息
     * 确保课程ID和课程名称（title）都能被检索到
     * 根据MySQL表结构：course表包含title(课程标题)、description(课程简介)等字段
     *
     * @param courseInfo 课程信息
     * @return 文本描述
     */
    private String convertCourseToText(CourseInfo courseInfo) {
        StringBuilder text = new StringBuilder();

        // ========== 核心检索字段：ID和标题（title）优先放置 ==========
        // 课程ID以多种形式出现，确保能被检索到
        if (courseInfo.getId() != null) {
            text.append("课程ID：").append(courseInfo.getId()).append(" ");
            text.append("ID：").append(courseInfo.getId()).append(" ");
            text.append("编号：").append(courseInfo.getId()).append(" ");
            text.append("course_id：").append(courseInfo.getId()).append("\n");
        }

        // 课程标题（title字段，对应CourseInfo.name）多次出现，增加检索匹配度
        if (StrUtil.isNotBlank(courseInfo.getName())) {
            text.append("课程标题：").append(courseInfo.getName()).append(" ");
            text.append("课程名称：").append(courseInfo.getName()).append(" ");
            text.append("标题：").append(courseInfo.getName()).append(" ");
            text.append("名称：").append(courseInfo.getName()).append(" ");
            text.append("课程：").append(courseInfo.getName()).append(" ");
            text.append("title：").append(courseInfo.getName()).append("\n");
        }

        // ========== 课程描述（description字段，对应CourseInfo.detail）==========
        if (StrUtil.isNotBlank(courseInfo.getDetail())) {
            text.append("课程简介：").append(courseInfo.getDetail()).append(" ");
            text.append("课程描述：").append(courseInfo.getDetail()).append(" ");
            text.append("简介：").append(courseInfo.getDetail()).append(" ");
            text.append("description：").append(courseInfo.getDetail()).append("\n");
        }

        // ========== 价格信息（price字段）==========
        if (courseInfo.getPrice() != null) {
            text.append("课程价格：").append(courseInfo.getPrice()).append("元 ");
            text.append("价格：").append(courseInfo.getPrice()).append("元 ");
            if (courseInfo.getPrice() == 0) {
                text.append("免费课程 ");
            }
            text.append("price：").append(courseInfo.getPrice()).append("\n");
        }

        // ========== 课时信息（lesson_num字段）==========
        if (courseInfo.getLessonNum() != null) {
            text.append("总课时：").append(courseInfo.getLessonNum()).append("课时 ");
            text.append("课时数：").append(courseInfo.getLessonNum()).append(" ");
            text.append("lesson_num：").append(courseInfo.getLessonNum()).append("\n");
        }

        // ========== 时长信息（duration_sum字段，单位：秒）==========
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

        // ========== 销售和浏览数据（buy_count、view_count字段）==========
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

        // ========== 课程状态（status字段：0未发布，1已发布）==========
        if (courseInfo.getStatus() != null) {
            String statusText = courseInfo.getStatus() == 1 ? "已发布" : "未发布";
            text.append("课程状态：").append(statusText).append(" ");
            text.append("状态：").append(statusText).append(" ");
            text.append("status：").append(courseInfo.getStatus()).append("\n");
        }

        // ========== 教师和分类信息（teacher_id、subject_id字段）==========
        if (courseInfo.getTeacherId() != null) {
            text.append("教师ID：").append(courseInfo.getTeacherId()).append(" ");
            text.append("teacher_id：").append(courseInfo.getTeacherId()).append("\n");
        }

        if (courseInfo.getSubjectId() != null) {
            text.append("课程分类ID：").append(courseInfo.getSubjectId()).append(" ");
            text.append("分类ID：").append(courseInfo.getSubjectId()).append(" ");
            text.append("subject_id：").append(courseInfo.getSubjectId()).append("\n");
        }

        // ========== 在文本末尾再次添加课程ID和标题，增强检索匹配 ==========
        if (courseInfo.getId() != null) {
            text.append("ID").append(courseInfo.getId()).append(" ");
        }
        if (StrUtil.isNotBlank(courseInfo.getName())) {
            text.append(courseInfo.getName());
        }

        return text.toString();
    }

}