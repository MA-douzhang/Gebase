package com.madou.gebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GebaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(GebaseApplication.class, args);
    }

}
