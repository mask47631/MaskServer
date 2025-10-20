package com.mask47631.maskserver.scheduled;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class WebVersionScheduledTask {
    @Schema(description = "自动获取最新前端版本")
    @Value("${app.autoGetWeb:false}")
    private boolean autoGetWeb;
    /**
     * 每天请求GitHub API获取最新的release tag，并写入webversion.log文件
     * 定时任务表达式含义：
     * 秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void fetchLatestWebVersion() {
        if (autoGetWeb){
            latestWebVersion();
        }
    }
    static public void latestWebVersion() {
        try {
            log.info("开始获取最新的Web版本信息");
            RestTemplate restTemplate = new RestTemplate();
            // 请求GitHub API获取最新release信息
            String url = "https://api.github.com/repos/mask47631/MaskWeb/releases/latest";
            String oldVersion = readWebVersion();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("GitHub API响应: {}", responseBody);
                JSONObject json = JSONObject.parse(responseBody);
                String tagName = json.getString("tag_name");
                if (tagName != null && !tagName.isEmpty()) {
                    // 写入文件
                    if(tagName.equals(oldVersion)){
                        log.info("当前版已经是最新的");
                    }else {
                        try {
                            String zipFilePath = "dist.zip";
                            String extractDir = "web";
                            System.out.println("开始下载最新Web版本: " + tagName);
                            downloadFile("https://github.com/mask47631/MaskWeb/releases/download/"+tagName+"/dist.zip",
                                    "dist.zip");
                            System.out.println("下载完成");
                            // 先删除现有的web文件夹
                            deleteDirectory(new File(extractDir));
                            // 解压文件
                            extractZipFile(zipFilePath, extractDir);
                            log.info("dist.zip解压完成到web文件夹");

                            // 删除临时zip文件
                            Files.deleteIfExists(Paths.get(zipFilePath));
                            log.info("临时文件dist.zip已删除");

                            FileWriter writer = new FileWriter("web.version", false);
                            writer.write(tagName);
                            log.info("成功获取并记录Web版本信息: {}", tagName);
                        } catch (IOException e) {
                            log.error("下载Web文件时发生错误", e);
                        }
                    }
                } else {
                    log.error("无法解析tag_name字段");
                }
            } else {
                log.error("请求GitHub API失败，状态码: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("获取Web版本信息时发生错误", e);
        }
    }
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
            log.info("已删除现有web文件夹");
        }
    }
    public static String readWebVersion() {
        try {
            Path path = Paths.get("web.version");
            if (Files.exists(path)) {
                String content = Files.readString(path);
                return content != null ? content.trim() : null;
            } else {
                log.warn("web.version文件不存在");
                return null;
            }
        } catch (IOException e) {
            log.error("读取web.version文件时发生错误", e);
            return null;
        }
    }
    public static void downloadFile(String fileUrl, String destination) throws Exception {
        URL url = new URL(fileUrl);
        InputStream inputStream = url.openStream();
        Files.copy(inputStream, Paths.get(destination));
    }
    /**
     * 解压zip文件
     */
    private static void extractZipFile(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ZipEntry entry = zipIn.getNextEntry();

            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    /**
     * 提取单个文件
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}