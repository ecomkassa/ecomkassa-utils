package com.thepointmoscow.frws;

public interface FiscalGateway {

    RegistrationResult register(Order order, Long issueID, boolean openSession);

    StatusResult closeSession();

    StatusResult status();
}
