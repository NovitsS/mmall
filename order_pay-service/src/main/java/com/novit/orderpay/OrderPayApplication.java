package com.novit.orderpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class OrderPayApplication {
    public static void main(String[]  args){
        SpringApplication.run(OrderPayApplication.class, args);
    }
}
