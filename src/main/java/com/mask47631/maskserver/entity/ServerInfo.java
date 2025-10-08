package com.mask47631.maskserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "服务器信息实体")
public class ServerInfo {
    @Schema(description = "操作系统名称")
    private String osName;
    @Schema(description = "操作系统架构")
    private String osArch;
    @Schema(description = "操作系统版本")
    private String osVersion;
    @Schema(description = "可用处理器数")
    private int availableProcessors;
    @Schema(description = "CPU负载")
    private Double cpuLoad;
    @Schema(description = "空闲内存")
    private long freeMemory;
    @Schema(description = "总内存")
    private long totalMemory;
    @Schema(description = "最大内存")
    private long maxMemory;
}
