package com.dietapp.usermanagement.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    private List<CorsMapping> mappings;

    @Data
    public static class CorsMapping {
        private String path;
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private Boolean allowCredentials;
    }
}
