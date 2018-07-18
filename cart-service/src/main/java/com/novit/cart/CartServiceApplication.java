package com.novit.cart;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.novit.cart.domain")
@ImportResource(value = "classpath:applicationContext.xml")
public class CartServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(CartServiceApplication.class,args);
    }
}
