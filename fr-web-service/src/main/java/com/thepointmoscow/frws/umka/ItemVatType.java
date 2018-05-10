package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value added tax (VAT) types.
 */
@AllArgsConstructor
public enum ItemVatType {
    VAT_18PCT(1),
    VAT_10PCT(2),
    VAT_118PCT(3),
    VAT_110PCT(4),
    VAT_0PCT(5),
    VAT_NONE(6),
    VAT_20PCT(6); // strange tax number, fallback to None

    @Getter
    private final int code;

}
