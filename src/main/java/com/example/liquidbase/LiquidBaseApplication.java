package com.example.liquidbase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

//@EnableKafka
@SpringBootApplication
public class LiquidBaseApplication {
    public static void main(String[] args) {

        SpringApplication.run(LiquidBaseApplication.class, args);
    }
}
