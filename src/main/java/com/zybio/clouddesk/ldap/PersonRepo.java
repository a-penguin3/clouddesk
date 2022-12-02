package com.zybio.clouddesk.ldap;

import com.zybio.clouddesk.pojo.Person;

import java.util.List;

public interface PersonRepo {

    /**
     * LDAP用户认证
     */
    boolean authenticate(String loginName, String password);

    /**
     * 检索域用户
     */
    List<Person> searchLdapUser(String keyword);
}
