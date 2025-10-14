package com.mask47631.maskserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文件记录DTO")
public class FileRecordDTO {
    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "文件原始名称")
    private String originalName;

    @Schema(description = "文件访问链接")
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String contentType;

    @Schema(description = "是否需要登录才能查看")
    private Boolean requireLogin;

    @Schema(description = "上传者用户ID")
    private Long uploaderId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}