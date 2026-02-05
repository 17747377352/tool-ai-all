package com.example.simvoice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({KimiProperties.class, DoubaoProperties.class})
public class AiClientsConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // 给外部AI接口一个比较保守的超时，避免卡住线程
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }
}
//


