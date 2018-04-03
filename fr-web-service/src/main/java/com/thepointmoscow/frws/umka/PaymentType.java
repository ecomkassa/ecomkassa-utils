package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

@AllArgsConstructor
public enum PaymentType {
    CASH(1),
    CREDIT_CARD(2);

    @Getter
    private final int code;
}
