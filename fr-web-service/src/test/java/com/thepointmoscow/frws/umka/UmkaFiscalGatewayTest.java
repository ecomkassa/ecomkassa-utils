package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UmkaFiscalGatewayTest {

    private static final String TEST_HOST = "office.armax.ru";
    private static final int TEST_PORT = 58088;
    private static final String USERNAME = "33";
    private static final String PASSWORD = "33";
    private static final String APP_VERSION = "1.2.3-test";

    private UmkaFiscalGateway sut;

    @BeforeEach
    void setup(){
        BuildProperties props = Mockito.mock(BuildProperties.class);
        Mockito.when(props.getVersion()).thenReturn(APP_VERSION);
        this.sut = new UmkaFiscalGateway(TEST_HOST, TEST_PORT, USERNAME, PASSWORD, props, new ObjectMapper());
    }

    @Test
    void shouldReturnStatus(){
        // GIVEN // WHEN
        val result = sut.status();
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualToIgnoringCase("status");
        assertThat(result.getErrorCode()).isEqualTo(0);
        assertThat(result.getModeFR()).isEqualTo(2);
        assertThat(result.getSerialNumber()).isNotEmpty();
        assertThat(result.getCurrentDocNumber()).isNotEqualTo(0);
        assertThat(result.getCurrentSession()).isNotEqualTo(0);
        assertThat(result.getAppVersion()).isEqualTo(APP_VERSION);
        assertThat(result.getFrDateTime()).isNotNull().isNotEqualTo(LocalDateTime.MIN);
        assertThat(result.isOnline()).isTrue();
    }

}