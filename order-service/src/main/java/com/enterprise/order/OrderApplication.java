package com.enterprise.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Order Service.
 *
 * @author Shivam Srivastav
 */
@SpringBootApplication
@EnableCaching
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
