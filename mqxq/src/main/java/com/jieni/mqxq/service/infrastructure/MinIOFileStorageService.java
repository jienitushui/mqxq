package com.jieni.mqxq.service.infrastructure;

import com.jieni.mqxq.common.enums.ContentType;

import java.io.InputStream;
public interface MinIOFileStorageService {

    /**
     * @Description 上传文件，根据文件类型构建出文件夹拼接上文件名
     @Param
     **/
    public String uploadFile(String filename, InputStream inputStream, ContentType contentType);
  
    /** 
     * 删除文件 
     * @param pathUrl  文件全路径 
     */  
    public void delete(String pathUrl);  
  
    /** 
     * 下载文件 
     * @param pathUrl  文件全路径 
     * @return 
     * 
     */  
    public byte[]  downLoadFile(String pathUrl);  

    /**
    * @description 上传压缩文件
    * @param filename 文件名
	* @param bytes 文件字节流
    * @return java.lang.String
    */
    public String uploadCompressImg(String filename,byte[] bytes);
}  
