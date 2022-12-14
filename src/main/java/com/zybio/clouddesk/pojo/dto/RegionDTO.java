package com.zybio.clouddesk.pojo.dto;

import lombok.Data;

@Data
public class RegionDTO {

    private String regionName;
    private String regionCode;
    private Integer securityLevel;
    private boolean issDefault;
}
