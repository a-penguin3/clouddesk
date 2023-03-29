package com.zybio.clouddesk.controller;

import com.purgeteam.cloud.dispose.starter.annotation.IgnoreResponseAdvice;
import com.purgeteam.cloud.dispose.starter.exception.category.BusinessException;
import com.zybio.clouddesk.kafka.producer.SendMessage;
import com.zybio.clouddesk.pojo.domain.BdFileRecord;
import com.zybio.clouddesk.pojo.dto.FileRecordDTO;
import com.zybio.clouddesk.pojo.dto.PageDTO;
import com.zybio.clouddesk.pojo.dto.RegionDTO;
import com.zybio.clouddesk.pojo.form.FileRecordForm;
import com.zybio.clouddesk.pojo.form.UserDocForm;
import com.zybio.clouddesk.service.UserDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RequestMapping("/doc")
@RestController
public class UserDocController {
    @Autowired
    private UserDocService userDocService;

    @PostMapping("/sendFile")
    public List<BdFileRecord> sendFiles(@Validated UserDocForm form, HttpServletRequest request) {
        if (form.getFiles() == null || form.getFiles().length == 0) {
            throw new BusinessException("500", "上传文件为空");
        }
        String userName = String.valueOf(request.getAttribute("userName"));
        return userDocService.sendFile(form.getFiles(), userName, form.getType());
    }

    @IgnoreResponseAdvice
    @PostMapping("/downloadFile")
    public ResponseEntity<Resource> download(@Valid @RequestBody FileRecordForm form, HttpServletRequest request) {
        return userDocService.downloadFile(form, String.valueOf(request.getAttribute("userName")));
    }

    @GetMapping("/region")
    public List<RegionDTO> getRegion() {
        return userDocService.getRegion();
    }

    @GetMapping("/getRecord")
    public PageDTO<BdFileRecord> getRecords(
            HttpServletRequest request,
            @RequestParam int pages,
            @RequestParam int pageSize,
            @Nullable @RequestParam Integer status
    ){
        String username = String.valueOf(request.getAttribute("userName"));
        return userDocService.getRecords(username,pages,pageSize,status);
    }

//    @GetMapping("/test")
//    public String test(@RequestParam String message, HttpServletRequest request){
//        send.sendMessage(message,String.valueOf(request.getAttribute("userName")),"encryptedFiles");
//        return "访问成功";
//    }
}
