package com.bosshi.maeul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaeulApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaeulApplication.class, args);
    }

}
