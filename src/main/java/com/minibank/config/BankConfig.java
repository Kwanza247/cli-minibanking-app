package com.minibank.config;
import java.math.BigDecimal;


public final class BankConfig {
    private BankConfig() {}

    public static final String BANK_NAME = "Mini Bank";

    //Daily outflow limits(withdrawals + transfer out)
    public static final BigDecimal SAVINGS_DAILY_LIMIT = new BigDecimal("500000");
    public static final BigDecimal CURRENT_DAILY_LIMIT = new BigDecimal("1000000");

    //Loan rules
    public static final BigDecimal LOAN_MONTHLY_RATE = new BigDecimal("0.03");//3% per month(simple interest)
    public static final int MAX_LOAN_TENURE_MONTHS = 12;
    public static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("5000000");

    public static final int MAX_PIN_ATTEMPTS = 3;
}