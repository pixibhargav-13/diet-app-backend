package com.dietapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.dietapp")
@EnableJpaAuditing
public class DietAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DietAppApplication.class, args);
    }
}
