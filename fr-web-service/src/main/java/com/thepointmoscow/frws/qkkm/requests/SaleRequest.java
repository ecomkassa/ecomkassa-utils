package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SaleRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "Sale")
    private Sale sale;

    @Data
    public static class Sale {
        @JacksonXmlProperty(isAttribute = true, localName = "Text")
        private String text;
        @JacksonXmlProperty(isAttribute = true, localName = "Amount")
        private int amount;
        @JacksonXmlProperty(isAttribute = true, localName = "Price")
        private int price;
        @JacksonXmlProperty(isAttribute = true, localName = "Group")
        private String group;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax1")
        private int tax1;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax2")
        private int tax2;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax3")
        private int tax3;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax4")
        private int tax4;
    }
}
