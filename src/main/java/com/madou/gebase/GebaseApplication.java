package com.madou.gebase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.madou.gebase.mapper")
@EnableScheduling
public class GebaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(GebaseApplication.class, args);
    }

}
