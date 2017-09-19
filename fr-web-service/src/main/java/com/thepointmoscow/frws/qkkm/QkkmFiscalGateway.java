package com.thepointmoscow.frws.qkkm;

import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.RegistrationIssue;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.StatusResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * A fiscal gateway based on QKKM server.
 */
@Component
public class QkkmFiscalGateway implements FiscalGateway {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private String host;
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


    @Override
    public RegistrationResult register(RegistrationIssue issue) {
        throw new UnsupportedOperationException("register");
    }

    @Override
    public StatusResult status() {
        throw new UnsupportedOperationException("status");
    }
}
