package com.gitanjsheth.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ProductServiceApplication.class);

        // Define port here
        Map<String, Object> props = new HashMap<>();
        props.put("server.port", 8081); // Change port as needed

        app.setDefaultProperties(props);
        app.run(args);
    }
}