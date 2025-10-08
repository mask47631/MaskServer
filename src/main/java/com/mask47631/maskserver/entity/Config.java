package com.mask47631.maskserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Comment;

@Data
@Entity
@Table(name = "config")
@Comment("系统配置表")
@Schema(description = "系统配置实体")
public class Config {
    @Id
    @Column(name = "config_key", length = 128, nullable = false, unique = true)
    @Comment("配置项key，主键")
    @Schema(description = "配置项key，主键")
    private String key;

    @Column(name = "config_value", length = 1024)
    @Comment("配置项value")
    @Schema(description = "配置项value")
    private String value;

    @Column(name = "remark", length = 255)
    @Comment("备注")
    @Schema(description = "备注")
    private String remark;
}
