package com.example.medigo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MediGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediGoApplication.class, args);
    }

}
