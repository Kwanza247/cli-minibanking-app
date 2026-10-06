package com.minibank.model;

import com.minibank.config.BankConfig;

import java.math.BigDecimal;

public class  SavingsAccount extends Account{
    public SavingsAccount(String accountNumber, String ownerName) {
        super(accountNumber, AccountType.SAVINGS, ownerName);
    }

    @Override 
    public BigDecimal getDailyLimit() {
        return BankConfig.SAVINGS_DAILY_LIMIT;
    }
}
