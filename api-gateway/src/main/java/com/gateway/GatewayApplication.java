package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// WHY: Gateway uses standard Spring Boot auto-configuration.
// Since Spring Cloud Gateway is reactive and built on WebFlux, it doesn't need @EnableGateway;
// inclusion of the dependency on the classpath automatically configures routing beans.
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
