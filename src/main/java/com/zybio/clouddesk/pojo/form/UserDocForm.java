package com.zybio.clouddesk.pojo.form;

import com.zybio.clouddesk.enums.FileOpsType;
import com.zybio.clouddesk.enums.Regions;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class UserDocForm {
    private MultipartFile[] files;
    private Regions region;
    private Integer securityLevel;
    private FileOpsType type;
}
