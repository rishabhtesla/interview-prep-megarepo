package com.interviewprep.progress;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class HttpClientConfiguration {
    @Bean
    RestClientCustomizer boundedRequests(
            @Value("${catalog.connect-timeout:1s}") Duration connectTimeout,
            @Value("${catalog.read-timeout:2s}") Duration readTimeout) {
        return builder -> {
            var factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(connectTimeout);
            factory.setReadTimeout(readTimeout);
            builder.requestFactory(factory);
        };
    }
}
