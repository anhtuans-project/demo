package com.example.liquidbase;

import com.example.liquidbase.service.OwnerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiquidBaseApplication implements CommandLineRunner {

    private final OwnerService ownerService;

    public LiquidBaseApplication(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    public static void main(String[] args) {
        SpringApplication.run(LiquidBaseApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Owner: ");
        ownerService.findAll().forEach(owner -> {
            System.out.printf("- ID: %s | Name: %s", owner.getId(), owner.getName());
        });
    }
}
