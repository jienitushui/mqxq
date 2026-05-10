package com.jieni.mqxq.common.enums;

import lombok.Getter;

/**
 * 文件内容类型枚举
 * 定义MinIO上传文件时的MIME类型映射，支持各种图片、文档、视频格式
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@Getter
public enum ContentType {
    /** 默认类型 - 通用二进制流 */
    DEFAULT("default","application/octet-stream"),
    
    /** 图片格式 */
    JPG("jpg", "image/jpeg"),
    TIFF("tiff", "image/tiff"),
    GIF("gif", "image/gif"),
    JFIF("jfif", "image/jpeg"),
    PNG("png", "image/png"),
    TIF("tif", "image/tiff"),
    ICO("ico", "image/x-icon"),
    JPEG("jpeg", "image/jpeg"),
    WBMP("wbmp", "image/vnd.wap.wbmp"),
    FAX("fax", "image/fax"),
    NET("net", "image/pnetvue"),
    JPE("jpe", "image/jpeg"),
    RP("rp", "image/vnd.rn-realpix"),
    
    /** 应用程序文件格式 */
    APK("apk","application/octet-stream"),
    EXCEL("excel","application/vnd.ms-excel"),
    PDF("pdf","application/pdf"),
    WORD("word","application/msword"),
    PPT("ppt","application/vnd.ms-powerpoint"),
    
    /** 文本格式 */
    TXT("txt","text/plain"),
    
    /** 视频格式 */
    MP4("mp4", "video/mp4");

    /** 文件前缀（用于指定上传文件的文件夹） */
    private final String prefix;
    
    /** 文件MIME类型，用于设置MinIO上传时的contentType */
    private final String type;

    /**
     * 构造函数
     * 
     * @param prefix 文件前缀
     * @param type MIME类型
     */
    private ContentType(String prefix, String type) {
        this.prefix = prefix;
        this.type = type;
    }

    /**
     * 判断文件后缀是否匹配指定的内容类型
     * 
     * @param contentType 内容类型枚举
     * @param suffix 文件后缀
     * @return boolean 是否匹配
     */
    public boolean judgeSuffix(ContentType contentType, String suffix){
        return contentType.getType().equals(suffix);
    }
}
