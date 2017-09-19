package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
class DeviceStatusRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "GetDeviceStatus")
    private String cutCheck = null;
}
