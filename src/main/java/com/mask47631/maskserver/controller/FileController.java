package com.mask47631.maskserver.controller;

import com.mask47631.maskserver.auth.LoginRequired;
import com.mask47631.maskserver.dto.FileRecordDTO;
import com.mask47631.maskserver.entity.FileRecord;
import com.mask47631.maskserver.entity.User;
import com.mask47631.maskserver.repository.FileRecordRepository;
import com.mask47631.maskserver.service.FileService;
import com.mask47631.maskserver.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "文件管理接口", description = "文件上传、下载和管理接口")
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileRecordRepository fileRecordRepository;

    @Autowired
    private FileService fileService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;



    @Operation(summary = "上传文件", description = "上传文件并返回文件链接，默认需要登录才能查看")
    @PostMapping("/upload")
    @LoginRequired
    public ApiResponse<FileRecordDTO> uploadFile(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // 获取用户ID（从session中获取）
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            Long uploaderId = user.getId();
            // 创建用户文件夹
            Path userDir = Paths.get(uploadDir, uploaderId.toString());
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = userDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 保存文件记录到数据库
            FileRecord fileRecord = new FileRecord();
            fileRecord.setOriginalName(originalFilename);
            fileRecord.setStoragePath(filePath.toString());
            fileRecord.setFileUrl("/file/download/" + fileRecord.getId());
            fileRecord.setFileSize(file.getSize());
            fileRecord.setContentType(file.getContentType());
            fileRecord.setRequireLogin(true); // 默认需要登录才能查看
            fileRecord.setUploaderId(uploaderId);
            fileRecord.setCreatedAt(LocalDateTime.now());
            
            FileRecord savedFileRecord = fileRecordRepository.save(fileRecord);
            
            // 更新文件URL
            savedFileRecord.setFileUrl("/file/download/" + savedFileRecord.getId());
            savedFileRecord = fileRecordRepository.save(savedFileRecord);

            // 转换为DTO
            FileRecordDTO dto = new FileRecordDTO();
            dto.setId(savedFileRecord.getId());
            dto.setOriginalName(savedFileRecord.getOriginalName());
            dto.setFileUrl(savedFileRecord.getFileUrl());
            dto.setFileSize(savedFileRecord.getFileSize());
            dto.setContentType(savedFileRecord.getContentType());
            dto.setRequireLogin(savedFileRecord.getRequireLogin());
            dto.setUploaderId(savedFileRecord.getUploaderId());
            dto.setCreatedAt(savedFileRecord.getCreatedAt());

            return ApiResponse.success(dto);
        } catch (Exception e) {
            return ApiResponse.fail("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取文件（需要登录）", description = "根据文件ID获取文件，需要登录")
    @GetMapping("/private/{id}")
    @LoginRequired
    public ResponseEntity<Resource> privateFile(
            @Parameter(description = "文件ID") @PathVariable Long id,
            HttpServletRequest request) throws IOException {
        try {
            FileRecord fileRecord = fileRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));

            // 检查是否需要登录
            if (!fileRecord.getRequireLogin()) {
                // 如果是公开文件，重定向到公开接口
                return ResponseEntity.status(302).location(URI.create("/file/public/" + id)).build();
            }

            Resource resource = fileService.loadFileAsResource(id, false);

            if (resource != null && resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(fileRecord.getContentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + fileRecord.getOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "获取公开文件（无需登录）", description = "根据文件ID获取公开文件，无需登录")
    @GetMapping("/public/{id}")
    public ResponseEntity<Resource> downloadPublicFile(
            @Parameter(description = "文件ID") @PathVariable Long id) throws IOException {
        try {
            FileRecord fileRecord = fileRecordRepository.findByIdAndRequireLoginFalse(id)
                    .orElseThrow(() -> new RuntimeException("文件不存在或不是公开文件"));

            Resource resource = fileService.loadFileAsResource(id, true);

            if (resource != null && resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(fileRecord.getContentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + fileRecord.getOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "修改文件公开属性", description = "修改文件是否需要登录才能查看")
    @PostMapping("/{id}/public")
    public ApiResponse<FileRecordDTO> updateFilePublicStatus(
            @Parameter(description = "文件ID") @PathVariable Long id,
            @Parameter(description = "是否公开") @RequestParam Boolean isPublic,
            HttpServletRequest request) {
        try {
            // 获取用户ID（从session中获取）
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            Long userId = user.getId();
            
            FileRecord fileRecord = fileRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));

            // 检查权限
            if (!fileRecord.getUploaderId().equals(userId)) {
                return ApiResponse.fail("无权限修改该文件");
            }

            // 更新公开状态（requireLogin为false表示公开）
            fileRecord.setRequireLogin(!isPublic);
            FileRecord updatedFileRecord = fileRecordRepository.save(fileRecord);

            // 转换为DTO
            FileRecordDTO dto = new FileRecordDTO();
            dto.setId(updatedFileRecord.getId());
            dto.setOriginalName(updatedFileRecord.getOriginalName());
            dto.setFileUrl(updatedFileRecord.getFileUrl());
            dto.setFileSize(updatedFileRecord.getFileSize());
            dto.setContentType(updatedFileRecord.getContentType());
            dto.setRequireLogin(updatedFileRecord.getRequireLogin());
            dto.setUploaderId(updatedFileRecord.getUploaderId());
            dto.setCreatedAt(updatedFileRecord.getCreatedAt());

            return ApiResponse.success(dto);
        } catch (Exception e) {
            return ApiResponse.fail("更新文件公开状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户上传的文件列表", description = "获取当前用户上传的所有文件")
    @GetMapping("/my-files")
    public ApiResponse<List<FileRecordDTO>> getCurrentUserFiles(HttpServletRequest request) {
        try {
            // 获取用户ID（从session中获取）
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            Long userId = user.getId();
            
            List<FileRecord> fileRecords = fileRecordRepository.findByUploaderId(userId);
            
            List<FileRecordDTO> dtos = fileRecords.stream().map(fileRecord -> {
                FileRecordDTO dto = new FileRecordDTO();
                dto.setId(fileRecord.getId());
                dto.setOriginalName(fileRecord.getOriginalName());
                dto.setFileUrl(fileRecord.getFileUrl());
                dto.setFileSize(fileRecord.getFileSize());
                dto.setContentType(fileRecord.getContentType());
                dto.setRequireLogin(fileRecord.getRequireLogin());
                dto.setUploaderId(fileRecord.getUploaderId());
                dto.setCreatedAt(fileRecord.getCreatedAt());
                return dto;
            }).collect(Collectors.toList());

            return ApiResponse.success(dtos);
        } catch (Exception e) {
            return ApiResponse.fail("获取文件列表失败: " + e.getMessage());
        }
    }
}