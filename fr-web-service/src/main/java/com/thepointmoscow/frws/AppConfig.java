package com.thepointmoscow.frws;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.fakes.LoggingFiscalGateway;
import com.thepointmoscow.frws.qkkm.QkkmFiscalGateway;
import lombok.Getter;
import lombok.Setter;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class AppConfig {

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
                .additionalMessageConverters(mappingJackson())
                .additionalInterceptors(interceptor)
                .build();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter.setObjectMapper(mapper);
        return converter;
    }

    @Bean
    @Scope
    public ScheduledExecutorService taskExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

    @Bean
    public FiscalGateway fiscalGateway() {
        if (isFgMock())
            return new LoggingFiscalGateway();
        return new QkkmFiscalGateway().setHost(fgHost).setPort(fgPort);
    }

    @Getter
    @Setter
    @Value("${qkkm.server.host}")
    private String fgHost;
    @Getter
    @Setter
    @Value("${qkkm.server.port}")
    private int fgPort;
    @Getter
    @Setter
    @Value("${qkkm.server.mock}")
    private boolean fgMock;


}
