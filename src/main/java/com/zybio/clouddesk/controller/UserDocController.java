package com.zybio.clouddesk.controller;

import com.purgeteam.cloud.dispose.starter.exception.category.BusinessException;
import com.zybio.clouddesk.pojo.dto.RegionDTO;
import com.zybio.clouddesk.pojo.form.UserDocForm;
import com.zybio.clouddesk.service.UserDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/doc")
@RestController
public class UserDocController {
    @Autowired
    private UserDocService userDocService;

    @PostMapping("/upload")
    public String sendDoc(MultipartFile[] files, HttpServletRequest request){
        if (files == null || files.length == 0){
            throw new BusinessException("500","上传文件为空");
        }
        String userName = String.valueOf(request.getAttribute("userName"));
        return userDocService.decodeFiles(files,userName);
    }

    @PostMapping("/encode")
    public String encodeDoc(@Validated UserDocForm form, HttpServletRequest request){
        if (form.getFiles() == null || form.getFiles().length == 0){
            throw new BusinessException("500","上传文件为空");
        }
        String userName = String.valueOf(request.getAttribute("userName"));
        return userDocService.encodeFiles(form.getFiles(),userName,form.getRegion(),form.getSecurityLevel());
    }

    @GetMapping("/region")
    public List<RegionDTO> getRegion(){
        return userDocService.getRegion();
    }
}
