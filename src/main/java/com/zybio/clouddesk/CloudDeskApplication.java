package com.zybio.clouddesk;

import com.purgeteam.cloud.dispose.starter.annotation.EnableGlobalDispose;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;

@SpringBootApplication
@EnableLdapRepositories
@EnableGlobalDispose
@MapperScan("com.zybio.clouddesk.mapper")
public class CloudDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudDeskApplication.class, args);
    }

}
