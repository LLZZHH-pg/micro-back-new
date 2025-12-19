package com.llzzhh.study.squareservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SquareServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SquareServiceApplication.class, args);
    }

}
