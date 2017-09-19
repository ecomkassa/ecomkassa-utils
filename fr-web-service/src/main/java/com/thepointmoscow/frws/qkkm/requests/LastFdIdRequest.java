package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
class LastFdIdRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "GetLastFdId")
    private String command = null;
}
