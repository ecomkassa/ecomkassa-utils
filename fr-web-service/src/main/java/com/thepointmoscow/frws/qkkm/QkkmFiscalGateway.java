package com.thepointmoscow.frws.qkkm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.StatusResult;
import com.thepointmoscow.frws.qkkm.requests.DeviceStatusRequest;
import com.thepointmoscow.frws.qkkm.requests.QkkmRequest;
import com.thepointmoscow.frws.qkkm.requests.XReportRequest;
import com.thepointmoscow.frws.qkkm.requests.ZReportRequest;
import com.thepointmoscow.frws.qkkm.responses.DeviceStatusResponse;
import com.thepointmoscow.frws.qkkm.responses.QkkmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * A fiscal gateway based on QKKM server.
 */
@Component
@Slf4j
public class QkkmFiscalGateway implements FiscalGateway {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    @Value("${qkkm.server.host}")
    private String host;
    @Value("${qkkm.server.port}")
    private int port;

    /**
     * Executes a text command with a fiscal registrar.
     *
     * @param command command
     * @return text response
     * @throws IOException possibly IO exception
     */
    private String executeCommand(String command) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(command.getBytes(CHARSET));
            byte buf[] = new byte[32 * 1024];
            int len = socket.getInputStream().read(buf);
            return new String(buf, 0, len, CHARSET);
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
    private <RESP> RESP executeCommand(QkkmRequest request, Class<RESP> responseType) throws IOException {
        XmlMapper mapper = new XmlMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String raw = executeCommand(mapper.writeValueAsString(request));
        return mapper.readValue(raw, responseType);
    }

    @Override
    public RegistrationResult register(Order order, Long issueID) {
        throw new UnsupportedOperationException("register");
    }

    @Override
    public StatusResult closeSession() {
        try {
            executeCommand(new XReportRequest(), QkkmResponse.class);
            executeCommand(new ZReportRequest(), QkkmResponse.class);
            return status();
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
