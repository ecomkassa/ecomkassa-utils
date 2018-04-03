package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ItemVatType {
    VAT_18PCT(1),
    VAT_10PCT(2),
    VAT_20PCT(6), // strange tax number, fallback to None
    VAT_0PCT(5);

    @Getter
    private final int code;

}
