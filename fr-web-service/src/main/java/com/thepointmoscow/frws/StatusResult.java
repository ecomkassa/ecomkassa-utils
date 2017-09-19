package com.thepointmoscow.frws;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class StatusResult {
    private boolean isOnline;
    private int errorCode;
    private LocalDateTime frDateTime;
    private String inn;
    private String serialNumber;
    private int currentDocNumber;
    private int currentSession;
    private byte modeFR;
    private byte subModeFR;
    private String statusMessage;
}
