# Excel 导出功能说明

## 概述

已成功为项目集成 Apache POI 5.2.3，并实现了 7 个数据导出接口的完整功能。

## 已实现的导出接口

### 教师端（5个）

1. **导出课程评论**
   - 接口：`GET /api/teacher/comment/export/{courseId}`
   - 功能：导出指定课程的所有评论数据
   - 权限：教师角色，只能导出自己课程的评论
   - 导出字段：评论ID、用户ID、用户姓名、评分、评论内容、状态、评论时间

2. **导出作业提交记录**
   - 接口：`GET /api/teacher/homework-submission/export/{homeworkId}`
   - 功能：导出指定作业的所有提交记录
   - 权限：教师角色，只能导出自己作业的提交记录
   - 导出字段：学生ID、学生姓名、作业标题、提交内容、附件URL、得分、满分、教师评语、状态、提交时间、批改时间

3. **导出课程学员列表**
   - 接口：`GET /api/teacher/my-course/export-students/{courseId}`
   - 功能：导出指定课程的学员列表
   - 权限：教师角色，只能导出自己课程的学员
   - 导出字段：学员ID、学员姓名、课程ID、课程名称、学习进度、学习状态、加入时间、最后学习时间

4. **导出作业数据**
   - 接口：`GET /api/teacher/homework-manage/export?courseId=&status=`
   - 功能：导出教师的作业数据（可按课程ID、状态筛选）
   - 权限：教师角色
   - 导出字段：作业ID、作业标题、作业内容、课程ID、课程名称、满分、开始时间、截止时间、状态、创建时间、创建人

5. **导出作业提交记录（作业管理）**
   - 接口：`GET /api/teacher/homework-manage/export-submissions/{homeworkId}`
   - 功能：导出指定作业的提交记录（通过作业管理模块）
   - 权限：教师角色
   - 导出字段：同教师端作业提交记录

### 管理员端（2个）

6. **导出作业数据（管理员）**
   - 接口：`GET /api/admin/homework/export?courseId=&teacherId=&status=`
   - 功能：导出所有作业数据（可按课程ID、教师ID、状态筛选）
   - 权限：管理员角色
   - 导出字段：同教师端作业数据

7. **导出作业提交数据（管理员）**
   - 接口：`GET /api/admin/homework-submission/export`
   - 功能：导出所有作业提交数据
   - 权限：管理员角色
   - 导出字段：同教师端作业提交记录

## 技术实现

### 核心工具类

**ExcelExportUtil.java**
- 位置：`src/main/java/com/jieni/mqxq/util/ExcelExportUtil.java`
- 功能：
  - 基于 Apache POI 生成 Excel 文件
  - 支持自定义表头和数据映射
  - 自动格式化日期、数字等数据类型
  - 提供美观的表头样式（灰色背景、加粗字体）
  - 支持导出到 HTTP 响应流或本地文件

### 实现特点

1. **直接下载**：所有导出接口返回 Excel 文件流，浏览器直接下载
2. **临时文件管理**：使用系统临时目录存储文件，下载后自动删除
3. **文件命名**：自动生成带时间戳的文件名，避免冲突
4. **数据安全**：包含完善的权限校验，确保数据安全
5. **异常处理**：统一的异常处理机制，导出失败时返回友好提示

### 文件格式

- 文件类型：`.xlsx` (Excel 2007+)
- 编码：UTF-8
- 表头样式：灰色背景、加粗、居中对齐
- 数据样式：左对齐、自动换行
- 列宽：自动设置为 4000 单位

## 使用示例

### 前端调用示例

```javascript
// 导出课程评论
function exportComments(courseId) {
    window.location.href = `/api/teacher/comment/export/${courseId}`;
}

// 导出作业提交记录
function exportSubmissions(homeworkId) {
    window.location.href = `/api/teacher/homework-submission/export/${homeworkId}`;
}

// 导出课程学员
function exportStudents(courseId) {
    window.location.href = `/api/teacher/my-course/export-students/${courseId}`;
}

// 导出作业数据（带参数）
function exportHomework(courseId, status) {
    let url = '/api/teacher/homework-manage/export?';
    if (courseId) url += `courseId=${courseId}&`;
    if (status !== undefined) url += `status=${status}`;
    window.location.href = url;
}
```

### 使用 Axios 下载

```javascript
axios({
    url: '/api/teacher/comment/export/1',
    method: 'GET',
    responseType: 'blob',
    headers: {
        'mqxqtoken': 'your-token-here'
    }
}).then(response => {
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '课程评论.xlsx');
    document.body.appendChild(link);
    link.click();
    link.remove();
});
```

## 扩展建议

1. 添加更多导出格式（CSV、PDF、自定义模板）
2. 异步导出（消息队列 + 邮件通知）
3. 导出历史记录
4. 数据过滤和排序增强

## 注意事项

1. **权限控制**：所有导出接口都包含严格的权限校验
2. **数据量限制**：建议对大数据量导出进行分页或限制
3. **临时文件清理**：当前实现会在下载后删除临时文件
4. **并发处理**：高并发场景建议使用文件服务器存储
5. **字符编码**：文件名使用 UTF-8 编码，支持中文

## 依赖版本

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

---

## 测试示例

### 快速测试步骤

1. 启动项目：`mvn spring-boot:run`
2. 访问 API 文档：`http://localhost:9999/doc.html`（Knife4j增强文档界面，用户名：user，密码见application.yml中knife4j.basic.password配置）
   - 也可访问标准Swagger UI：`http://localhost:9999/swagger-ui/index.html`

### 教师端测试

| 接口 | URL | 参数 |
|------|-----|------|
| 导出课程评论 | `GET /api/teacher/comment/export/{courseId}` | courseId |
| 导出作业提交记录 | `GET /api/teacher/homework-submission/export/{homeworkId}` | homeworkId |
| 导出课程学员 | `GET /api/teacher/my-course/export-students/{courseId}` | courseId |
| 导出作业数据 | `GET /api/teacher/homework-manage/export` | courseId, status |
| 导出作业提交记录(管理) | `GET /api/teacher/homework-manage/export-submissions/{homeworkId}` | homeworkId |

### 管理员端测试

| 接口 | URL | 参数 |
|------|-----|------|
| 导出作业数据 | `GET /api/admin/homework/export` | courseId, teacherId, status |
| 导出作业提交数据 | `GET /api/admin/homework-submission/export` | - |

### cURL 测试

```bash
# 导出课程评论
curl -X GET "http://localhost:9999/api/teacher/comment/export/1" \
  -H "mqxqtoken: your-token-here" --output 课程评论.xlsx

# 导出作业提交记录
curl -X GET "http://localhost:9999/api/teacher/homework-submission/export/1" \
  -H "mqxqtoken: your-token-here" --output 作业提交记录.xlsx

# 导出课程学员
curl -X GET "http://localhost:9999/api/teacher/my-course/export-students/1" \
  -H "mqxqtoken: your-token-here" --output 课程学员列表.xlsx
```

### 常见问题排查

| 问题 | 排查方法 |
|------|---------|
| 下载文件无法打开 | 检查文件大小、Content-Type、后端日志 |
| 中文乱码 | 确认文件编码为 UTF-8 |
| 权限错误 | 确认已登录、角色正确、token 有效 |
| 数据为空 | 检查数据库数据、查询条件、后端日志 |
