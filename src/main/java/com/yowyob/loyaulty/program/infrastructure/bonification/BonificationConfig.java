package com.yowyob.loyaulty.program.infrastructure.bonification;

import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationEventPort;
import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationPort;
import com.yowyob.loyaulty.program.domain.loyalty.service.BonificationDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BonificationConfig {

    @Bean
    public BonificationDomainService bonificationDomainService(BonificationPort bonificationPort,
                                                                BonificationEventPort eventPort) {
        return new BonificationDomainService(bonificationPort, eventPort);
    }
}
