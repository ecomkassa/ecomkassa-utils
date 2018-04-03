package com.thepointmoscow.frws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thepointmoscow.frws.fakes.LoggingFiscalGateway;
import com.thepointmoscow.frws.qkkm.QkkmFiscalGateway;
import com.thepointmoscow.frws.umka.UmkaFiscalGateway;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.info.BuildProperties;
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

@Configuration
@Slf4j
public class AppConfig {

    private static final String UMKA_DEFAULT_LOGIN = "1";
    private static final String UMKA_DEFAULT_PASSWORD = "1";
    private final BuildProperties buildProperties;

    @Autowired
    public AppConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public ClientHttpRequestFactory requestFactory() {
        return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    }

    @Bean
    public ClientHttpRequestInterceptor interceptor() {
        return new RequestLoggingInterceptor();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RestTemplate restTemplate(
            RestTemplateBuilder builder, ClientHttpRequestFactory factory, ClientHttpRequestInterceptor interceptor) {
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

    @Bean
    public FiscalGateway fiscalGateway(ObjectMapper mapper) {

        try {
            switch (FiscalServerType.valueOf(fgType)) {
            case mock:
                return new LoggingFiscalGateway(buildProperties);
            case qkkm:
                return new QkkmFiscalGateway(buildProperties).setHost(fgHost).setPort(fgPort);
            case umka:
                return new UmkaFiscalGateway(fgHost, fgPort, UMKA_DEFAULT_LOGIN, UMKA_DEFAULT_PASSWORD, buildProperties,
                        mapper);
            default:
                throw new IllegalArgumentException(getFgType());
            }
        } catch (IllegalArgumentException e) {
            log.error("'fiscal.server.type' property value is unknown: {}. "
                            + "Intended to use some of 'mock', 'qkkm' or 'umka'",
                    fgType);
            throw e;
        }
    }

    @Getter
    @Setter
    @Value("${fiscal.server.host}")
    private String fgHost;
    @Getter
    @Setter
    @Value("${fiscal.server.port}")
    private int fgPort;
    @Getter
    @Setter
    @Value("${fiscal.server.type}")
    private String fgType;

}
