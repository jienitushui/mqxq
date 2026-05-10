package com.jieni.mqxq.controller.publicapi;


import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jieni.mqxq.common.enums.ContentType;
import com.jieni.mqxq.common.Result;
import com.jieni.mqxq.domain.entity.User;
import com.jieni.mqxq.service.infrastructure.MinIOFileStorageService;
import com.jieni.mqxq.service.auth.UserService;
import com.jieni.mqxq.util.SaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * 文件管理控制器
 * <p>
 * 提供文件上传、删除等完整的文件管理功能，支持图片、视频、文档等多种文件类型。
 * 集成MinIO对象存储，支持智能文件类型识别、用户头像管理等特性。为平台的媒体资源管理提供核心支持。
 *
 * @author jieni
 * @version 1.0
 * @since 2025
 */
@SaCheckLogin
@RestController
@Validated
@RequestMapping("/api/public/files")
@Slf4j
@Tag(name = "文件管理", description = "文件上传相关接口，包括图片和视频上传")
public class FileController {

    @Resource
    private MinIOFileStorageService minIOFileStorageService;

    @Resource
    private UserService userService;

    @Value("${noFoundPath}")
    private String noFoundPath;

    /**
     * 图片文件上传
     * 支持PNG、JPG、JPEG等常见图片格式
     */
    @PostMapping("/image")
    @Operation(
            summary = "上传图片文件",
            description = "上传图片文件到MinIO存储，支持常见图片格式，返回文件访问URL"
    )
    @Parameter(
            name = "file",
            description = "要上传的图片文件",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "上传成功",
                    content = @Content(schema = @Schema(description = "文件访问URL"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数错误或文件为空"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户未登录"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误"
            )
    })
    public String saveImage(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        //1 检查参数
        if (Objects.isNull(multipartFile) || Objects.equals(multipartFile.getSize(), 0)) {
            return "参数错误";
        }
        //2 上传图片到 minio

        String fileName = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
//        log.info("上传图片文件{}", fileName + substring);
        return minIOFileStorageService.uploadFile(fileName + substring, multipartFile.getInputStream(), ContentType.PNG);
    }

    /**
     * 视频文件上传
     * 支持MP4、AVI、MOV等常见视频格式
     */
    @PostMapping("/video")
    @Operation(
            summary = "上传视频文件",
            description = "上传视频文件到MinIO存储，支持常见视频格式，返回文件访问URL"
    )
    @Parameter(
            name = "file",
            description = "要上传的视频文件",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "上传成功",
                    content = @Content(schema = @Schema(description = "文件访问URL"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数错误或文件为空"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户未登录"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误"
            )
    })
    public String saveVideo(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        //1 检查参数
        if (Objects.isNull(multipartFile) || Objects.equals(multipartFile.getSize(), 0)) {
            return "参数错误";
        }
        //2 上传视频到 minio
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        return minIOFileStorageService.uploadFile(fileName + substring, multipartFile.getInputStream(), ContentType.MP4);
    }

    /**
     * 删除文件
     * 根据文件URL删除MinIO中的文件
     */
    @DeleteMapping("/{pathUrl}")
    @Operation(
            summary = "删除文件",
            description = "根据文件URL删除MinIO存储中的文件"
    )
    @Parameter(
            name = "pathUrl",
            description = "要删除的文件URL路径",
            required = true,
            example = "http://minio-server/bucket/folder/file.jpg"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "删除成功"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数错误"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户未登录"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "文件不存在"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误"
            )
    })
    public String deleteFile(@PathVariable("pathUrl") String pathUrl) {
        // 检查参数
        if (Objects.isNull(pathUrl) || pathUrl.trim().isEmpty()) {
            return "参数错误：文件URL不能为空";
        }


        minIOFileStorageService.delete(pathUrl);
        return "文件删除成功";

    }

    /**
     * 删除文件（通过请求参数方式）
     * 根据文件URL删除MinIO中的文件，支持通过请求参数传递URL
     */
    @DeleteMapping("/delete")
    @Operation(
            summary = "删除文件（参数方式）",
            description = "根据文件URL删除MinIO存储中的文件，通过请求参数传递URL"
    )
    @Parameter(
            name = "pathUrl",
            description = "要删除的文件URL路径",
            required = true,
            example = "http://minio-server/bucket/folder/file.jpg"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "删除成功"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数错误"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户未登录"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "文件不存在"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误"
            )
    })
    public String deleteFileByParam(@RequestParam("pathUrl") String pathUrl) {
        // 检查参数
        if (Objects.isNull(pathUrl) || pathUrl.trim().isEmpty()) {
            return "参数错误：文件URL不能为空";
        }


        minIOFileStorageService.delete(pathUrl);
        return "文件删除成功";

    }

    /**
     * 通用文件上传
     * 支持任意文件格式，使用默认的application/octet-stream类型
     */
    @PostMapping("/upload")
    @Operation(
            summary = "通用文件上传",
            description = "上传任意格式文件到MinIO存储，使用默认的application/octet-stream类型，返回文件访问URL"
    )
    @Parameter(
            name = "file",
            description = "要上传的文件",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "上传成功",
                    content = @Content(schema = @Schema(description = "文件访问URL"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数错误或文件为空"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户未登录"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误"
            )
    })
    public String saveFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        // 检查参数
        if (Objects.isNull(multipartFile) || Objects.equals(multipartFile.getSize(), 0)) {
            return "参数错误";
        }
        // 上传文件到minio，使用默认类型
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        return minIOFileStorageService.uploadFile(fileName + substring, multipartFile.getInputStream(), ContentType.DEFAULT);
    }

    /**
     * 根据文件类型自动选择上传方式
     * 根据文件扩展名自动判断文件类型并使用相应的ContentType
     */
    @PostMapping("/auto")
    @Operation(
            summary = "智能文件上传",
            description = "根据文件扩展名自动判断类型并上传文件到MinIO存储，支持图片、视频、文档等常见格式"
    )
    @Parameter(
            name = "file",
            description = "要上传的文件",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "上传成功",
                    content = @Content(schema = @Schema(description = "文件访问URL"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数错误或文件为空"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户未登录"
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "不支持的文件类型"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误"
            )
    })
    public String saveFileAuto(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        // 检查参数
        if (Objects.isNull(multipartFile) || Objects.equals(multipartFile.getSize(), 0)) {
            return "参数错误";
        }

        // 获取文件扩展名
        String originalFilename = multipartFile.getOriginalFilename();
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // 根据扩展名选择ContentType
        ContentType contentType;
        switch (fileExt) {
            case ".png":
                contentType = ContentType.PNG;
                break;
            case ".jpg":
            case ".jpeg":
                contentType = ContentType.JPG;
                break;
            case ".gif":
                contentType = ContentType.GIF;
                break;
            case ".mp4":
                contentType = ContentType.MP4;
                break;
            case ".ppt":
                contentType = ContentType.PPT;
                break;
            case ".pdf":
                contentType = ContentType.PDF;
                break;
            case ".txt":
                contentType = ContentType.TXT;
                break;
            case ".doc":
            case ".docx":
                contentType = ContentType.WORD;
                break;
            case ".xls":
            case ".xlsx":
                contentType = ContentType.EXCEL;
                break;
            default:
                contentType = ContentType.DEFAULT;
                break;
        }

        // 上传文件到minio
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExt;
        return minIOFileStorageService.uploadFile(fileName, multipartFile.getInputStream(), contentType);
    }

    /**
     * 更新用户头像
     * 先删除旧头像，再上传新头像
     */
    @PostMapping("/avatar")
    @Operation(
            summary = "更新用户头像",
            description = "上传新头像并更新用户信息，如果存在旧头像，则会先删除旧头像"
    )
    @Parameter(
            name = "file",
            description = "要上传的头像图片文件",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "头像更新成功",
                    content = @Content(schema = @Schema(description = "新头像的访问URL"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "参数错误或文件为空"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户未登录"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误"
            )
    })
    public Result updateAvatar(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        // 1. 检查文件是否为空
        if (multipartFile.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 2. 获取当前登录用户
        log.info("当前登录用户ID: {}", SaUtil.getLoginId());
        Integer userId = SaUtil.getLoginId();
        User user = userService.queryById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

//            log.info("当前用户头像: {}", user.getAvatar());
        // 3. 如果存在旧头像，则删除,http://localhost:9000/mqxq/png/404.png不能是这个链接
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            if (!user.getAvatar().equals(noFoundPath)) {
                minIOFileStorageService.delete(user.getAvatar());
            }
        }

        // 4. 上传新头像
        String newAvatarUrl = minIOFileStorageService.uploadFile(
                generateAvatarFileName(multipartFile.getOriginalFilename()),
                multipartFile.getInputStream(),
                ContentType.PNG // 假设头像是PNG格式
        );

//            // 5. 更新用户信息
//            user.setAvatar(newAvatarUrl);
//            userService.update(user);

        return Result.success("头像更新成功",newAvatarUrl);

    }

    /**
     * 生成头像文件名
     */
    private String generateAvatarFileName(String originalFilename) {
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
        return "avatars/" + UUID.randomUUID().toString().replace("-", "") + fileExt;
    }
}