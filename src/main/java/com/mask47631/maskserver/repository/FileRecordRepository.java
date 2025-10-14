package com.mask47631.maskserver.repository;

import com.mask47631.maskserver.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    List<FileRecord> findByUploaderId(Long uploaderId);
    
    Optional<FileRecord> findByIdAndRequireLoginFalse(Long id);
}