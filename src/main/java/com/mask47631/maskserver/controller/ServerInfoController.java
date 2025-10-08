package com.mask47631.maskserver.controller;

import com.mask47631.maskserver.auth.AdminRequired;
import com.mask47631.maskserver.util.ApiResponse;
import com.mask47631.maskserver.entity.ServerInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * 需要admin权限才能访问
 */
@RestController
@RequestMapping("/server")
@AdminRequired
@Tag(name = "获取服务器CPU、内存等信息", description = "获取服务器CPU、内存等信息，仅管理员可用")
public class ServerInfoController {
    /**
     * 获取服务器CPU、内存等信息（仅admin可访问）
     */
    @Operation(
        summary = "获取服务器信息",
        description = "获取服务器CPU、内存等信息，仅admin可访问",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "成功返回服务器信息",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ServerInfo.class)
                )
            )
        }
    )
    @GetMapping("/info")
    public ApiResponse<ServerInfo> getServerInfo(
            @Parameter(hidden = true) HttpSession session) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ServerInfo info = new ServerInfo();
        info.setOsName(osBean.getName());
        info.setOsArch(osBean.getArch());
        info.setOsVersion(osBean.getVersion());
        info.setAvailableProcessors(osBean.getAvailableProcessors());
        try {
            double cpuLoad = (double) osBean.getClass().getMethod("getSystemCpuLoad").invoke(osBean);
            info.setCpuLoad(cpuLoad);
        } catch (Exception ignored) {}
        info.setFreeMemory(Runtime.getRuntime().freeMemory());
        info.setTotalMemory(Runtime.getRuntime().totalMemory());
        info.setMaxMemory(Runtime.getRuntime().maxMemory());
        return ApiResponse.success(info);
    }
}
