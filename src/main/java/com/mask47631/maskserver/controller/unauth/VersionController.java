package com.mask47631.maskserver.controller.unauth;

import com.mask47631.maskserver.entity.Config;
import com.mask47631.maskserver.entity.ServiceInfo;
import com.mask47631.maskserver.repository.ConfigRepository;
import com.mask47631.maskserver.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "版本接口", description = "获取服务信息")
@RestController
@RequestMapping("/unauth")
public class VersionController {

    @Schema(description = "服务版本号")
    @Value("${app.version}")
    private String version;

    @Schema(description = "服务名称")
    @Value("${app.name}")
    private String name;

    @Schema(description = "服务描述")
    @Value("${app.description}")
    private String description;

    @Schema(description = "服务图标")
    @Value("${app.avatarUrl}")
    private String avatarUrl;

    @Autowired
    private ConfigRepository configRepository;

    @Operation(summary = "获取服务信息", description = "返回服务版本号、服务名称和服务描述")
    @GetMapping("/version")
    public ApiResponse<ServiceInfo> getVersion() {
        List<Config> configs = configRepository.findAllById(List.of("app.name", "app.description", "app.avatarUrl"));
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setVersion(version);
        serviceInfo.setName(name);
        serviceInfo.setDescription(description);
        serviceInfo.setAvatarUrl(avatarUrl);
        for (Config config : configs){
            switch (config.getKey()){
                case "app.name":
                    serviceInfo.setName(config.getValue());
                    break;
                case "app.description":
                    serviceInfo.setDescription(config.getValue());
                    break;
                case "app.avatarUrl":
                    serviceInfo.setAvatarUrl(config.getValue());
                    break;
            }
        }
        return ApiResponse.success(serviceInfo);
    }
}