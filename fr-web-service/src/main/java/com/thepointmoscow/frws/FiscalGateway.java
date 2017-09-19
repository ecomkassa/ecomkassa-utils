package com.thepointmoscow.frws;

public interface FiscalGateway {

    RegistrationResult register(RegistrationIssue issue);

    StatusResult status();
}
