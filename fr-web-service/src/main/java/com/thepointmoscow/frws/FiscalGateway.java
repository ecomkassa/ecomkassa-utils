package com.thepointmoscow.frws;

public interface FiscalGateway {

    RegistrationResult register(Order order, Long issueID);

    StatusResult closeSession();

    StatusResult status();
}
