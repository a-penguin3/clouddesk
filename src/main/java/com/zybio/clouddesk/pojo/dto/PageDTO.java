package com.zybio.clouddesk.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO<T> {
    private List<T> data;
    private Integer pageSize;
    private Integer pages;
    private Long totalCount;

}
