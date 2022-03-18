package com.wenxia;

import com.wenxia.swift.scan.SwiftRpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhouw
 * @date 2022-03-17
 */
@SpringBootApplication
@SwiftRpcServiceScan(packages = "com.wenxia.facade.service")
public class ClientBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(ClientBootstrap.class, args);
    }
}
