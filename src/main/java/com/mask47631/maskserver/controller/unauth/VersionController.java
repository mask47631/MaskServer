package com.mask47631.maskserver.controller.unauth;

import com.mask47631.maskserver.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "版本接口", description = "获取服务版本号")
@RestController
@RequestMapping("/unauth")
public class VersionController {

    @Schema(description = "服务版本号")
    @Value("${app.version}")
    private String version;

    @Operation(summary = "获取服务版本号", description = "返回当前服务的版本号")
    @GetMapping("/version")
    public ApiResponse<String> getVersion() {
        return ApiResponse.success(version);
    }
}
