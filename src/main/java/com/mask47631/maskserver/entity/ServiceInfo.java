package com.mask47631.maskserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "服务信息实体")
public class ServiceInfo {
    @Schema(description = "服务版本号")
    private String version;
    
    @Schema(description = "服务名称")
    private String name;
    
    @Schema(description = "服务描述")
    private String description;

    @Schema(description = "服务图标")
    private String avatarUrl;

}