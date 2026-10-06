package com.minibank.model;

import com.minibank.config.BankConfig;

import java.math.BigDecimal;

public class CurrentAccount extends Account{
    public CurrentAccount(String accountNumber, String ownerName) {
        super(accountNumber, AccountType.CURRENT, ownerName);
    }

    @Override 
    public BigDecimal getDailyLimit() {
        return BankConfig.CURRENT_DAILY_LIMIT;
    }
}
