package com.zybio.clouddesk.interceptor;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.purgeteam.cloud.dispose.starter.exception.category.BusinessException;
import com.zybio.clouddesk.config.JwtConfig;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws BusinessException {

        /** 地址过滤 */
        String uri = request.getRequestURI();
        if (uri.contains("/login")) {
            return true;
        }
        try {
            /** Token 验证 */
            String token = request.getHeader(jwtConfig.getHeader());
            if (StringUtils.isEmpty(token)) {
                token = request.getParameter(jwtConfig.getHeader());
            }
            if (StringUtils.isEmpty(token)) {
                throw new TokenExpiredException(jwtConfig.getHeader() + "不能为空", LocalDateTime.now().toInstant(ZoneOffset.of("+08:00")));
            }
            String bearerToken = token.replace("Bearer", "");


            List<String> manager = List.of(jwtConfig.getManager());


            Claims claims;

            try {
                claims = jwtConfig.getTokenClaim(bearerToken);
                if (claims == null || jwtConfig.isTokenExpired(claims.getExpiration())) {
                    throw new TokenExpiredException(jwtConfig.getHeader() + "失效，请重新登录。", LocalDateTime.now().toInstant(ZoneOffset.of("+08:00")));
                }
            } catch (Exception e) {
                throw new TokenExpiredException(jwtConfig.getHeader() + "失效，请重新登录。", LocalDateTime.now().toInstant(ZoneOffset.of("+08:00")));
            }
            String userName = claims.getSubject();
            request.setAttribute("userName", userName);
            return true;
        } catch (TokenExpiredException e){
            throw new BusinessException("403","token验证失效 "+e.getMessage());
        }
    }
}
