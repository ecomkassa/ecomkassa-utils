package com.thepointmoscow.frws;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RegistrationResult extends StatusResult {

    public RegistrationResult() {
        super("REGISTRATION");
    }

    private Registration registration;

    @Data
    @Accessors(chain = true)
    public static class Registration {
        private String issueID;
        private String signature;
        private String docNo;
        private ZonedDateTime regDate;
    }
}
