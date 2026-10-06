package com.minibank.util;

import java.math.BigDecimal;

public final class Money {
    private Money(){}

    public static String format(BigDecimal amount) {
        return String.format("NGN %, .2f", amount);
    }
}
