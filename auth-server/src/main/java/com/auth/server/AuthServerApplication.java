package com.auth.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Auth Server.
 * 
 * WHY: @SpringBootApplication is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services in the 'com.auth.server' package, allowing it to find controllers, services, etc.
 */
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

}
