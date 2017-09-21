package com.thepointmoscow.frws;

/**
 * A gateway for a fiscal registrar.
 */
public interface FiscalGateway {
    /**
     * Makes a voucher register command.
     *
     * @param order       order info
     * @param issueID     issue ID
     * @param openSession is need session to open
     * @return result
     */
    RegistrationResult register(Order order, Long issueID, boolean openSession);

    /**
     * Makes a session closing command.
     *
     * @return status
     */
    StatusResult closeSession();

    /**
     * Makes a status retrieval command.
     *
     * @return status
     */
    StatusResult status();
}
