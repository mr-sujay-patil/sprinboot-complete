package com.task.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// WHY: Marks this as a Spring Boot application, enabling component scanning, auto-configuration, and property support.
@SpringBootApplication
public class TaskServiceApplication {

    public static void main(String[] args) {
        // WHY: Bootstraps the application, starts the embedded Tomcat server, and initializes the Spring application context.
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
