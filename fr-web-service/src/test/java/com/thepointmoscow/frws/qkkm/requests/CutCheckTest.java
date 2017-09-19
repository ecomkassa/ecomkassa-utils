package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CutCheckTest {

    @Test
    void shouldSerializeCommand() throws IOException {
        // GIVEN
        XmlMapper mapper = new XmlMapper();
        QkkmRequest command = new CutCheck();
        // WHEN
        String result = mapper.writeValueAsString(command);
        // THEN
        assertEquals("<ControlProtocol messageType=\"request\"><CutCheck/></ControlProtocol>", result);
    }
}