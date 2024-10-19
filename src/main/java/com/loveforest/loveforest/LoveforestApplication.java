package com.loveforest.loveforest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LoveforestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoveforestApplication.class, args);
    }

}
