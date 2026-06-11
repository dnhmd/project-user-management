package com.dnhmd.user_management.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private String secretKey;
    private Integer accessTokenExpireMinutes;
    private String algorithm;
    private Integer passwordResetTokenExpireMinutes;
    private Integer refreshTokenExpireDays;
}
