package com.jieni.mqxq.util;

import java.io.*;
import java.util.Base64;
import java.util.UUID;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.jieni.mqxq.common.config.URL;
import com.jieni.mqxq.exception.MyException;

/**
 * 文件处理工具类
 * 
 * 提供文件上传、下载、删除、压缩等完整的文件处理功能
 * 支持Base64转图片、图片压缩、多格式文件上传等特性
 * 实现文件系统的统一管理和存储优化，提供高效的文件操作能力
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Component
public class FUtil {
    
    private static final Logger log = LoggerFactory.getLogger(FUtil.class);
    
    @Resource
    private URL url;

    /**
     * 支持的图片格式
     */
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    
    /**
     * 默认图片压缩大小(200KB)
     */
    private static final int DEFAULT_COMPRESS_SIZE = 200;

    /**
     * Base64转图片并存储到本地
     * @param base64 base64字符串
     * @param path 相对路径
     * @return 存储后的完整文件名
     */
    public String base64ToImg(String base64, String path) throws IOException {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new MyException("Base64字符串不能为空");
        }

        // 处理base64前缀
        String base64Data = base64;
        int commaIndex = base64.indexOf(",");
        if (commaIndex > 0) {
            base64Data = base64.substring(commaIndex + 1);
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        
        // 压缩图片
        imageBytes = PicUtil.compressPicAsByte(imageBytes, DEFAULT_COMPRESS_SIZE);
        
        // 确保目录存在
        String fullPath = ensureDirectoryExists(path);
        
        // 生成文件名
        String fileName = generateFileName("jpg");
        String fullFilePath = fullPath + File.separator + fileName;
        
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(fullFilePath)) {
            fos.write(imageBytes);
        }
        
        log.info("Base64图片保存成功: {}", fullFilePath);
        return path + "/" + fileName;
    }

    /**
     * 上传图片文件并压缩
     * @param multipartFile 上传的文件
     * @param path 相对路径
     * @return 存储后的相对路径
     */
    public String uploadImg(MultipartFile multipartFile, String path) throws IOException {
        return uploadImg(multipartFile, path, DEFAULT_COMPRESS_SIZE);
    }

    /**
     * 上传图片文件并压缩到指定大小
     * @param multipartFile 上传的文件
     * @param path 相对路径
     * @param maxSizeKB 最大文件大小(KB)
     * @return 存储后的相对路径
     */
    public String uploadImg(MultipartFile multipartFile, String path, int maxSizeKB) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new MyException("上传文件不能为空");
        }

        if (!isImageFile(multipartFile.getOriginalFilename())) {
            throw new MyException("不支持的图片格式");
        }

        byte[] bytes = multipartFile.getBytes();
        
        // 压缩图片
        bytes = PicUtil.compressPicAsByte(bytes, maxSizeKB);
        
        // 确保目录存在
        String fullPath = ensureDirectoryExists(path);
        
        // 生成文件名
        String extension = getFileExtension(multipartFile.getOriginalFilename());
        String fileName = generateFileName(extension);
        String fullFilePath = fullPath + File.separator + fileName;
        
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(fullFilePath)) {
            fos.write(bytes);
        }
        
        log.info("图片上传成功: {}, 原始大小={}KB, 压缩后={}KB", 
                fullFilePath, multipartFile.getSize() / 1024, bytes.length / 1024);
        
        return path + "/" + fileName;
    }

    /**
     * 上传用户头像
     * @param multipartFile 头像文件
     * @param userId 用户ID
     * @return 存储后的相对路径
     */
    public String uploadHead(MultipartFile multipartFile, String userId) throws IOException {
        return uploadImg(multipartFile, "user/head/" + userId);
    }

    /**
     * 删除文件或目录
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return false;
        }
        
        String fullPath = url.getImg() + relativePath;
        File file = new File(fullPath);
        
        if (!file.exists()) {
            log.warn("文件不存在: {}", fullPath);
            return false;
        }
        
        boolean deleted = deleteFileRecursively(file);
        if (deleted) {
            log.info("文件删除成功: {}", fullPath);
        } else {
            log.error("文件删除失败: {}", fullPath);
        }
        
        return deleted;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查是否为支持的图片格式
     */
    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String extension = getFileExtension(fileName);
        for (String ext : IMAGE_EXTENSIONS) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    /**
     * 确保目录存在
     */
    private String ensureDirectoryExists(String relativePath) {
        String fullPath = url.getImg() + relativePath;
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return fullPath;
    }

    /**
     * 递归删除文件或目录
     */
    private boolean deleteFileRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFileRecursively(child);
                }
            }
        }
        return file.delete();
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(String relativePath) {
        String fullPath = url.getImg() + relativePath;
        File file = new File(fullPath);
        return file.exists() ? file.length() : 0;
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String relativePath) {
        String fullPath = url.getImg() + relativePath;
        return new File(fullPath).exists();
    }
}
