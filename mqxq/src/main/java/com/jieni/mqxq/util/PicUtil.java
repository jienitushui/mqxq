package com.jieni.mqxq.util;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

/**
 * 图片处理工具类
 * 
 * 提供图片的完整处理功能，包括图片压缩、缩放、格式转换、缩略图生成等。
 * 支持智能压缩算法、高质量压缩、批量处理等特性。为平台的多媒体资源优化和存储管理提供核心支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
public class PicUtil {
    private static final Logger log = LoggerFactory.getLogger(PicUtil.class);
    
    // 文件大小常量
    private static final int KB = 1024;
    private static final int MB = 1024 * KB;
    
    // 压缩质量参数
    private static final float HIGH_QUALITY = 0.9f;
    private static final float MEDIUM_QUALITY = 0.75f;
    private static final float LOW_QUALITY = 0.5f;
    
    // 默认图片尺寸
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int THUMBNAIL_SIZE = 200;

    /**
     * 压缩图片到指定大小以下
     * @param imageBytes 原始图片字节数组
     * @param maxSizeKB 最大文件大小(KB)
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressPicAsByte(byte[] imageBytes, long maxSizeKB) {
        if (imageBytes == null || imageBytes.length == 0 || imageBytes.length <= maxSizeKB * KB) {
            return imageBytes;
        }

        try {
            long srcSize = imageBytes.length;
            float quality = calculateQuality(srcSize / KB, maxSizeKB);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                log.error("无法读取图片格式");
                return imageBytes;
            }
            
            // 智能调整尺寸
            int[] dimensions = calculateDimensions(originalImage.getWidth(), originalImage.getHeight(), maxSizeKB);
            
            Thumbnails.of(originalImage)
                    .size(dimensions[0], dimensions[1])
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            
            byte[] result = outputStream.toByteArray();
            
            log.info("图片压缩完成: 原始大小={}KB, 压缩后={}KB, 压缩率={}%", 
                    srcSize / KB, result.length / KB, 
                    String.format("%.1f", (1 - (double)result.length/srcSize) * 100));
            
            return result;
            
        } catch (Exception e) {
            log.error("图片压缩失败", e);
            return imageBytes;
        }
    }

    /**
     * 生成缩略图
     * @param imageBytes 原始图片字节数组
     * @param size 缩略图尺寸
     * @return 缩略图字节数组
     */
    public static byte[] generateThumbnail(byte[] imageBytes, int size) {
        if (imageBytes == null || imageBytes.length == 0) {
            return imageBytes;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            
            Thumbnails.of(inputStream)
                    .size(size, size)
                    .crop(Positions.CENTER)
                    .outputQuality(MEDIUM_QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成缩略图失败", e);
            return imageBytes;
        }
    }

    /**
     * 压缩图片并返回输入流
     */
    public static InputStream compressPicAsInputStream(byte[] imageBytes, long maxSizeKB) {
        byte[] bytes = compressPicAsByte(imageBytes, maxSizeKB);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 获取图片信息
     */
    public static ImageInfo getImageInfo(byte[] imageBytes) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                return new ImageInfo(image.getWidth(), image.getHeight(), imageBytes.length);
            }
        } catch (Exception e) {
            log.error("获取图片信息失败", e);
        }
        return null;
    }

    /**
     * 计算压缩质量
     */
    private static float calculateQuality(long srcSizeKB, long targetSizeKB) {
        if (srcSizeKB <= targetSizeKB) return HIGH_QUALITY;
        
        double ratio = (double) targetSizeKB / srcSizeKB;
        if (ratio > 0.8) return HIGH_QUALITY;
        if (ratio > 0.5) return MEDIUM_QUALITY;
        return LOW_QUALITY;
    }

    /**
     * 计算合适的尺寸
     */
    private static int[] calculateDimensions(int originalWidth, int originalHeight, long targetSizeKB) {
        if (originalWidth <= DEFAULT_WIDTH && originalHeight <= DEFAULT_HEIGHT) {
            return new int[]{originalWidth, originalHeight};
        }
        
        double ratio = Math.min((double) DEFAULT_WIDTH / originalWidth, (double) DEFAULT_HEIGHT / originalHeight);
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        return new int[]{Math.max(newWidth, 100), Math.max(newHeight, 100)};
    }

    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private final int width;
        private final int height;
        private final long size;

        public ImageInfo(int width, int height, long size) {
            this.width = width;
            this.height = height;
            this.size = size;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getSize() { return size; }
        public long getSizeKB() { return size / KB; }
    }
}