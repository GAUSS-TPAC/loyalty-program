package com.yowyob.loyaulty.program.shared.config;

import com.yowyob.loyaulty.program.shared.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AppConfig {
}
