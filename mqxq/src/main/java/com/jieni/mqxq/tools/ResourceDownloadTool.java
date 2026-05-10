package com.jieni.mqxq.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 资源下载工具
 */
public class ResourceDownloadTool {

    /**
     * 文件保存目录
     */
//    String FILE_SAVE_DIR = System.getProperty("user.dir") + "/tmp";
    String FILE_SAVE_DIR = System.getProperty("user.dir") + "\\tmp";

    @Tool(description = "Download file from URL")
    public String downloadResource(@ToolParam(description = "File URL") String url, @ToolParam(description = "File name") String fileName) {
//        String fileDir = FILE_SAVE_DIR + "/download";
        String fileDir = FILE_SAVE_DIR + "\\download";
        String filePath = fileDir + "\\" + fileName;
//        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 使用 Hutool 的 downloadFile 方法下载资源
            HttpUtil.downloadFile(url, new File(filePath));
            return "Resource downloaded successfully to: " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
