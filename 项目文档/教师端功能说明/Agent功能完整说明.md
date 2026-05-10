# Agent 功能完整说明

本系统集成了 Manus 智能体（Agent），支持教师通过 AI 对话生成教案、Word 文件等。以下按智能体接口、文件生成下载、文件输出实现、文件清理逻辑、文件删除触发方式五部分说明。Token 优化过程详见 [Agent优化记录](Agent优化记录.md)。

---

## 一、智能体接口

### Manus智能体接口使用说明

#### 📋 接口信息

**接口地址：** `GET /api/teacher/manus/chat`

**请求方式：** GET

**Content-Type：** `application/x-www-form-urlencoded` 或 `text/plain`

**响应类型：** `text/event-stream` (SSE流式响应)

**权限要求：** 
- ✅ 需要登录认证
- ✅ 需要"教师"角色权限

---

#### 🔐 认证说明

所有请求都需要在请求头中携带认证Token：

```http
Authorization: Bearer {your_token}
```

---

#### 📝 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| message | String | 是 | 用户发送给Manus智能体的消息 | "帮我查询一下课程信息" |

---

#### 📤 请求示例

### 1. 使用 cURL 测试

```bash
curl -X GET "http://localhost:8080/api/teacher/manus/chat?message=你好，请介绍一下你自己" \
  -H "Authorization: Bearer your_token_here" \
  -H "Accept: text/event-stream"
```

### 2. 使用 Postman 测试

1. **请求方式**：选择 `GET`
2. **URL**：`http://localhost:8080/api/teacher/manus/chat`
3. **Params**：
   - Key: `message`
   - Value: `你好，请介绍一下你自己`
4. **Headers**：
   - `Authorization`: `Bearer your_token_here`
   - `Accept`: `text/event-stream`
5. **发送请求**：点击 Send

**注意**：Postman 可能无法很好地显示 SSE 流式响应，建议使用浏览器或专门的 SSE 客户端工具。

### 3. 使用浏览器 JavaScript (前端调用)

```javascript
// 方式1：使用 EventSource (推荐)
function chatWithManus(message) {
    // 注意：EventSource 不支持自定义请求头，需要将token放在URL参数中
    // 或者使用 fetch + ReadableStream 方式
    
    const url = `/api/teacher/manus/chat?message=${encodeURIComponent(message)}`;
    const eventSource = new EventSource(url);
    
    eventSource.onmessage = function(event) {
        console.log('收到数据:', event.data);
        // 处理接收到的数据
        // event.data 就是每一步的执行结果，格式如："Step 1: ..."
    };
    
    eventSource.onerror = function(error) {
        console.error('SSE连接错误:', error);
        eventSource.close();
    };
    
    // 如果需要携带认证token，使用方式2
}

// 方式2：使用 fetch + ReadableStream (支持自定义请求头)
async function chatWithManusWithAuth(message) {
    const url = `/api/teacher/manus/chat?message=${encodeURIComponent(message)}`;
    
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${your_token}`,
            'Accept': 'text/event-stream'
        }
    });
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    
    while (true) {
        const { done, value } = await reader.read();
        
        if (done) {
            console.log('流式响应结束');
            break;
        }
        
        // 解码数据
        const chunk = decoder.decode(value, { stream: true });
        console.log('收到数据块:', chunk);
        
        // SSE格式解析：每行以 "data: " 开头
        const lines = chunk.split('\n');
        for (const line of lines) {
            if (line.startsWith('data: ')) {
                const data = line.substring(6); // 去掉 "data: " 前缀
                console.log('解析后的数据:', data);
                // 处理数据，更新UI
            }
        }
    }
}

// 使用示例
chatWithManusWithAuth('帮我查询一下课程信息');
```

### 4. 使用 Axios (前端调用)

```javascript
import axios from 'axios';

async function chatWithManusAxios(message) {
    try {
        const response = await axios({
            method: 'GET',
            url: '/api/teacher/manus/chat',
            params: {
                message: message
            },
            headers: {
                'Authorization': `Bearer ${your_token}`,
                'Accept': 'text/event-stream'
            },
            responseType: 'stream' // 重要：设置为stream类型
        });
        
        // 处理流式响应
        response.data.on('data', (chunk) => {
            const data = chunk.toString();
            console.log('收到数据:', data);
            // 解析SSE格式并处理
        });
        
        response.data.on('end', () => {
            console.log('流式响应结束');
        });
        
    } catch (error) {
        console.error('请求错误:', error);
    }
}
```

### 5. Vue 3 组件示例

```vue
<template>
  <div>
    <input v-model="userMessage" @keyup.enter="sendMessage" placeholder="输入消息..." />
    <button @click="sendMessage">发送</button>
    <div class="response-area">
      <div v-for="(step, index) in responseSteps" :key="index" class="step">
        {{ step }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const userMessage = ref('');
const responseSteps = ref([]);

async function sendMessage() {
  if (!userMessage.value.trim()) return;
  
  // 清空之前的响应
  responseSteps.value = [];
  
  try {
    const response = await axios({
      method: 'GET',
      url: '/api/teacher/manus/chat',
      params: {
        message: userMessage.value
      },
      headers: {
        'Authorization': `Bearer ${your_token}`,
        'Accept': 'text/event-stream'
      },
      responseType: 'stream'
    });
    
    let buffer = '';
    
    response.data.on('data', (chunk) => {
      buffer += chunk.toString();
      
      // 按行分割处理SSE格式
      const lines = buffer.split('\n');
      buffer = lines.pop() || ''; // 保留最后不完整的行
      
      lines.forEach(line => {
        if (line.startsWith('data: ')) {
          const data = line.substring(6);
          responseSteps.value.push(data);
        }
      });
    });
    
    response.data.on('end', () => {
      console.log('响应完成');
    });
    
  } catch (error) {
    console.error('请求失败:', error);
    responseSteps.value.push('请求失败: ' + error.message);
  }
}
</script>
```

---

#### 📥 响应格式

### SSE 流式响应格式

Manus智能体返回的是 **Server-Sent Events (SSE)** 格式的流式响应。

**响应示例：**

```
data: Step 1: 正在思考如何解决您的问题...

data: Step 2: 已选择工具：文件操作工具

data: Step 3: 工具执行结果：文件读取成功

data: Step 4: 根据查询结果，我为您找到以下信息...

data: 执行结束：达到最大步骤（10）
```

**响应说明：**
- 每行以 `data: ` 开头，后面跟着实际数据
- 每个 `data:` 行后面有一个空行（`\n\n`）
- 数据格式为：`Step X: 执行结果描述`
- 最后会发送完成标记或错误信息

---

#### 🎯 响应数据说明

### 正常响应

每一步执行都会返回一个字符串，格式为：
```
Step {步骤编号}: {执行结果描述}
```

例如：
- `Step 1: 正在分析您的问题...`
- `Step 2: 已选择工具：WebSearchTool`
- `Step 3: 工具执行结果：查询到相关信息`
- `Step 4: 根据查询结果，我为您整理如下...`

### 完成标记

当所有步骤执行完成或达到最大步骤数时，会发送：
```
执行结束：达到最大步骤（10）
```

### 错误响应

如果执行过程中出现错误，会发送：
```
执行错误：{错误信息}
```

---

#### ⚠️ 注意事项

### 1. 超时设置
- 接口超时时间为 **5分钟**（300秒）
- 如果智能体执行时间较长，请确保客户端有足够的超时时间

### 2. 权限要求
- 必须已登录
- 必须具有"教师"角色权限
- Token 需要在请求头中正确传递

### 3. 参数编码
- `message` 参数需要进行 URL 编码
- 特殊字符需要使用 `encodeURIComponent()` 处理

### 4. SSE 连接管理
- 客户端需要正确处理连接断开和重连
- 建议实现错误重试机制
- 长时间无响应时，考虑主动关闭连接

### 5. 状态管理
- Manus智能体每次调用都会创建新实例
- 不支持会话状态保持（每次调用都是独立的）
- 如果需要多轮对话，需要在 `message` 中包含完整的上下文

---

#### 🔍 调试技巧

### 1. 查看日志

后端日志会记录：
- 教师调用Manus智能体的请求
- 每一步的执行过程
- 工具选择和执行结果

### 2. 使用浏览器开发者工具

在 Network 标签页中：
- 找到对应的 SSE 请求
- 查看 Response 标签页可以看到实时流式数据
- 查看 Headers 确认请求头是否正确

### 3. 测试工具推荐

- **Postman**：可以发送请求，但SSE显示不友好
- **curl**：命令行工具，适合快速测试
- **浏览器控制台**：使用 JavaScript 代码测试
- **SSE 客户端工具**：如 `sse-client` 等专门工具

---

#### 📚 相关接口

如果需要会话管理功能，可以参考：
- `/api/user/session` - 创建会话（用户端）
- `/api/user/chat` - 发送消息（用户端，支持会话）

**注意**：Manus智能体接口是独立的，不依赖会话系统。

---

#### 🐛 常见问题

### Q1: 返回 401 未授权错误
**A:** 检查请求头中是否正确携带了 `Authorization: Bearer {token}`，以及Token是否有效。

### Q2: 返回 403 无权限错误
**A:** 确认当前用户角色是否为"教师"，如果不是需要切换角色或联系管理员。

### Q3: 连接立即断开
**A:** 检查 `message` 参数是否为空，空消息会导致连接立即关闭。

### Q4: 前端无法接收数据
**A:** 
- 确认设置了正确的 `Accept: text/event-stream` 头
- 检查 CORS 配置是否允许 SSE 连接
- 确认使用了正确的 SSE 解析方式

### Q5: 响应数据格式不正确
**A:** SSE 格式要求每行以 `data: ` 开头，后面跟空行。确保正确解析了 SSE 格式。

---

#### 📞 技术支持

如有问题，请查看：
- 项目日志文件
- Swagger API 文档（如果已配置）
- 联系开发团队

---

**最后更新：** 2025-01-XX


---

## 二、文件生成与下载流程

### Agent文件生成与下载流程说明

#### 📋 概述

本文档详细说明了Manus智能体如何生成文件（如教案）并返回给用户的完整流程。

---

#### 🔄 完整流程

### 1. 用户请求

**用户输入示例：**
```
帮我写一个关于python基础语法的教案
```

**API调用：**
```http
GET /api/teacher/manus/chat?message=帮我写一个关于python基础语法的教案
Authorization: Bearer {token}
Accept: text/event-stream
```

### 2. Agent执行流程

```
用户请求
    ↓
ManusTeacherController.doChatWithManus()
    ↓
创建YuManus实例
    ↓
执行runStream()方法（SSE流式响应）
    ↓
循环执行步骤（think → act）
    ↓
Agent分析任务，选择工具
    ↓
调用WordGenerationTool生成Word文档
    ↓
文件保存到: /tmp/word/{fileName}.docx
    ↓
返回包含下载链接的消息
    ↓
通过SSE流式返回给用户
```

### 3. 文件生成过程

**WordGenerationTool执行：**
1. 接收参数：
   - `fileName`: 文件名（如：`python基础语法教案.docx`）
   - `textContents`: 文本内容列表
   - `imagePaths`: 图片路径列表（可选）

2. 生成Word文档：
   - 创建目录：`/tmp/word/`
   - 生成Word文件到：`/tmp/word/{fileName}.docx`
   - 支持Markdown格式转换（标题、列表、代码块等）

3. 返回结果：
   ```
   Word文档已成功生成！
   文件路径：/tmp/word/python基础语法教案.docx
   下载链接：http://localhost:9999/api/teacher/manus/download/word/python基础语法教案.docx
   
   您可以通过下载链接获取文件，或者直接访问文件路径。
   ```

### 4. 文件下载

**下载接口：**
```http
GET /api/teacher/manus/download/{fileType}/{fileName}
Authorization: Bearer {token}
```

**参数说明：**
- `fileType`: 文件类型，可选值：
  - `word` - Word文档（.docx）
  - `pdf` - PDF文档（.pdf）
  - `file` - 普通文件（.md, .txt等）
- `fileName`: 文件名（包含扩展名），支持中文

**示例：**
```http
GET /api/teacher/manus/download/word/python基础语法教案.docx
```

**响应：**
- 返回文件流，浏览器会自动下载
- 支持中文文件名（URL编码）

---

#### 📁 文件存储结构

```
项目根目录/
└── tmp/
    ├── word/          # Word文档存储目录
    │   └── *.docx
    ├── pdf/           # PDF文档存储目录
    │   └── *.pdf
    ├── file/          # 普通文件存储目录
    │   └── *.md, *.txt
    └── download/      # 下载资源存储目录
        └── *
```

---

#### 🎯 使用示例

### 示例1：生成Python基础语法教案

**用户输入：**
```
帮我写一个关于python基础语法的教案
```

**Agent执行过程：**
1. Agent分析任务，理解需要生成教案
2. Agent调用`writeFile`工具，先创建教案大纲
3. Agent调用`WordGenerationTool`生成Word文档
4. 返回包含下载链接的消息

**SSE响应示例：**
```
data: Step 1: 正在分析您的需求，准备生成Python基础语法教案...

data: Step 2: 已选择工具：FileOperationTool，正在创建教案大纲...

data: Step 3: 已选择工具：WordGenerationTool，正在生成Word文档...

data: Step 4: Word文档已成功生成！
文件路径：/tmp/word/python基础语法教案.docx
下载链接：http://localhost:9999/api/teacher/manus/download/word/python基础语法教案.docx

您可以通过下载链接获取文件，或者直接访问文件路径。

data: 执行结束：任务已完成
```

**前端处理：**
```javascript
// 解析SSE响应，提取下载链接
const downloadUrl = extractDownloadUrl(sseResponse);

// 创建下载按钮或自动下载
if (downloadUrl) {
    // 方式1：创建下载链接
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = '教案.docx';
    link.click();
    
    // 方式2：使用fetch下载
    fetch(downloadUrl, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.blob())
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = '教案.docx';
        a.click();
    });
}
```

---

#### 🔧 技术实现细节

### 1. WordGenerationTool优化

**修改前：**
```java
return "Word document generated successfully to: " + filePath;
```

**修改后：**
```java
String downloadUrl = String.format("http://localhost:%d/api/teacher/manus/download/word/%s", 
        SERVER_PORT, URLEncoder.encode(fileName, StandardCharsets.UTF_8));

return String.format("Word文档已成功生成！\n文件路径：%s\n下载链接：%s\n\n您可以通过下载链接获取文件，或者直接访问文件路径。", 
        filePath, downloadUrl);
```

### 2. 文件下载接口

**新增接口：**
```java
@GetMapping("/download/{fileType}/{fileName}")
public ResponseEntity<SpringResource> downloadFile(
        @PathVariable String fileType,
        @PathVariable String fileName)
```

**功能：**
- 支持多种文件类型（word、pdf、file）
- 支持中文文件名（URL编码）
- 自动设置Content-Disposition响应头
- 返回文件流供浏览器下载

### 3. SSE流式响应

**BaseAgent.runStream()方法：**
- 创建SseEmitter（5分钟超时）
- 异步执行agent步骤
- 每步执行结果通过SSE发送
- 支持实时显示执行进度

---

#### ⚠️ 注意事项

### 1. 文件路径
- 文件保存在服务器本地：`/tmp/{fileType}/`
- 确保目录有写入权限
- 生产环境建议使用对象存储（如MinIO）

### 2. 文件下载
- 需要登录认证（Bearer Token）
- 需要"教师"角色权限
- 文件名支持中文，会自动URL编码

### 3. 服务器端口
- 当前硬编码为9999（从application.yml读取）
- 生产环境需要配置正确的服务器地址
- 建议使用配置项或动态获取

### 4. 文件清理
- 当前未实现自动清理机制
- 建议定期清理临时文件
- 或实现文件过期删除策略

---

#### 🚀 优化建议

### 1. 文件上传到MinIO
**当前方案：** 文件保存在本地
**优化方案：** 生成后自动上传到MinIO，返回MinIO访问URL

```java
// 在WordGenerationTool中
String minioUrl = minIOFileStorageService.uploadFile(
    fileName, 
    new FileInputStream(filePath), 
    ContentType.WORD
);
return "文件已生成并上传，访问链接：" + minioUrl;
```

### 2. 动态服务器地址
**当前方案：** 硬编码localhost:9999
**优化方案：** 从配置读取或动态获取

```java
@Value("${server.address:http://localhost:${server.port}}")
private String serverAddress;
```

### 3. 文件管理
- 添加文件列表接口
- 添加文件删除接口
- 添加文件过期清理机制

### 4. 错误处理
- 文件生成失败时的友好提示
- 文件不存在时的错误处理
- 下载失败时的重试机制

---

#### 📝 相关文件

### 核心文件
- `ManusTeacherController.java` - Agent控制器，包含聊天和下载接口
- `YuManus.java` - Manus智能体实现
- `ToolCallAgent.java` - 工具调用Agent基类
- `WordGenerationTool.java` - Word文档生成工具

### 配置文件
- `application.yml` - 服务器端口等配置
- `ToolRegistration.java` - 工具注册配置

### 文档
- `Manus智能体接口使用说明.md` - API使用文档
- `Agent文件生成与下载流程说明.md` - 本文档

---

#### 🔍 调试技巧

### 1. 查看生成的文件
```bash
### 查看word目录
ls -la tmp/word/

### 查看文件内容（如果是文本文件）
cat tmp/file/xxx.md
```

### 2. 测试下载接口
```bash
### 使用curl测试
curl -X GET "http://localhost:9999/api/teacher/manus/download/word/教案.docx" \
  -H "Authorization: Bearer {token}" \
  -o 教案.docx
```

### 3. 查看日志
```bash
### 查看agent执行日志
tail -f logs/application.log | grep "Manus"
```

---

#### 📞 常见问题

### Q1: 文件生成成功但无法下载
**A:** 检查：
1. 文件是否真的存在
2. 下载URL是否正确
3. 是否有权限访问
4. 服务器地址是否正确

### Q2: 中文文件名乱码
**A:** 确保：
1. 文件名使用URLEncoder编码
2. 响应头设置正确的字符集
3. 浏览器支持UTF-8编码

### Q3: 文件路径问题（Windows）
**A:** 注意：
- Windows使用反斜杠`\`
- Linux使用正斜杠`/`
- 建议使用`File.separator`或`Paths.get()`

### Q4: SSE连接断开
**A:** 检查：
1. 超时时间设置（当前5分钟）
2. 网络连接稳定性
3. 服务器资源是否充足

---

**最后更新：** 2025-01-XX
**版本：** 1.0


---

## 三、文件输出实现要点

> 以下为第二节"文件生成与下载流程"的**实现要点摘要**，详细流程和示例代码请参见第二节。

### 修改的文件

- `src/main/java/com/jieni/mqxq/controller/teacher/ManusTeacherController.java` — 新增下载接口
- `src/main/java/com/jieni/mqxq/tools/WordGenerationTool.java` — 优化返回消息，添加下载链接
- `src/main/java/com/jieni/mqxq/tools/ToolRegistration.java` — 注入 `serverPort`，传给 WordGenerationTool

```java
// ToolRegistration.java
@Value("${server.port:9999}")
private int serverPort;

WordGenerationTool wordGenerationTool = new WordGenerationTool(serverPort);
```

### 测试建议

1. **测试文件生成**：
   ```bash
   curl -X GET "http://localhost:9999/api/teacher/manus/chat?message=帮我写一个关于python基础语法的教案" \
     -H "Authorization: Bearer {token}" -H "Accept: text/event-stream"
   ```

2. **测试文件下载**：
   ```bash
   curl -X GET "http://localhost:9999/api/teacher/manus/download/word/教案.docx" \
     -H "Authorization: Bearer {token}" -o 教案.docx
   ```

3. **检查生成的文件**：`ls -la tmp/word/`


---

## 四、文件清理逻辑

### 文件删除逻辑说明

#### 📋 概述

文件删除功能用于自动清理 Agent 生成的临时文件，防止磁盘空间占用过多。支持定时自动清理和手动触发清理两种方式。

---

#### 🔧 配置参数

### 配置文件位置
`src/main/resources/application.yml`

### 配置项说明

> 定时任务 Cron 配置详见 [公共参考-定时任务配置](../公共参考/系统基础设施配置汇总.md#三定时任务配置)

核心配置：`file.cleanup.cron: "0 0 3 * * ?"`（每天凌晨3点）、`retention-hours: 24`、`base-dir: ${user.dir}/tmp`

---

#### 🗂️ 清理目录结构

清理功能会扫描以下子目录：

```
tmp/
├── file/      # 普通文件目录
├── word/      # Word文档目录
└── download/  # 下载资源目录
```

---

#### 🔄 执行流程

### 1. 定时自动清理流程

```
启动应用
    ↓
@Scheduled 定时任务触发（每天凌晨3点）
    ↓
检查 enabled 配置（是否启用）
    ↓
[未启用] → 跳过清理，记录日志
    ↓
[已启用] → 执行 cleanupFiles() 方法
    ↓
检查基础目录是否存在
    ↓
[不存在] → 跳过清理，记录日志
    ↓
[存在] → 遍历子目录（file, word, download）
    ↓
对每个子目录调用 cleanupDirectory()
    ↓
递归清理文件和子目录
    ↓
统计删除文件数和释放空间
    ↓
记录清理结果日志
```

### 2. 手动触发清理流程

```
API调用 /api/teacher/manus/cleanup
    ↓
Controller.triggerCleanup()
    ↓
调用 fileCleanupScheduledTask.manualCleanup()
    ↓
执行 cleanupFiles() 方法
    ↓
（后续流程与定时清理相同）
```

---

#### 📝 核心方法详解

### 1. `cleanupFiles()` - 主清理方法

**功能：** 执行文件清理的主入口

**执行步骤：**
1. 检查 `enabled` 配置，如果禁用则直接返回
2. 检查基础目录是否存在
3. 计算当前时间和保留时间阈值
4. 遍历三个子目录（file, word, download）
5. 对每个子目录调用 `cleanupDirectory()` 进行清理
6. 统计并记录清理结果

**关键代码：**
```java
long currentTime = System.currentTimeMillis();
long retentionMillis = retentionHours * 60 * 60 * 1000L;  // 转换为毫秒
```

---

### 2. `cleanupDirectory(File dir, long currentTime, long retentionMillis)` - 递归清理方法

**功能：** 递归清理指定目录中的过期文件

**参数说明：**
- `dir`: 要清理的目录
- `currentTime`: 当前时间（毫秒时间戳）
- `retentionMillis`: 保留时间（毫秒）

**处理逻辑：**

#### 2.1 处理文件（file.isFile()）

```
遍历目录中的每个文件
    ↓
获取文件最后修改时间 lastModified
    ↓
计算文件年龄：age = currentTime - lastModified
    ↓
判断：age > retentionMillis？
    ↓
[否] → 文件未过期，跳过
    ↓
[是] → 文件已过期，执行删除
    ↓
file.delete() 删除文件
    ↓
[成功] → 统计删除数量和释放空间
    ↓
[失败] → 记录警告日志
```

**删除条件：**
- 文件年龄 > 保留时间（默认24小时）
- 例如：文件创建于25小时前，当前保留时间为24小时，则会被删除

#### 2.2 处理子目录（file.isDirectory()）

```
发现子目录
    ↓
递归调用 cleanupDirectory() 清理子目录
    ↓
统计子目录的删除结果
    ↓
检查子目录是否为空
    ↓
[为空] → 删除空目录
    ↓
[不为空] → 保留目录
```

**空目录处理：**
- 清理完文件后，如果目录为空，会自动删除该目录
- 避免留下大量空目录占用空间

#### 2.3 异常处理

```
处理文件/目录时发生异常
    ↓
捕获异常，记录警告日志
    ↓
继续处理下一个文件/目录
    ↓
不影响整体清理流程
```

---

#### 📊 删除判断逻辑

### 时间计算

```
当前时间：2025-01-17 03:00:00
文件修改时间：2025-01-16 02:00:00
保留时间：24小时

文件年龄 = 当前时间 - 文件修改时间
         = 25小时

判断：25小时 > 24小时？
结果：是 → 删除文件
```

### 示例场景

| 文件修改时间 | 当前时间 | 保留时间 | 文件年龄 | 是否删除 |
|------------|---------|---------|---------|---------|
| 2025-01-16 03:00 | 2025-01-17 03:00 | 24小时 | 24小时 | ❌ 不删除（刚好24小时） |
| 2025-01-16 02:00 | 2025-01-17 03:00 | 24小时 | 25小时 | ✅ 删除（超过24小时） |
| 2025-01-16 20:00 | 2025-01-17 03:00 | 24小时 | 7小时 | ❌ 不删除（未超过24小时） |

---

#### 🎯 清理范围

### 清理的文件类型
- ✅ 所有超过保留时间的文件
- ✅ 空目录（清理后自动删除）

### 不清理的内容
- ❌ 未超过保留时间的文件
- ❌ 非空目录（即使目录本身过期，只要里面有未过期文件就不删除）

---

#### 📈 统计信息

清理完成后会记录以下统计信息：

```
删除文件数：X 个
释放空间：Y MB
```

**统计方式：**
- 统计所有子目录的删除结果
- 累加删除文件数量
- 累加释放的磁盘空间（字节转MB）

---

#### 🔍 日志记录

### 日志级别

| 级别 | 场景 | 示例 |
|------|------|------|
| `INFO` | 清理开始/结束、统计信息 | `========== 开始清理临时文件 ==========` |
| `DEBUG` | 单个文件删除详情 | `删除过期文件: xxx.docx, 文件年龄: 25 小时` |
| `WARN` | 删除失败、处理异常 | `删除文件失败: /path/to/file` |
| `ERROR` | 清理过程严重错误 | `清理临时文件失败` |

---

#### 🛠️ 使用方式

三种触发方式（定时自动、Agent工具、API接口）详见**第五节"文件删除触发方式"**。

#### ⚠️ 注意事项

### 1. 删除不可恢复
- 文件删除后无法恢复，请确保保留时间设置合理
- 建议重要文件及时下载或移动到其他位置

### 2. 文件年龄计算
- 基于文件的**最后修改时间**（`lastModified`）
- 不是基于文件创建时间
- 如果文件被修改过，会重新计算时间

### 3. 并发安全
- 清理过程中如果文件正在被使用，删除可能失败
- 会记录警告日志，但不影响其他文件的清理

### 4. 目录权限
- 确保应用有删除文件的权限
- 某些系统文件或只读文件可能无法删除

### 5. 性能考虑
- 大量文件清理可能耗时较长
- 建议在低峰期执行（默认凌晨3点）
- 递归清理深度过深可能影响性能

---

#### 🔧 配置建议

### 生产环境
```yaml
file:
  cleanup:
    enabled: true
    retention-hours: 48        # 保留48小时，给用户更多时间下载
    cron: "0 0 3 * * ?"       # 凌晨3点执行，避开业务高峰期
```

### 开发环境
```yaml
file:
  cleanup:
    enabled: true
    retention-hours: 12        # 保留12小时，快速清理测试文件
    cron: "0 0 */6 * * ?"     # 每6小时执行一次，更频繁清理
```

### 禁用清理
```yaml
file:
  cleanup:
    enabled: false            # 完全禁用自动清理
```

---

#### 📚 相关文件

- **实现类：** `FileCleanupScheduledTask.java`
- **控制器：** `ManusTeacherController.java`
- **工具类：** `FileCleanupTool.java`
- **DTO：** `FileStatsDTO.java`
- **配置文件：** `application.yml`

---

#### 🔄 流程图

```
┌─────────────────┐
│  定时任务触发    │
│  (每天凌晨3点)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 检查是否启用？   │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
  否│         │是
    │         │
    ▼         ▼
┌──────┐  ┌─────────────────┐
│ 跳过 │  │ 检查目录是否存在 │
└──────┘  └────────┬────────┘
                   │
              ┌────┴────┐
              │         │
           否│         │是
              │         │
              ▼         ▼
         ┌──────┐  ┌─────────────────┐
         │ 跳过 │  │ 遍历子目录       │
         └──────┘  │ (file/word/...) │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │ 递归清理目录     │
                   │ cleanupDirectory│
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │ 判断文件年龄      │
                   │ age > retention? │
                   └────────┬────────┘
                            │
                    ┌───────┴───────┐
                    │               │
                  是│               │否
                    │               │
                    ▼               ▼
            ┌───────────┐    ┌──────────┐
            │ 删除文件   │    │ 保留文件  │
            └─────┬─────┘    └──────────┘
                  │
                  ▼
         ┌─────────────────┐
         │ 统计删除结果     │
         │ (数量、空间)     │
         └─────────────────┘
```

---

#### 📝 总结

文件删除逻辑采用**基于时间的过期策略**，通过定时任务自动清理超过保留时间的文件。核心特点：

1. ✅ **自动化**：定时执行，无需人工干预
2. ✅ **可配置**：保留时间、执行时间可灵活配置
3. ✅ **递归清理**：支持子目录的递归清理
4. ✅ **安全可靠**：异常处理完善，不影响整体流程
5. ✅ **统计完善**：记录详细的清理结果

通过合理的配置，可以有效控制临时文件的磁盘占用，保持系统运行效率。


---

## 五、文件删除触发方式

> 清理算法和配置参数详见第四节"文件清理逻辑"。本节聚焦三种触发方式的差异和使用场景。

#### 🎯 触发方式总览

| 触发方式 | 适用场景 | 权限要求 | 执行时机 |
|---------|---------|---------|---------|
| **1. 定时自动触发** | 日常维护 | 无需权限 | 每天凌晨3点（可配置） |
| **2. Agent工具触发** | 智能对话中 | 教师角色 | 用户请求时 |
| **3. API接口触发** | 手动管理 | 教师角色 | 调用接口时 |

---

#### 1️⃣ 定时自动触发

由 Spring `@Scheduled` 注解驱动，配置见第四节。默认每天凌晨3点执行 `cleanupFiles()`，可通过 `file.cleanup.enabled=false` 禁用。

---

#### 2️⃣ Agent工具触发（智能对话）

### 工作原理

通过 `FileCleanupTool` 工具类，Agent 可以在对话过程中调用文件清理功能。

### 工具注册

**文件：** `src/main/java/com/jieni/mqxq/tools/ToolRegistration.java`

```java
@Bean
public ToolCallback[] allTools() {
    // ...
    if (fileCleanupTask != null) {
        FileCleanupTool fileCleanupTool = new FileCleanupTool(fileCleanupTask);
        return ToolCallbacks.from(
            // ... 其他工具
            fileCleanupTool  // 注册文件清理工具
        );
    }
}
```

### 可用工具方法

**文件：** `src/main/java/com/jieni/mqxq/tools/FileCleanupTool.java`

#### 方法1：获取文件统计信息
```java
@Tool(description = "Get file directory statistics")
public String getFileStats()
```

#### 方法2：手动触发文件清理
```java
@Tool(description = "Manually trigger file cleanup")
public String cleanupFiles()
```

### 使用示例

#### 示例1：用户请求查看文件统计

**用户输入：**
```
帮我看看临时文件有多少，占用了多少空间
```

**Agent 执行流程：**
```
用户请求
    ↓
Agent 理解意图
    ↓
调用 getFileStats() 工具
    ↓
返回统计信息
    ↓
Agent 格式化并返回给用户
```

**返回结果：**
```
文件目录统计:
基础目录: D:\Desktop\毕设码趣星球\mqxq/tmp
保留时间: 24 小时

file: 5 个文件, 2.34 MB
word: 3 个文件, 1.56 MB
download: 2 个文件, 0.89 MB
```

#### 示例2：用户请求清理文件

**用户输入：**
```
帮我清理一下过期的临时文件
```

**Agent 执行流程：**
```
用户请求
    ↓
Agent 理解意图
    ↓
调用 cleanupFiles() 工具
    ↓
触发文件清理任务
    ↓
返回执行结果
```

**返回结果：**
```
文件清理任务已触发，请查看日志了解详情
```

### 特点

- ✅ **自然语言交互**：用户可以用自然语言请求
- ✅ **智能理解**：Agent 自动理解用户意图
- ✅ **即时响应**：立即执行，无需等待定时任务
- ✅ **集成方便**：与对话系统无缝集成

---

#### 3️⃣ API接口触发（手动管理）

### 工作原理

通过 RESTful API 接口，前端或外部系统可以手动触发文件清理。

### 接口信息

**文件：** `src/main/java/com/jieni/mqxq/controller/teacher/ManusTeacherController.java`

#### 接口1：手动触发文件清理

```
POST /api/teacher/manus/cleanup
```

**请求示例：**
```bash
curl -X POST "http://localhost:9999/api/teacher/manus/cleanup" \
  -H "Authorization: Bearer <token>"
```

**响应示例：**
```json
{
  "code": "200",
  "msg": "文件清理任务已触发，请查看日志了解详情",
  "data": null
}
```

#### 接口2：获取文件统计信息

```
GET /api/teacher/manus/file-stats
```

**请求示例：**
```bash
curl -X GET "http://localhost:9999/api/teacher/manus/file-stats" \
  -H "Authorization: Bearer <token>"
```

**响应示例：**
```json
{
  "code": "200",
  "msg": "获取成功",
  "data": {
    "baseDir": "D:\\Desktop\\毕设码趣星球\\mqxq/tmp",
    "retentionHours": 24,
    "directories": [
      {
        "name": "file",
        "fileCount": 5,
        "totalSizeMB": 2.34
      },
      {
        "name": "word",
        "fileCount": 3,
        "totalSizeMB": 1.56
      },
      {
        "name": "download",
        "fileCount": 2,
        "totalSizeMB": 0.89
      }
    ]
  }
}
```

### 权限要求

- **角色要求：** 教师角色（`@SaCheckRole("教师")`）
- **认证方式：** 需要有效的认证 Token

### 代码实现

```java
@Operation(summary = "手动触发文件清理", description = "手动触发清理超过保留时间的临时文件")
@PostMapping("/cleanup")
public Result<String> triggerCleanup() {
    try {
        log.info("手动触发文件清理");
        fileCleanupScheduledTask.manualCleanup();
        return Result.success("文件清理任务已触发，请查看日志了解详情");
    } catch (Exception e) {
        log.error("触发文件清理失败", e);
        return Result.error("触发文件清理失败: " + e.getMessage());
    }
}
```

### 特点

- ✅ **灵活调用**：可以在任何时间手动触发
- ✅ **前端集成**：前端可以添加"清理文件"按钮
- ✅ **外部系统**：其他系统可以通过API调用
- ✅ **权限控制**：需要教师角色权限

---

#### 🔄 三种方式的执行链路

### 定时自动触发
```
@Scheduled 注解
    ↓
cleanupFiles() 方法
    ↓
清理文件
```

### Agent工具触发
```
用户对话请求
    ↓
Agent 调用 cleanupFiles() 工具
    ↓
FileCleanupTool.cleanupFiles()
    ↓
fileCleanupTask.manualCleanup()
    ↓
cleanupFiles() 方法
    ↓
清理文件
```

### API接口触发
```
HTTP POST 请求
    ↓
Controller.triggerCleanup()
    ↓
fileCleanupTask.manualCleanup()
    ↓
cleanupFiles() 方法
    ↓
清理文件
```

**注意：** 无论哪种方式，最终都会调用 `FileCleanupScheduledTask.cleanupFiles()` 方法执行清理。

---

#### 📊 使用场景对比

| 场景 | 推荐方式 | 原因 |
|------|---------|------|
| 日常维护 | 定时自动触发 | 无需人工干预，自动化管理 |
| 用户主动请求 | Agent工具触发 | 自然语言交互，用户体验好 |
| 管理后台 | API接口触发 | 前端集成方便，可控性强 |
| 紧急清理 | API接口触发 | 立即执行，不等待定时任务 |
| 查看统计 | Agent工具或API | 根据使用场景选择 |

---

#### 🛠️ 工具注册机制

### 注册流程

```
应用启动
    ↓
Spring 扫描 @Configuration 类
    ↓
执行 ToolRegistration.allTools() 方法
    ↓
检查 fileCleanupTask 是否可用
    ↓
[可用] → 创建 FileCleanupTool 实例
    ↓
注册到工具列表
    ↓
Agent 可以使用该工具
```

### 关键代码

```java
@Autowired(required = false)
private FileCleanupScheduledTask fileCleanupTask;

@Bean
public ToolCallback[] allTools() {
    // ... 其他工具
    
    // 如果文件清理任务可用，添加文件清理工具
    if (fileCleanupTask != null) {
        FileCleanupTool fileCleanupTool = new FileCleanupTool(fileCleanupTask);
        return ToolCallbacks.from(
            // ... 其他工具
            fileCleanupTool
        );
    }
}
```

**注意：** `@Autowired(required = false)` 表示如果 `FileCleanupScheduledTask` 不存在，不会报错，只是不会注册文件清理工具。

---

#### ⚠️ 注意事项

### 1. 工具可用性

- 只有当 `FileCleanupScheduledTask` Bean 存在时，Agent 才能使用文件清理工具
- 如果 Bean 不存在，Agent 将无法调用相关工具

### 2. 并发执行

- 如果同时触发多次清理（如定时任务和手动触发同时执行），会并发执行
- 建议避免频繁手动触发，以免影响性能

### 3. 权限控制

- API 接口需要教师角色权限
- Agent 工具调用也需要相应的权限（取决于 Agent 的权限设置）

### 4. 日志查看

- 所有触发方式都会记录日志
- 可以通过日志查看清理结果和统计信息

---

#### 📝 总结

文件删除功能提供了**三种灵活的触发方式**：

1. **定时自动触发** - 适合日常维护，自动化管理
2. **Agent工具触发** - 适合用户交互，自然语言操作
3. **API接口触发** - 适合系统集成，手动控制

无论使用哪种方式，最终都会执行相同的清理逻辑，确保一致性。根据实际需求选择合适的方式即可。

