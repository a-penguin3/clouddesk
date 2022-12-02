package com.zybio.clouddesk.ldap.impl;

import com.zybio.clouddesk.ldap.PersonRepo;
import com.zybio.clouddesk.pojo.Person;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Component
public class PersonRepoImpl implements PersonRepo {

    private final LdapTemplate ldapTemplate;

    public PersonRepoImpl(LdapTemplate ldapTemplate){
        this.ldapTemplate = ldapTemplate;
    }


    public List<String> getAllPersonNames() {
        return ldapTemplate.search(
                query().where("objectclass").is("person"),
                (AttributesMapper<String>) attrs ->
                        (String) attrs.get("cn").get());
    }

    @Override
    public boolean authenticate(String loginName, String password) {
        EqualsFilter filter = new EqualsFilter("sAMAccountName",loginName);
        return ldapTemplate.authenticate("", filter.toString(), password);
    }

    @Override
    public List<Person> searchLdapUser(String keyword) {
        keyword = "*" + keyword + "*";
        LdapQuery query = query().where("sAMAccountName").like(keyword).or("cn").like(keyword).or("mail").like(keyword);
        return ldapTemplate.find(query, Person.class);
    }
}
