package com.thepointmoscow.frws;

/**
 * Backend gateway.
 */
public interface BackendGateway {
    /**
     * Sends status to backend.
     *
     * @param statusResult status
     * @return command
     */
    BackendCommand status(StatusResult statusResult);

    /**
     * Sends registration to backend.
     *
     * @param registration registration
     * @return command
     */
    BackendCommand register(RegistrationResult registration);
}
