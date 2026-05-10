package com.jieni.mqxq.service.impl.infrastructure;

import com.jieni.mqxq.common.config.MinIOConfig;
import com.jieni.mqxq.common.config.MinIOConfigProperties;
import com.jieni.mqxq.common.enums.ContentType;
import com.jieni.mqxq.service.infrastructure.MinIOFileStorageService;
import com.jieni.mqxq.util.PicUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jieni.mqxq.exception.MyException;

/**
 * MinIO文件存储服务实现类
 * 
 * 提供基于MinIO对象存储的完整文件管理功能，包括文件上传、下载、删除等操作。
 * 支持多种文件类型的分类存储、图片压缩、自动文件路径构建等特性。为平台的多媒体资源管理提供核心支持。
 * 
 * @author jieni
 * @version 1.0
 * @since 2025
 */

@Slf4j
@EnableConfigurationProperties(MinIOConfigProperties.class)
@Import(MinIOConfig.class)
@Service
public class MinIOFileStorageServiceImpl implements MinIOFileStorageService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinIOConfigProperties minIOConfigProperties;

    private final static String SEPARATOR = "/";

    /**
    * @description 根据文件类型构建出文件存储在minio的相对位置
    * @param contentType 文件类型
	* @param filename 文件名
    * @return java.lang.String
    */
    private String builderFilePath(ContentType contentType, String filename) {
        return contentType.getPrefix() + SEPARATOR + filename;
    }

    /**
    * @description 获取真实的基础路径
    * @return java.lang.String
    */
    private String getBasisUrl() {
        return minIOConfigProperties.getEndpoint() + SEPARATOR + minIOConfigProperties.getBucket() + SEPARATOR;
    }

    /**
     * @Description 上传文件，根据文件类型构建出文件夹拼接上文件名
     * 如传参 【filename】:"1.png"；【contentType】：ContentType.PNG
     * 构建目录：/png/1.png
     * @Param
     **/
    @Override
    public String uploadFile(String filename, InputStream inputStream, ContentType contentType) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new MyException("文件名不能为空");
        }
        if (inputStream == null) {
            throw new MyException("文件流不能为空");
        }
        if (contentType == null) {
            throw new MyException("文件类型不能为空");
        }
        
        String filePath = builderFilePath(contentType, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType(contentType.getType())
                    .bucket(minIOConfigProperties.getBucket()).stream(inputStream, inputStream.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
            return this.getBasisUrl() + filePath;
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            throw new MyException("上传文件失败: " + ex.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param pathUrl 文件全路径
     */
    @Override
    public void delete(String pathUrl) {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint() + "/", "");
        int index = key.indexOf(SEPARATOR);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        // 删除Objects  
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  pathUrl:{}", pathUrl);
            e.printStackTrace();
        }
    }


    /**
     * 下载文件
     * @param pathUrl 文件全路径
     * @return 文件流
     */

    @Override
    public byte[] downLoadFile(String pathUrl) {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint() + "/", "");
        int index = key.indexOf(SEPARATOR);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(minIOConfigProperties.getBucket()).object(filePath).build());
        } catch (Exception e) {
            log.error("minio down file error.  pathUrl:{}", pathUrl);
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (!((rc = inputStream.read(buff, 0, 100)) > 0)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 上传压缩后的图片文件
     * 
     * 将图片字节数组进行压缩处理后上传到MinIO存储
     * 用于缩略图、头像处理、降低存储成本等场景
     * 
     * @param filename 图片文件名，不能为空
     * @param bytes 原始图片的字节数组，不能为空
     * @return String 压缩图片的完整访问链接
     * @throws MyException 当参数无效或上传失败时抛出
     */
    @Override
    public String uploadCompressImg(String filename, byte[] bytes) {
        // 参数验证
        if (filename == null || filename.trim().isEmpty()) {
            throw new MyException("图片文件名不能为空");
        }
        if (bytes == null || bytes.length == 0) {
            throw new MyException("图片数据不能为空");
        }
        
        try {
            InputStream inputStream = PicUtil.compressPicAsInputStream(bytes, 200);
            return this.uploadFile(filename, inputStream, ContentType.PNG);
        } catch (Exception e) {
            log.error("图片压缩上传失败，文件名: {}, 错误信息: {}", filename, e.getMessage(), e);
            throw new MyException("图片压缩上传失败: " + e.getMessage());
        }
    }
}  
