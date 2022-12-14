package com.zybio.clouddesk.pojo.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@TableName("user_document_tree")
public class UserDocumentTree {

    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField(fill = FieldFill.INSERT)
    private ZonedDateTime created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private ZonedDateTime updated;
    private Integer level;
    private Integer parent;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private String name;
    @TableField(insertStrategy = FieldStrategy.NOT_NULL)
    private String path;
}
