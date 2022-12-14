package com.zybio.clouddesk.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private String userName;
    private String role;
}