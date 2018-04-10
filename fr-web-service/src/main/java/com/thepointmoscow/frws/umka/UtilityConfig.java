package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thepointmoscow.frws.FiscalServerType;
import com.thepointmoscow.frws.RequestLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.thepointmoscow.frws.FiscalServerType.umka;

@Configuration
public class UtilityConfig {
    private static final String UMKA_DEFAULT_LOGIN = "1";
    private static final String UMKA_DEFAULT_PASSWORD = "1";

    @Bean
    public ClientHttpRequestFactory requestFactory() {
        return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    }

    @Bean
    public ClientHttpRequestInterceptor interceptor() {
        return new RequestLoggingInterceptor();
    }

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            ClientHttpRequestFactory factory,
            ClientHttpRequestInterceptor interceptor,
            @Value("${fiscal.server.type}") String fgType) {

        if (umka == FiscalServerType.valueOf(fgType)) {
            builder.basicAuthorization(UMKA_DEFAULT_LOGIN, UMKA_DEFAULT_PASSWORD);
        }
        return builder
                .requestFactory(factory)
                .additionalInterceptors(interceptor)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .indentOutput(false)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(new JavaTimeModule())
                .build();
    }

    @Bean
    @Scope
    public ScheduledExecutorService taskExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

}
