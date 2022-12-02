package com.zybio.clouddesk.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Data
@Entry(objectClasses = {"OrganizationalPerson","Person","top","user"})
public class Person {

    @Id
    @JsonIgnore // 必写
    private Name distinguishedName;

    /* 登录账号 */
    @Attribute(name = "sAMAccountName")
    private String loginName;

    /* 用户姓名 */
    @Attribute(name = "cn")
    private String userName;

    /* 邮箱 */
    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "mobile")
    private String mobile;

    @Attribute(name = "employeeID")
    private String employeeID;
}
