package com.zybio.clouddesk.pojo.form;

import com.zybio.clouddesk.enums.Regions;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class UserDocForm {
    private MultipartFile[] files;
    @NotNull(message = "安全区域不能为空")
    private Regions region;
    @NotNull(message = "安全等级不能为空")
    private Integer securityLevel;
}
