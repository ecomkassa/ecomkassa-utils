package com.thepointmoscow.frws.qkkm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.StatusResult;
import com.thepointmoscow.frws.qkkm.requests.*;
import com.thepointmoscow.frws.qkkm.responses.DeviceStatusResponse;
import com.thepointmoscow.frws.qkkm.responses.FiscalMarkResponse;
import com.thepointmoscow.frws.qkkm.responses.LastFdIdResponse;
import com.thepointmoscow.frws.qkkm.responses.QkkmResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.*;
import java.util.Objects;

import static com.thepointmoscow.frws.qkkm.requests.OpenCheckRequest.SALE_TYPE;
import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * A fiscal gateway based on QKKM server.
 */
@Slf4j
@Accessors(chain = true)
public class QkkmFiscalGateway implements FiscalGateway {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String VAT_18_PCT = "VAT_18PCT";
    private static final String VAT_10_PCT = "VAT_10PCT";
    private static final String VAT_0_PCT = "VAT_0PCT";
    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private int port;

    /**
     * Executes a text command with a fiscal registrar.
     *
     * @param command command
     * @return text response
     * @throws IOException possibly IO exception
     */
    private synchronized String executeCommand(String command) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(command.getBytes(CHARSET));
            byte buf[] = new byte[32 * 1024];
            int len = socket.getInputStream().read(buf);
            String response = new String(buf, 0, len, CHARSET);
            log.info("SENT >>> {}; RECEIVED <<< {}", command, response);
            return response;
        }
    }

    /**
     * Executes command with a fiscal registrar.
     *
     * @param request      request object
     * @param responseType response type
     * @param <RESP>       response type
     * @return response object
     * @throws IOException possibly IO exception
     */
    private <RESP extends QkkmResponse> RESP executeCommand(QkkmRequest request, Class<RESP> responseType) throws IOException, QkkmException {
        XmlMapper mapper = new XmlMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        RESP resp;
        do {
            String raw = executeCommand(mapper.writeValueAsString(request));
            resp = mapper.readValue(raw, responseType);
        } while (resp.getError().getId() == 80);
        if (resp.getError().getId() != 0)
            throw new QkkmException(resp.getError().getText(), resp.getError().getId());
        return resp;
    }

    @Override
    public RegistrationResult register(Order order, Long issueID, boolean openSession) {
        try {
            if (openSession) {
                executeCommand(new OpenSessionRequest(), QkkmResponse.class);
            }
            executeCommand(
                    new OpenCheckRequest().setOpenCheck(
                            new OpenCheckRequest.OpenCheck().setType(SALE_TYPE).setOperator(order.getCashier().toString())
                    ), QkkmResponse.class);
            for (Order.Item item : order.getItems()) {
                executeCommand(new SaleRequest().setSale(
                        new SaleRequest.Sale()
                                .setText(item.getName())
                                .setAmount(item.getAmount())
                                .setPrice(item.getPrice())
                                .setTax1(Objects.equals(VAT_18_PCT, item.getVatType()) ? 1 : 0)
                                .setTax2(Objects.equals(VAT_10_PCT, item.getVatType()) ? 1 : 0)
                                .setTax3(0) // VAT 20% is not applicable
                                .setTax4(Objects.equals(VAT_0_PCT, item.getVatType()) ? 1 : 0)
                                .setGroup("0")
                ), QkkmResponse.class);
            }

            if (order.getIsElectronic()) {
                executeCommand(new SetTlvRequest().setSetTlv(
                        new SetTlvRequest.SetTlv()
                                .setType("1008")
                                .setData(order.getCustomer().getId())
                                .setLen(order.getCustomer().getId().length())
                        ), QkkmResponse.class
                );
            }

            long[] payments = new long[]{0, 0, 0, 0};
            for (Order.Payment pmt : order.getPayments()) {
                switch (pmt.getPaymentType()) {
                    case "CASH":
                        payments[0] += pmt.getAmount();
                        break;
                    case "CREDIT_CARD":
                        payments[1] += pmt.getAmount();
                        break;
                }
            }

            executeCommand(new CloseCheckRequest().setOpenCheck(
                    new CloseCheckRequest.CloseCheck()
                            .setSummaCash(payments[0])
                            .setSumma2(payments[1])
                            .setSumma3(payments[2])
                            .setSumma4(payments[3])
                            .setTax1(order.getItems().stream().map(Order.Item::getVatType)
                                    .anyMatch(x -> Objects.equals(VAT_18_PCT, x)) ? 1 : 0)
                            .setTax2(order.getItems().stream().map(Order.Item::getVatType)
                                    .anyMatch(x -> Objects.equals(VAT_10_PCT, x)) ? 1 : 0)
                            .setTax3(0)
                            .setTax4(order.getItems().stream().map(Order.Item::getVatType)
                                    .anyMatch(x -> Objects.equals(VAT_0_PCT, x)) ? 1 : 0)
            ), QkkmResponse.class);

            String docId = executeCommand(new LastFdIdRequest(), LastFdIdResponse.class).getResponse().getId();
            String sign = executeCommand(new FiscalMarkRequest().setCommand(
                    new FiscalMarkRequest.FiscalMark().setId(docId)
            ), FiscalMarkResponse.class).getResponse().getId();

            StatusResult status = status();

            return new RegistrationResult().apply(status).setRegistration(
                    new RegistrationResult.Registration()
                            .setDocNo(docId)
                            .setIssueID(issueID.toString())
                            .setRegDate(ZonedDateTime.of(status.getFrDateTime(), ZoneId.of(order.getFirm().getTimezone())))
                            .setSignature(sign)
            );
        } catch (QkkmException e) {
            log.error("An error occurred while registering.", e);
            return new RegistrationResult()
                    .setErrorCode(e.getErrorCode())
                    .setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while registering.", e);
            return new RegistrationResult()
                    .setErrorCode(-1)
                    .setStatusMessage(e.getMessage());
        }
    }

    @Override
    public StatusResult closeSession() {
        try {
            executeCommand(new XReportRequest(), QkkmResponse.class);
            executeCommand(new ZReportRequest(), QkkmResponse.class);
            return status();
        } catch (QkkmException e) {
            log.error("An error occurred while closing session.", e);
            return new StatusResult()
                    .setErrorCode(e.getErrorCode())
                    .setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while closing session.", e);
            return new StatusResult()
                    .setErrorCode(-1)
                    .setStatusMessage(e.getMessage());
        }

    }

    @Override
    public StatusResult status() {
        StatusResult result = new StatusResult();
        try {
            DeviceStatusResponse dsr = executeCommand(new DeviceStatusRequest(), DeviceStatusResponse.class);
            if (!Objects.equals(dsr.getError().getId(), 0)) {
                return result
                        .setStatusMessage(dsr.getError().getText())
                        .setOnline(false)
                        .setErrorCode(dsr.getError().getId());
            }
            DeviceStatusResponse.DeviceStatus ds = dsr.getStatus();
            result.setOnline("1".equals(ds.getIsOnline()))
                    .setStatusMessage(ds.getStatusMessageHTML())
                    .setCurrentDocNumber(ds.getCurrentDocNumber())
                    .setCurrentSession(ds.getNumberLastClousedSession() + 1)
                    .setErrorCode(ds.getDeviceErrorCode())
                    .setInn(ds.getInn())
                    .setSerialNumber(ds.getSerialNumber())
                    .setModeFR(ds.getModeFR())
                    .setSubModeFR(ds.getSubModeFR())
                    .setFrDateTime(LocalDateTime.of(
                            LocalDate.parse(ds.getDateFR(), ofPattern("yyyy.MM.dd")),
                            LocalTime.parse(ds.getTimeFR(), ofPattern("HH:mm:ss"))
                    ));
        } catch (Exception e) {
            log.error("Error while fetching a status of the fiscal registrar", e);
            result.setErrorCode(-1);
            result.setStatusMessage(e.getMessage());
        }
        return result;
    }
}
