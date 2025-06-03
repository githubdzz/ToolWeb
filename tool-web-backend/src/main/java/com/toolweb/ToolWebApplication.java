package com.toolweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.toolweb")
public class ToolWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolWebApplication.class, args);
    }
} 