package com.zybio.clouddesk.pojo.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.zybio.clouddesk.enums.Regions;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@TableName("bd_file_record")
public class BdFileRecord {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private ZonedDateTime created_at;
    private ZonedDateTime updated_at;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private String file_path;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private String file_name;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private String username;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private Integer status;
    private Integer security_level;
    private Regions region;
    private String error_message;
}
