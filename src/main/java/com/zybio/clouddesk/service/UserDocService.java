package com.zybio.clouddesk.service;

import com.zybio.clouddesk.enums.FileOpsType;
import com.zybio.clouddesk.pojo.domain.BdFileRecord;
import com.zybio.clouddesk.pojo.dto.FileRecordDTO;
import com.zybio.clouddesk.pojo.dto.PageDTO;
import com.zybio.clouddesk.pojo.dto.RegionDTO;
import com.zybio.clouddesk.pojo.form.FileRecordForm;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserDocService {

//    String decodeFiles(MultipartFile[] files, String userName);
//
//    String encodeFiles(MultipartFile[] files, String userName, Regions regin, Integer securityLevel);

    void encodeFiles(FileRecordDTO dto);
    void decodeFiles(FileRecordDTO dto);
    List<BdFileRecord> sendFile(MultipartFile[] files, String userName, FileOpsType optionType);

    ResponseEntity<Resource> downloadFile(FileRecordForm form, String username);

    PageDTO<BdFileRecord> getRecords(String username, int pages, int pageSize, Integer status);

    List<RegionDTO> getRegion();
}
