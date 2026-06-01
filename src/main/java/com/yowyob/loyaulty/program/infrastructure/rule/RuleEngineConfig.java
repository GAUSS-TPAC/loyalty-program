package com.yowyob.loyaulty.program.infrastructure.rule;

import com.yowyob.loyaulty.program.domain.rule.service.RuleEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleEngineConfig {

    @Bean
    public RuleEngine ruleEngine() {
        return new RuleEngine();
    }
}
