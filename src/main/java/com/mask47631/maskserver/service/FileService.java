package com.mask47631.maskserver.service;

import com.mask47631.maskserver.entity.FileRecord;
import com.mask47631.maskserver.repository.FileRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private FileRecordRepository fileRecordRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public Resource loadFileAsResource(Long fileId, boolean isPublic) throws IOException {
        Optional<FileRecord> fileRecordOptional;
        
        if (isPublic) {
            // 只能获取公开文件
            fileRecordOptional = fileRecordRepository.findByIdAndRequireLoginFalse(fileId);
        } else {
            // 可以获取所有文件（需要在控制器中检查权限）
            fileRecordOptional = fileRecordRepository.findById(fileId);
        }

        if (fileRecordOptional.isPresent()) {
            FileRecord fileRecord = fileRecordOptional.get();
            Path filePath = Paths.get(fileRecord.getStoragePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            }
        }
        
        return null;
    }
}