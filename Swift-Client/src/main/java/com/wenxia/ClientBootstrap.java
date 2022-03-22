package com.wenxia;

import com.wenxia.facade.service.UserService;
import com.wenxia.swift.scan.SwiftRpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author zhouw
 * @date 2022-03-17
 */
@SpringBootApplication
@SwiftRpcServiceScan(packages = "com.wenxia.facade.service")
public class ClientBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ClientBootstrap.class, args);
        Object o1 = ctx.getBean("userService");
        //Object o2 = ctx.getBean("com.wenxia.facade.service.UserService");
        Object o3 = ctx.getBean(UserService.class);
        System.out.println("o1: " + o1);
        //System.out.println("o2: " + o2);
        System.out.println("o3: " + o3);
    }
}
