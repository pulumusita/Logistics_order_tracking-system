package com.logistics.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@EnableScheduling
public class LogisticsTrackingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogisticsTrackingApplication.class, args);
    }
} 