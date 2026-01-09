package com.llzzhh.study.interactionservice;

import org.mybatis.spring.annotation.MapperScan; // 新增导入
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.llzzhh.study.interactionservice.mapper") // 仅加这一行
public class InteractionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InteractionServiceApplication.class, args);
    }
}