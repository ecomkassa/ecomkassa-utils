package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.JsonNode;
import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.StatusResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Fiscal gateway using "umka" devices.
 *
 * @author unlocker
 */
@RequiredArgsConstructor
@Getter
public class UmkaFiscalGateway implements FiscalGateway {

    private final String umkaHost;
    private final int umkaPort;
    private final String username;
    private final String password;
    private final BuildProperties buildProperties;

    private String makeUrl(String endingPath) {
        return String.format("http://%s:%s/%s", getUmkaHost(), getUmkaPort(), endingPath);
    }

    private StatusResult prepareStatus() {
        return new StatusResult().setAppVersion(getBuildProperties().getVersion());
    }

    @Override
    public RegistrationResult register(Order order, Long issueID, boolean openSession) {
        throw new UnsupportedOperationException("register");
    }

    @Override
    public StatusResult openSession() {
        RestTemplate rest = new RestTemplateBuilder()
                .basicAuthorization(getUsername(), getPassword()).build();
        rest.getForObject(makeUrl("cycleopen.json?print=1"), JsonNode.class);
        return status();
    }

    @Override public StatusResult closeSession() {
        RestTemplate rest = new RestTemplateBuilder()
                .basicAuthorization(getUsername(), getPassword()).build();
        rest.getForObject(makeUrl("cycleclose.json?print=1"), JsonNode.class);
        return status();
    }

    @Override public StatusResult cancelCheck() {
        throw new UnsupportedOperationException("cancelCheck");
    }

    @Override
    public StatusResult status() {
        RestTemplate rest = new RestTemplateBuilder()
                .basicAuthorization(getUsername(), getPassword()).build();
        JsonNode response = rest.getForObject(makeUrl("cashboxstatus.json"), JsonNode.class);
        val result = prepareStatus();
        val errorCode = Optional.ofNullable(response.path("result")).map(JsonNode::asInt).orElse(0);
        if (errorCode == 136) {
            return result.setErrorCode(0).setModeFR(3);
        } else if (errorCode != 0) {
            return result.setErrorCode(errorCode);
        }
        val status = Optional.ofNullable(response.path("cashboxStatus"));
        result.setErrorCode(0);
        result.setCurrentDocNumber(status.map(x -> x.get("fsStatus").get("lastDocNumber").asInt()).orElse(-1));
        result.setCurrentSession(status.map(x -> x.get("cycleNumber").asInt()).orElse(-1));
        result.setFrDateTime(status.map(x -> x.get("dt").asText()).map(OffsetDateTime::parse).map(
                OffsetDateTime::toLocalDateTime).orElse(
                LocalDateTime.MIN));
        result.setOnline(
                status.map(x -> ((x.get("fsStatus").get("transport").get("state").asInt() & 1) == 1)).orElse(true));
        result.setInn(status.map(x -> x.get("userInn").asText()).orElse(""));
        result.setModeFR(2);
        result.setSubModeFR(0);
        result.setSerialNumber(status.map(x -> x.get("serial").asText()).orElse(""));
        result.setStatusMessage(Optional.ofNullable(response.path("message")).map(JsonNode::asText).orElse(""));
        return result;
    }

    @Override public StatusResult continuePrint() {
        throw new UnsupportedOperationException("continuePrint");
    }
}
