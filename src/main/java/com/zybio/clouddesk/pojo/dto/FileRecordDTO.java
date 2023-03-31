package com.zybio.clouddesk.pojo.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.zybio.clouddesk.enums.Regions;
import lombok.Data;

import java.time.ZonedDateTime;


@Data
public class FileRecordDTO{

    @JSONField
    private String id;
    @JSONField
    private ZonedDateTime created_at;
    @JSONField
    private ZonedDateTime updated_at;
    @JSONField
    private String file_path;
    @JSONField
    private String file_name;
    @JSONField
    private String username;
    @JSONField
    private Integer status;
    @JSONField
    private Regions region;
    @JSONField
    private Integer file_type;
    @JSONField
    private Integer security_level;
    @JSONField
    private String error_message;
    @JSONField
    private long file_size;
}
