package com.thepointmoscow.frws;

import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

@Slf4j
public class FetchTask implements Runnable {

    private final BackendGateway backend;
    private final FiscalGateway fiscal;
    private final BiConsumer<Runnable, BackendCommand> callback;

    public FetchTask(BackendGateway backend, FiscalGateway fiscal, BiConsumer<Runnable, BackendCommand> callback) {
        this.backend = backend;
        this.fiscal = fiscal;
        this.callback = callback;
    }

    @Override
    public void run() {
        BackendCommand command = null;
        try {
            StatusResult status = fiscal.status();
            command = backend.status(status);
            switch (command.getCommand()) {
            case NONE:
                return;
            case REGISTER:
                RegistrationResult registration = fiscal
                        .register(command.getOrder(), command.getIssueID(), (byte) 4 == status.getModeFR());
                backend.register(registration);
                break;
            case CLOSE_SESSION:
                fiscal.closeSession();
                break;
            }
        } catch (Exception e) {
            log.error("Error while processing own status or input command (" + command + ").", e);
        } finally {
            doCallback(command);
        }
    }

    /**
     * Does a callback.
     *
     * @param command command
     */
    private void doCallback(BackendCommand command) {
        if (this.callback == null)
            return;
        this.callback.accept(this, command);
    }
}
