package com.zybio.clouddesk.controller;

import com.purgeteam.cloud.dispose.starter.exception.category.BusinessException;
import com.zybio.clouddesk.config.JwtConfig;
import com.zybio.clouddesk.enums.RolesEnum;
import com.zybio.clouddesk.ldap.PersonRepo;
import com.zybio.clouddesk.pojo.Person;
import com.zybio.clouddesk.pojo.dto.LoginDTO;
import com.zybio.clouddesk.pojo.form.LoginForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/user")
@RestController
@Slf4j
public class LoginController {


    @Resource
    private JwtConfig jwtConfig;

    @Resource
    private PersonRepo personRepo;

    @PostMapping("/login")
    public String login(@RequestBody LoginForm form){
        String userName = form.getUserName();
        String password = form.getPassword();

        boolean loginAble = personRepo.authenticate(userName,password);
        if (loginAble){
            String roles;
            List<String> manager = List.of(jwtConfig.getManager());
            if (manager.contains(userName)){
                roles = RolesEnum.ADMIN.getValue();
            }else{
                roles = RolesEnum.USER.getValue();
            }
            return jwtConfig.createToken(userName,roles);
        }else{
            throw new BusinessException("403","登录失败，用户名或密码错误");
        }
    }

    @GetMapping("/info")
    public LoginDTO getInfo(HttpServletRequest req){
        LoginDTO loginInfo = new LoginDTO();
        loginInfo.setRole(String.valueOf(req.getAttribute("roles")));
        loginInfo.setUserName(String.valueOf(req.getAttribute("identityId")));
        return loginInfo;
    }

    @GetMapping("/search")
    public String searchUser(@RequestParam String userName){
        List<Person> person = personRepo.searchLdapUser(userName);
        if (person == null || person.isEmpty()){
            return "无该人员，请查证！";
        }else{
            return "确认成功";
        }
    }

    @GetMapping("/test")
    public List<Person> getPersonInfo(@RequestParam String userName){
        List<Person> persons = personRepo.searchLdapUser(userName);
        return persons;
    }
}
