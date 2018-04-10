package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SaleCharge {
    SALE(1),
    SALE_RETURN(2),
    CHARGE(3),
    CHARGE_RETURN(4);

    @Getter
    private final int code;
}
