package com.jieni.mqxq.tools;

import cn.hutool.core.io.FileUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

/**
 * Word 生成工具（用于生成教案等文档，方便修改）
 */
public class WordGenerationTool {

    /**
     * 文件保存目录
     */
    String FILE_SAVE_DIR = System.getProperty("user.dir") + File.separator + "tmp";
    
    /**
     * 服务器主机地址（用于生成下载链接）
     */
    private final String serverHost;
    
    /**
     * 服务器端口（用于生成下载链接）
     */
    private final int serverPort;

    /**
     * 构造函数
     * @param serverHost 服务器主机地址
     * @param serverPort 服务器端口
     */
    public WordGenerationTool(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @Tool(description = "Generate Word document (.docx) with text and images", returnDirect = false)
    public String generateWordWithAlternatingTextAndImages(
            @ToolParam(description = "File name (.docx)") String fileName,
            @ToolParam(description = "Text content list") List<String> textContents,
            @ToolParam(description = "Image path list") List<String> imagePaths) {

        // 确保文件名以.docx结尾
        if (!fileName.toLowerCase().endsWith(".docx")) {
            fileName = fileName + ".docx";
        }
        
        // 清理文件名，移除路径分隔符等危险字符
        fileName = sanitizeFileName(fileName);

        String fileDir = FILE_SAVE_DIR + File.separator + "word";
        String filePath = fileDir + File.separator + fileName;

        try {
            // 创建目录
            FileUtil.mkdir(fileDir);

            // 创建Word文档
            try (XWPFDocument document = new XWPFDocument();
                 FileOutputStream out = new FileOutputStream(filePath)) {

                // 获取较大集合的大小，确保所有内容都被处理
                int maxCount = Math.max(textContents.size(), imagePaths.size());

                // 交替添加文字和图片
                for (int i = 0; i < maxCount; i++) {
                    // 添加文字内容（如果存在）
                    if (i < textContents.size() && textContents.get(i) != null && !textContents.get(i).isEmpty()) {
                        // 将 Markdown 格式的文本转换为格式化的 Word 内容
                        convertMarkdownToWord(document, textContents.get(i));
                    }

                    // 添加图片（如果存在）
                    if (i < imagePaths.size() && imagePaths.get(i) != null &&
                            !imagePaths.get(i).isEmpty() && FileUtil.exist(imagePaths.get(i))) {
                        try {
                            XWPFParagraph paragraph = document.createParagraph();
                            XWPFRun run = paragraph.createRun();
                            
                            // 先读取图片尺寸以计算比例
                            File imageFile = new File(imagePaths.get(i));
                            BufferedImage bufferedImage = ImageIO.read(imageFile);
                            if (bufferedImage == null) {
                                throw new IOException("无法读取图片文件: " + imagePaths.get(i));
                            }
                            
                            // 计算图片尺寸
                            int originalWidth = bufferedImage.getWidth();
                            int originalHeight = bufferedImage.getHeight();
                            
                            // 设置目标宽度为500像素，按比例计算高度
                            int targetWidth = 500;
                            int targetHeight = (int) (originalHeight * (targetWidth / (double) originalWidth));
                            
                            // 根据图片路径获取图片类型
                            String imageType = getImageType(imagePaths.get(i));
                            
                            // 转换为EMU单位（POI使用的单位）
                            int width = Units.toEMU(targetWidth);
                            int height = Units.toEMU(targetHeight);
                            
                            // 读取图片输入流并添加到文档
                            try (FileInputStream imageStream = new FileInputStream(imagePaths.get(i))) {
                                // addPicture方法：输入流、图片类型、文件名、宽度、高度
                                run.addPicture(imageStream, getPictureType(imageType), imagePaths.get(i), width, height);
                            }
                        } catch (Exception e) {
                            // 如果图片添加失败，记录错误但继续处理
                            System.err.println("Failed to add image: " + imagePaths.get(i) + ", error: " + e.getMessage());
                        }
                    }
                }

                // 保存文档
                document.write(out);
            }

            // 生成下载链接
            String downloadUrl = String.format("http://%s:%d/api/teacher/manus/download/word/%s", 
                    serverHost, serverPort, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            
            return String.format("Word文档已成功生成！\n文件路径：%s\n下载链接：%s\n\n您可以通过下载链接获取文件，或者直接访问文件路径。", 
                    filePath, downloadUrl);

        } catch (IOException e) {
            return "Error generating Word document: " + e.getMessage();
        }
    }

    /**
     * 根据文件扩展名获取图片类型
     */
    private String getImageType(String imagePath) {
        String lowerPath = imagePath.toLowerCase();
        if (lowerPath.endsWith(".png")) {
            return "png";
        } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "jpg";
        } else if (lowerPath.endsWith(".gif")) {
            return "gif";
        } else if (lowerPath.endsWith(".bmp")) {
            return "bmp";
        }
        return "png"; // 默认返回png
    }

    /**
     * 根据图片类型字符串获取POI的图片类型常量
     */
    private int getPictureType(String imageType) {
        switch (imageType.toLowerCase()) {
            case "png":
                return XWPFDocument.PICTURE_TYPE_PNG;
            case "jpg":
            case "jpeg":
                return XWPFDocument.PICTURE_TYPE_JPEG;
            case "gif":
                return XWPFDocument.PICTURE_TYPE_GIF;
            case "bmp":
                return XWPFDocument.PICTURE_TYPE_BMP;
            default:
                return XWPFDocument.PICTURE_TYPE_PNG;
        }
    }

    /**
     * 将 Markdown 格式的文本转换为格式化的 Word 文档内容
     * 支持Markdown格式和中文标题格式（如"第X周："、"课程目标："等）
     * 
     * @param document Word 文档对象
     * @param markdownText Markdown 格式的文本
     */
    private void convertMarkdownToWord(XWPFDocument document, String markdownText) {
        if (markdownText == null || markdownText.trim().isEmpty()) {
            return;
        }

        // 先处理转义字符，将\n转换为真正的换行
        markdownText = markdownText.replace("\\n", "\n");
        
        String[] lines = markdownText.split("\n");
        boolean inCodeBlock = false;
        boolean inUnorderedList = false;
        boolean inOrderedList = false;

        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // 处理代码块
            if (trimmedLine.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                if (inCodeBlock) {
                    // 代码块开始
                    XWPFParagraph para = document.createParagraph();
                    para.setSpacingAfter(0);
                    XWPFRun run = para.createRun();
                    run.setText("代码块：");
                    run.setFontFamily("宋体");
                    run.setFontSize(11);
                    run.setBold(true);
                } else {
                    // 代码块结束，添加空行
                    document.createParagraph();
                }
                continue;
            }

            if (inCodeBlock) {
                // 在代码块中，使用等宽字体
                // 清理 ANSI 转义序列（如 \033[31m）和其他转义字符
                String cleanedLine = cleanAnsiEscapeCodes(line);
                XWPFParagraph para = document.createParagraph();
                para.setIndentationLeft(400); // 缩进
                para.setSpacingAfter(0);
                XWPFRun run = para.createRun();
                run.setText(cleanedLine);
                run.setFontFamily("Courier New");
                run.setFontSize(10);
                run.setColor("333333");
                continue;
            }

            // 处理Markdown标题（以#开头）
            if (trimmedLine.startsWith("#")) {
                // 结束之前的列表
                if (inUnorderedList || inOrderedList) {
                    inUnorderedList = false;
                    inOrderedList = false;
                }
                
                int level = 0;
                while (level < trimmedLine.length() && trimmedLine.charAt(level) == '#') {
                    level++;
                }
                
                String titleText = trimmedLine.substring(level).trim();
                if (!titleText.isEmpty()) {
                    addTitle(document, titleText, level);
                }
                continue;
            }
            
            // 处理中文标题格式（如"第X周："、"课程目标："、"第X章："等）
            if (isChineseTitle(trimmedLine)) {
                // 结束之前的列表
                if (inUnorderedList || inOrderedList) {
                    inUnorderedList = false;
                    inOrderedList = false;
                }
                
                // 判断标题级别
                int level = getTitleLevel(trimmedLine);
                addTitle(document, trimmedLine, level);
                continue;
            }

            // 处理有序列表
            if (trimmedLine.matches("^\\d+\\.\\s+.*")) {
                if (!inOrderedList) {
                    inOrderedList = true;
                    inUnorderedList = false;
                }
                
                // 提取原始编号和文本内容
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(\\d+)\\.\\s+(.*)").matcher(trimmedLine);
                if (matcher.matches()) {
                    String number = matcher.group(1);
                    String listText = matcher.group(2);
                    
                    // 创建列表项段落
                    XWPFParagraph para = document.createParagraph();
                    para.setIndentationLeft(400);
                    para.setSpacingAfter(0);
                    
                    // 添加编号
                    XWPFRun numberRun = para.createRun();
                    numberRun.setText(number + ". ");
                    numberRun.setFontFamily("宋体");
                    numberRun.setFontSize(12);
                    
                    // 解析并添加格式化的文本内容
                    parseFormattedText(para, listText, false);
                }
                continue;
            }

            // 处理无序列表（支持 - 或 - 开头）
            if (trimmedLine.matches("^[-*+]\\s+.*")) {
                if (!inUnorderedList) {
                    inUnorderedList = true;
                    inOrderedList = false;
                }
                
                String listText = trimmedLine.replaceFirst("^[-*+]\\s+", "");
                
                // 创建列表项段落
                XWPFParagraph para = document.createParagraph();
                para.setIndentationLeft(400);
                para.setSpacingAfter(0);
                
                // 添加列表符号
                XWPFRun bulletRun = para.createRun();
                bulletRun.setText("• ");
                bulletRun.setFontFamily("宋体");
                bulletRun.setFontSize(12);
                
                // 解析并添加格式化的文本内容
                parseFormattedText(para, listText, false);
                continue;
            }

            // 如果之前是列表，现在不是列表项，结束列表
            if (inUnorderedList || inOrderedList) {
                inUnorderedList = false;
                inOrderedList = false;
            }

            // 处理空行
            if (trimmedLine.isEmpty()) {
                // 如果之前有内容，添加适当的间距
                XWPFParagraph para = document.createParagraph();
                para.setSpacingAfter(60);
                continue;
            }

            // 处理普通段落
            // 检查是否是段落开头（前面有空行或者是第一行）
            addFormattedText(document, trimmedLine, false, false);
        }
    }
    
    /**
     * 判断是否是中文标题格式
     * 支持：第X周、第X章、课程目标、课程安排、教学建议等
     */
    private boolean isChineseTitle(String line) {
        if (line == null || line.isEmpty()) {
            return false;
        }
        
        // 匹配中文标题模式
        // 第X周、第X章、第X节等
        if (line.matches("^第[一二三四五六七八九十\\d]+[周章节]：?.*")) {
            return true;
        }
        
        // 课程目标、课程安排、教学建议等（更严格的匹配）
        if (line.matches("^(课程目标|课程安排|教学建议|学习目标|教学目标|课程内容|参考资料|附加建议)：?$")) {
            return true;
        }
        
        // 不再使用宽松的冒号结尾规则，避免误识别普通文本为标题
        return false;
    }
    
    /**
     * 获取标题级别
     * 1级：课程目标、课程安排等
     * 2级：第X周、第X章等
     * 3级：其他标题
     */
    private int getTitleLevel(String line) {
        // 课程目标、课程安排等（与 isChineseTitle 保持一致）
        if (line.matches("^(课程目标|课程安排|教学建议|学习目标|教学目标|课程内容|参考资料|附加建议)：?$")) {
            return 1; // 一级标题
        }
        // 第X周、第X章等
        if (line.matches("^第[一二三四五六七八九十\\d]+[周章节]：?.*")) {
            return 2; // 二级标题
        }
        return 3; // 三级标题
    }
    
    /**
     * 添加标题到文档
     */
    private void addTitle(XWPFDocument document, String titleText, int level) {
        // 移除末尾的冒号（如果有）
        titleText = titleText.replaceAll("：?$", "").trim();
        
        if (titleText.isEmpty()) {
            return;
        }
        
        XWPFParagraph para = document.createParagraph();
        para.setSpacingAfter(level == 1 ? 200 : 100);
        para.setSpacingBefore(level == 1 ? 200 : 100);
        
        XWPFRun run = para.createRun();
        run.setText(titleText);
        run.setFontFamily("宋体");
        run.setBold(true);
        
        // 根据级别设置字体大小
        switch (level) {
            case 1:
                run.setFontSize(18);
                break;
            case 2:
                run.setFontSize(16);
                break;
            case 3:
                run.setFontSize(14);
                break;
            default:
                run.setFontSize(12);
        }
    }

    /**
     * 添加格式化的文本到 Word 文档
     * 支持粗体、斜体等 Markdown 格式
     * 
     * @param document Word 文档对象
     * @param text 文本内容
     * @param isListItem 是否是列表项
     * @param isCodeBlock 是否是代码块
     */
    private void addFormattedText(XWPFDocument document, String text, boolean isListItem, boolean isCodeBlock) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        XWPFParagraph para = document.createParagraph();
        
        if (isListItem) {
            para.setIndentationLeft(400); // 列表项缩进
            para.setSpacingAfter(0);
        } else {
            // 普通段落：设置适当的行间距和段后间距
            para.setSpacingAfter(120); // 增加段后间距，使段落更清晰
            para.setSpacingBetween(1.5); // 设置行间距为1.5倍
        }

        // 解析文本中的格式标记（粗体、斜体等）
        parseFormattedText(para, text, isCodeBlock);
    }

    /**
     * 解析文本中的 Markdown 格式标记并应用到 Word 文档
     * 支持：**粗体**、*斜体*、`代码`
     * 注意：为了减少误识别，只有在明确是格式标记时才应用格式
     */
    private void parseFormattedText(XWPFParagraph para, String text, boolean isCodeBlock) {
        if (isCodeBlock) {
            XWPFRun run = para.createRun();
            run.setText(text);
            run.setFontFamily("Courier New");
            run.setFontSize(10);
            run.setColor("333333");
            return;
        }

        // 如果文本中没有明确的 Markdown 格式标记，直接添加为普通文本
        if (!text.contains("**") && !text.contains("*") && !text.contains("`")) {
            addTextRun(para, text, false, false, false);
            return;
        }

        // 使用简单的字符遍历方法来解析格式标记
        StringBuilder currentText = new StringBuilder();
        boolean inBold = false;
        boolean inItalic = false;
        boolean inCode = false;
        int i = 0;
        
        while (i < text.length()) {
            char c = text.charAt(i);
            
            // 检查粗体标记 **（必须是成对的）
            if (i < text.length() - 1 && text.charAt(i) == '*' && text.charAt(i + 1) == '*') {
                // 输出当前累积的文本
                if (currentText.length() > 0) {
                    addTextRun(para, currentText.toString(), inBold, inItalic, inCode);
                    currentText.setLength(0);
                }
                inBold = !inBold;
                i += 2;
                continue;
            }
            
            // 检查斜体标记 *（不在粗体标记中，且前后不是*）
            if (c == '*' && !inBold && 
                (i == 0 || text.charAt(i - 1) != '*') && 
                (i == text.length() - 1 || text.charAt(i + 1) != '*')) {
                // 输出当前累积的文本
                if (currentText.length() > 0) {
                    addTextRun(para, currentText.toString(), inBold, inItalic, inCode);
                    currentText.setLength(0);
                }
                inItalic = !inItalic;
                i++;
                continue;
            }
            
            // 检查代码标记 `
            if (c == '`') {
                // 输出当前累积的文本
                if (currentText.length() > 0) {
                    addTextRun(para, currentText.toString(), inBold, inItalic, inCode);
                    currentText.setLength(0);
                }
                inCode = !inCode;
                i++;
                continue;
            }
            
            // 普通字符
            currentText.append(c);
            i++;
        }
        
        // 输出剩余的文本
        if (currentText.length() > 0) {
            addTextRun(para, currentText.toString(), inBold, inItalic, inCode);
        }
        
        // 如果没有添加任何内容，添加原始文本（不加粗）
        if (para.getRuns().isEmpty()) {
            addTextRun(para, text, false, false, false);
        }
    }
    
    /**
     * 添加文本运行到段落
     */
    private void addTextRun(XWPFParagraph para, String text, boolean bold, boolean italic, boolean code) {
        if (text.isEmpty()) {
            return;
        }
        
        XWPFRun run = para.createRun();
        run.setText(text);
        
        if (code) {
            run.setFontFamily("Courier New");
            run.setFontSize(11);
            run.setColor("0066CC");
        } else {
            run.setFontFamily("宋体");
            run.setFontSize(12);
        }
        
        run.setBold(bold);
        run.setItalic(italic);
    }
    
    /**
     * 清理 ANSI 转义序列
     * 移除终端颜色控制码（如 \033[31m）和其他 ANSI 转义序列
     * 
     * @param text 包含 ANSI 转义序列的文本
     * @return 清理后的文本
     */
    private String cleanAnsiEscapeCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 移除 ANSI 转义序列
        // ANSI 转义序列格式：ESC[参数m 或 ESC[参数;参数m
        // 在字符串中可能表示为：\033[、\x1b[、\u001b[ 或实际的 ESC 字符
        
        String cleaned = text;
        
        // 移除 \033[数字;数字;...m 格式（最常见）
        // 例如：\033[31m、\033[0m、\033[1;31m
        cleaned = cleaned.replaceAll("\\\\033\\[[0-9;]*m", "");
        
        // 移除 \x1b[数字;数字;...m 格式
        cleaned = cleaned.replaceAll("\\\\x1b\\[[0-9;]*m", "");
        
        // 移除 \u001b[数字;数字;...m 格式
        cleaned = cleaned.replaceAll("\\\\u001b\\[[0-9;]*m", "");
        
        // 移除其他 ANSI 转义序列（不以 m 结尾的）
        cleaned = cleaned.replaceAll("\\\\033\\[[0-9;]*[A-Za-z]", "");
        cleaned = cleaned.replaceAll("\\\\x1b\\[[0-9;]*[A-Za-z]", "");
        cleaned = cleaned.replaceAll("\\\\u001b\\[[0-9;]*[A-Za-z]", "");
        
        // 移除实际的 ESC 字符（ASCII 27）开头的转义序列
        // 这处理了实际运行时可能存在的转义字符
        cleaned = cleaned.replaceAll("\u001b\\[[0-9;]*m", "");
        cleaned = cleaned.replaceAll("\u001b\\[[0-9;]*[A-Za-z]", "");
        
        return cleaned;
    }
    
    /**
     * 清理文件名，移除危险字符
     * 移除路径分隔符、特殊字符等，防止路径遍历攻击
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed.docx";
        }
        
        // 移除路径分隔符和危险字符
        String sanitized = fileName
                .replaceAll("[\\\\/:*?\"<>|]", "_")  // Windows/Linux路径分隔符和特殊字符
                .replaceAll("\\s+", "_")  // 多个空格替换为单个下划线
                .trim();
        
        // 如果清理后为空，使用默认文件名
        if (sanitized.isEmpty()) {
            sanitized = "unnamed.docx";
        }
        
        // 确保以.docx结尾
        if (!sanitized.toLowerCase().endsWith(".docx")) {
            sanitized = sanitized + ".docx";
        }
        
        // 限制文件名长度（不包括扩展名）
        int dotIndex = sanitized.lastIndexOf('.');
        if (dotIndex > 0) {
            String name = sanitized.substring(0, dotIndex);
            String ext = sanitized.substring(dotIndex);
            if (name.length() > 200) {
                sanitized = name.substring(0, 200) + ext;
            }
        } else if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        return sanitized;
    }
}

