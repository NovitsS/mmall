package com.novit.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ImportResource;


@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.novit.user.domain.repository")
@ImportResource(value ="classpath:applicationContext.xml")
public class UserServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(UserServiceApplication.class,args);
    }
}
