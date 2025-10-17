package com.mask47631.maskserver.controller;

import com.mask47631.maskserver.auth.AdminRequired;
import com.mask47631.maskserver.entity.Config;
import com.mask47631.maskserver.repository.ConfigRepository;
import com.mask47631.maskserver.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "系统配置接口", description = "系统配置项的增删查改，仅管理员可用")
@RestController
@RequestMapping("/config")
@AdminRequired // 整个类只允许管理员访问
public class ConfigController {
    @Autowired
    private ConfigRepository configRepository;

    /**
     * 读取所有配置，返回为json对象（key-value），同时返回key-remark对应关系
     */
    @Operation(summary = "获取所有配置项", description = "返回所有配置项的key-value和key-remark映射")
    @GetMapping("/all")
    public ApiResponse<Map<String, Object>> getAllConfig() {
        List<Config> configs = configRepository.findAll();
        Map<String, String> kv = new HashMap<>();
        Map<String, String> remarkMap = new HashMap<>();
        for (Config c : configs) {
            kv.put(c.getKey(), c.getValue());
            remarkMap.put(c.getKey(), c.getRemark());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", kv);
        result.put("remark", remarkMap);
        return ApiResponse.success(result);
    }

    /**
     * 批量新建或修改配置，接收json对象（key: {value, remark}）
     * {
     *   "key1": { "value": "xxx", "remark": "备注1" },
     *   "key2": { "value": "yyy", "remark": "备注2" }
     * }
     */
    @Operation(summary = "批量保存配置项", description = "批量新建或修改配置，接收json对象（key: {value, remark}）")
    @PostMapping("/save")
    public ApiResponse<Void> saveConfig(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "配置项对象，key为配置名，value为对象{value,remark}")
            @RequestBody List<Config> configs) {
        configRepository.saveAll(configs);
        return ApiResponse.success("配置已保存", null);
    }

    /**
     * 删除指定key的配置
     */
    @Operation(summary = "删除配置项", description = "根据key删除指定配置项")
    @DeleteMapping("/delete/{key}")
    public ApiResponse<Void> deleteConfig(
            @Parameter(description = "配置项key", required = true) @PathVariable("key") String key) {
        if (!configRepository.existsById(key)) {
            return ApiResponse.fail("配置项不存在");
        }
        configRepository.deleteById(key);
        return ApiResponse.success("配置已删除", null);
    }
}
