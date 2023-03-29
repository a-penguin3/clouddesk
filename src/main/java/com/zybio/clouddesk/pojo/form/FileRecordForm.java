package com.zybio.clouddesk.pojo.form;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FileRecordForm {
    private String id;
    @NotNull(message = "文件路径不能为空")
    private String file_path;
    private String file_name;
    @NotNull(message = "用户名不能为空")
    private String username;
    private Integer status;
}
