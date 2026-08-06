package com.ai.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.ai.service.config.AiProviderProperties;

// WHY: @EnableConfigurationProperties registers our custom AiProviderProperties class
// so Spring can bind the 'ai.*' YAML properties to it at startup.
@SpringBootApplication
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
