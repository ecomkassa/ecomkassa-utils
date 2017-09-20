package com.thepointmoscow.frws;

public interface BackendGateway {
    BackendCommand status(StatusResult statusResult);

    BackendCommand sendResult(RegistrationResult registrationResult);
}
