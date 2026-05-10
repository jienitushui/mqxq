package com.jieni.mqxq.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具
 * 优化版本：只提取有用的文本内容，去除HTML标签、脚本、样式等无用信息
 */
public class WebScrapingTool {

    @Tool(description = "Scrape web page text content")
    public String scrapeWebPage(@ToolParam(description = "Web page URL") String url) {
        try {
            // 连接网页，设置超时和User-Agent
            Document document = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            
            // 移除脚本、样式等无用元素
            document.select("script, style, noscript, iframe, embed, object").remove();
            
            // 提取标题
            StringBuilder content = new StringBuilder();
            String title = document.title();
            if (title != null && !title.trim().isEmpty()) {
                content.append("标题：").append(title).append("\n\n");
            }
            
            // 优先提取主要内容区域（article, main, content等）
            Element mainContent = document.selectFirst("article, main, [role='main'], .content, .main-content, #content, #main");
            if (mainContent != null) {
                String mainText = extractTextContent(mainContent);
                if (mainText != null && !mainText.trim().isEmpty()) {
                    content.append("主要内容：\n").append(mainText).append("\n\n");
                }
            }
            
            // 如果没有找到主要内容区域，尝试提取body中的文本
            if (content.length() == 0 || content.toString().trim().length() < 100) {
                Element body = document.body();
                if (body != null) {
                    String bodyText = extractTextContent(body);
                    if (bodyText != null && !bodyText.trim().isEmpty()) {
                        if (content.length() > 0) {
                            content.append("页面内容：\n");
                        }
                        content.append(bodyText);
                    }
                }
            }
            
            // 如果还是没有内容，返回整个文档的文本
            if (content.length() == 0 || content.toString().trim().length() < 50) {
                String fullText = document.body() != null ? document.body().text() : document.text();
                content.append(fullText);
            }
            
            // 清理和格式化文本
            String result = cleanText(content.toString());
            
            // 限制长度，避免返回过多内容
            if (result.length() > 10000) {
                result = result.substring(0, 10000) + "\n\n[内容已截断，仅显示前10000个字符]";
            }
            
            return result;
            
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
    
    /**
     * 提取元素的文本内容，保留结构
     */
    private String extractTextContent(Element element) {
        if (element == null) {
            return "";
        }
        
        StringBuilder text = new StringBuilder();
        
        // 提取标题
        Elements headings = element.select("h1, h2, h3, h4, h5, h6");
        for (Element heading : headings) {
            String headingText = heading.text().trim();
            if (!headingText.isEmpty()) {
                text.append("\n").append(headingText).append("\n");
            }
        }
        
        // 提取段落
        Elements paragraphs = element.select("p");
        for (Element para : paragraphs) {
            String paraText = para.text().trim();
            if (!paraText.isEmpty() && paraText.length() > 10) { // 过滤太短的段落
                text.append(paraText).append("\n\n");
            }
        }
        
        // 提取列表
        Elements lists = element.select("ul, ol");
        for (Element list : lists) {
            Elements items = list.select("li");
            for (Element item : items) {
                String itemText = item.text().trim();
                if (!itemText.isEmpty()) {
                    text.append("• ").append(itemText).append("\n");
                }
            }
            text.append("\n");
        }
        
        // 如果没有提取到足够的内容，提取所有文本
        if (text.length() < 100) {
            String allText = element.text();
            if (allText != null && !allText.trim().isEmpty()) {
                text.append(allText);
            }
        }
        
        return text.toString();
    }
    
    /**
     * 清理文本内容
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        
        // 移除多余的空白字符
        text = text.replaceAll("\\s+", " ");
        
        // 移除多余的换行
        text = text.replaceAll("\n{3,}", "\n\n");
        
        // 移除特殊字符和不可见字符
        text = text.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "");
        
        // 移除URL（可选，如果需要保留可以注释掉）
        // text = text.replaceAll("https?://[\\S]+", "");
        
        return text.trim();
    }
}
