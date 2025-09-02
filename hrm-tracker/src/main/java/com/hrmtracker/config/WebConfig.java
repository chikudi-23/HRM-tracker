package com.hrmtracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.final-dir}")  // for ID proof / resume
    private String finalDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // For ID proof / resume files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + finalDir + "/");

        // For profile pictures
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:uploads/");
        }

    }

