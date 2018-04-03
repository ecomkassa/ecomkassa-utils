package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.RequestLoggingInterceptor;
import com.thepointmoscow.frws.StatusResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Fiscal gateway using "umka" devices.
 *
 * @author unlocker
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class UmkaFiscalGateway implements FiscalGateway {

    private static final int SESSION_EXPIRED_ERROR = 136;
    private static final int CLEARING_TYPE = 4;
    private static final int CLEARING_OBJECT_COMMODITY = 1;
    private static final int SUMMARY_AMOUNT_DENOMINATOR = 1000;

    private final String umkaHost;
    private final int umkaPort;
    private final String username;
    private final String password;
    private final BuildProperties buildProperties;
    private final ObjectMapper mapper;

    private volatile RegInfo lastStatus;

    /**
     * Makes an URL using the host, port and an ending path.
     *
     * @param endingPath ending path
     * @return URL
     */
    private String makeUrl(String endingPath) {
        return String.format("http://%s:%s/%s", getUmkaHost(), getUmkaPort(), endingPath);
    }

    /**
     * Prepares a status object.
     *
     * @return status
     */
    private StatusResult prepareStatus() {
        return new StatusResult().setAppVersion(getBuildProperties().getVersion());
    }

    /**
     * Configures a REST client.
     *
     * @return REST client
     */
    private RestTemplate configureRest() {
        val factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        val interceptor = new RequestLoggingInterceptor();
        return new RestTemplateBuilder()
                .requestFactory(factory)
                .additionalInterceptors(interceptor)
                .basicAuthorization(getUsername(), getPassword())
                .build();
    }

    @Override
    public RegistrationResult register(Order order, Long issueID, boolean openSession) {
        if (openSession) {
            openSession();
        }
        val doc = new FiscalDoc();
        doc.setPrint(1);
        doc.setSessionId(issueID.toString());
        FiscalData data = new FiscalData();
        doc.setData(data);
        data.setDocName("Кассовый чек");
        // multiple payment types are not supported
        val paymentType = order.getPayments().stream().findFirst().map(Order.Payment::getPaymentType)
                .map(PaymentType::valueOf).orElse(PaymentType.CASH);
        data.setMoneyType(paymentType.getCode());
        data.setType(SaleChargeGeneral.valueOf(order.getSaleCharge()).getCode());
        final Long sum = order.getItems().stream()
                .map(it -> it.getPrice() * it.getAmount()).reduce((x, y) -> x + y)
                .orElse(0L) / SUMMARY_AMOUNT_DENOMINATOR;
        data.setSum(0);
        val tags = new ArrayList<FiscalProperty>();
        data.setFiscprops(tags);

        val info = getLastStatus();
        // Registration number, Tax identifier, Tax Variant
        tags.add(new FiscalProperty().setTag(1037).setValue(info.getRegNumber()));
        tags.add(new FiscalProperty().setTag(1018).setValue(info.getInn()));
        tags.add(new FiscalProperty().setTag(1055).setValue(info.getTaxVariant()));
        // check total
        tags.add(new FiscalProperty().setTag(paymentType == PaymentType.CREDIT_CARD ? 1081 : 1031).setValue(sum));
        // Sale Charge
        tags.add(new FiscalProperty().setTag(1054)
                .setValue(SaleCharge.valueOf(order.getSaleCharge()).getCode()));
        // customer id: email or phone
        tags.add(new FiscalProperty().setTag(1008).setValue(order.getCustomer().getId()));

        for (Order.Item i : order.getItems()) {
            val item = new FiscalProperty().setTag(1059).setFiscprops(new ArrayList<>());
            item.getFiscprops().add(new FiscalProperty().setTag(1214).setValue(CLEARING_TYPE));
            item.getFiscprops().add(new FiscalProperty().setTag(1212).setValue(CLEARING_OBJECT_COMMODITY));
            item.getFiscprops().add(new FiscalProperty().setTag(1030).setValue(i.getName()));
            item.getFiscprops().add(new FiscalProperty().setTag(1079).setValue(i.getPrice()));
            item.getFiscprops().add(new FiscalProperty().setTag(1023)
                    .setValue(String.format("%.3f", ((double) i.getAmount()) / SUMMARY_AMOUNT_DENOMINATOR)));
            item.getFiscprops().add(new FiscalProperty().setTag(1199)
                    .setValue(ItemVatType.valueOf(i.getVatType()).getCode()));
            val total = i.getAmount() * i.getPrice() / SUMMARY_AMOUNT_DENOMINATOR;
            item.getFiscprops().add(new FiscalProperty().setTag(1043).setValue(total));
            tags.add(item);
        }
        tags.add(new FiscalProperty().setTag(1060).setValue("www.nalog.ru"));
        RestTemplate rest = configureRest();
        Map<String, Object> request = new HashMap<>();
        request.put("document", doc);

        String responseStr = rest.postForObject(
                makeUrl("fiscalcheck.json"),
                new HttpEntity<>(request, generateHttpHeaders()),
                String.class);
        RegistrationResult registration = new RegistrationResult().apply(status());
        try {
            JsonNode response = mapper.readTree(responseStr);
            if (response.path("document").path("result").asInt(0) == SESSION_EXPIRED_ERROR) {
                closeSession();
                return register(order, issueID, true);
            }
            val propsArr = response.path("document").path("data").path("fiscprops").iterator();
            val codes = new HashSet<Integer>(Arrays.asList(1040, 1042));
            val values = new HashMap<Integer, Integer>();
            ZonedDateTime regDate = null;
            String signature = "TBD";
            while (propsArr.hasNext()) {
                val current = propsArr.next();
                final int tag = current.get("tag").asInt();
                if (1012 == tag) {
                    regDate = OffsetDateTime.parse(current.get("value").asText(), RFC_1123_DATE_TIME).toZonedDateTime();
                    continue;
                }
                if (1077 == tag) {
                    signature = current.get("value").asText();
                    continue;
                }
                if (codes.contains(tag)) {
                    values.put(tag, current.get("value").asInt());
                }
            }
            val regInfo = new RegistrationResult.Registration()
                    .setIssueID(issueID.toString())
                    .setRegDate(regDate)
                    .setDocNo(values.getOrDefault(1040, -1).toString())
                    .setSignature(signature)
                    .setSessionCheck(values.getOrDefault(1042, -1));
            return registration.setRegistration(regInfo);
        } catch (Exception e) {
            log.error("Error parsing the response: {} | {}", responseStr, e.getMessage());
            return registration.setErrorCode(-1);
        }
    }

    /**
     * Makes HTTP headers for JSON request.
     *
     * @return headers
     */
    private HttpHeaders generateHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public StatusResult openSession() {
        configureRest().getForObject(makeUrl("cycleopen.json?print=1"), String.class);
        return status();
    }

    @Override
    public StatusResult closeSession() {
        configureRest().getForObject(makeUrl("cycleclose.json?print=1"), String.class);
        return status();
    }

    @Override
    public StatusResult cancelCheck() {
        throw new UnsupportedOperationException("cancelCheck");
    }

    @Override
    public StatusResult status() {
        RestTemplate rest = configureRest();
        String responseStr = rest.getForObject(makeUrl("cashboxstatus.json"), String.class);
        val result = prepareStatus();
        JsonNode response;
        try {
            response = mapper.readTree(responseStr);
        } catch (IOException e) {
            return result.setErrorCode(-1);
        }
        val status = Optional.ofNullable(response.get("cashboxStatus"));
        result.setErrorCode(0);
        result.setCurrentDocNumber(status.map(x -> x.get("fsStatus").get("lastDocNumber").asInt()).orElse(-1));
        result.setCurrentSession(status.map(x -> x.get("cycleNumber").asInt()).orElse(-1));
        result.setFrDateTime(
                status.map(x -> x.get("dt").asText()).map(x -> OffsetDateTime.parse(x, RFC_1123_DATE_TIME))
                        .map(OffsetDateTime::toLocalDateTime).orElse(LocalDateTime.MIN)
        );
        result.setOnline(status.map(x -> !x.get("offlineMode").asBoolean()).orElse(false));
        final String inn = status.map(x -> x.get("userInn").asText()).orElse("");
        result.setInn(inn);
        final String regNumber = status.map(x -> x.get("regNumber").asText()).orElse("");
        final int taxVariant = status.map(x -> x.get("taxes").asInt()).orElse(0);
        this.lastStatus = new RegInfo(inn, taxVariant, regNumber);
        result.setModeFR(2);
        result.setSubModeFR(0);
        result.setSerialNumber(status.map(x -> x.get("serial").asText()).orElse(""));
        result.setStatusMessage(Optional.ofNullable(response.path("message")).map(JsonNode::asText).orElse(""));
        return result;
    }

    /**
     * Reads last status.
     *
     * @return last status
     */
    private RegInfo getLastStatus() {
        if (lastStatus == null) {
            synchronized (this) {
                if (lastStatus == null) {
                    status();
                }
            }
        }
        return lastStatus;
    }

    @Override
    public StatusResult continuePrint() {
        throw new UnsupportedOperationException("continuePrint");
    }
}
