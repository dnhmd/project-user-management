package com.dnhmd.user_management.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private Boolean debug;
    private String environment;
    private String adminEmail;
    private String adminPassword;
}
