package com.zybio.clouddesk.service;

import com.zybio.clouddesk.enums.Regions;
import com.zybio.clouddesk.pojo.dto.RegionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserDocService {

    String decodeFiles(MultipartFile[] files, String userName);

    String encodeFiles(MultipartFile[] files, String userName, Regions regin, Integer securityLevel);

    List<RegionDTO> getRegion();
}
