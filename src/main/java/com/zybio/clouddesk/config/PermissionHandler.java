package com.zybio.clouddesk.config;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.purgeteam.cloud.dispose.starter.exception.category.BusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PermissionHandler {
    @ExceptionHandler(value = {TokenExpiredException.class})
    @ResponseBody
    public Map<String, Object> authorizationException(TokenExpiredException e) {
        throw new BusinessException("403",e.getMessage());
    }
}
