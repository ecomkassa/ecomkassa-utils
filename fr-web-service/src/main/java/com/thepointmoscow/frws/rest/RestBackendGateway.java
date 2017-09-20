package com.thepointmoscow.frws.rest;

import com.thepointmoscow.frws.BackendCommand;
import com.thepointmoscow.frws.BackendGateway;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.StatusResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestBackendGateway implements BackendGateway {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${backend.server.url}")
    private String rootUrl;
    @Value("${backend.server.username}")
    private String username;
    @Value("${backend.server.password}")
    private String password;
    @Value("${backend.server.ccmID}")
    private String ccmID;

    @Override
    public BackendCommand status(StatusResult statusResult) {
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/status?ccmID={ccmID}", statusResult, BackendCommand.class, ccmID);
        return result.getBody();
    }

    @Override
    public BackendCommand sendResult(RegistrationResult registrationResult) {
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/registered?ccmID={ccmID}&issueID={issueID}", registrationResult,
                BackendCommand.class, ccmID, registrationResult.getRegistration().getIssueID());
        return result.getBody();
    }
}
