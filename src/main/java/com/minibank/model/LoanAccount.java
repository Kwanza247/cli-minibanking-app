package com.minibank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoanAccount extends Account {
    /** for a loan account balance means the outstanding amount still owed */

    private final BigDecimal principal;
    private final BigDecimal totalInterest;
    private final int tenureMonths;


    public LoanAccount(String accountNumber, String ownerName, BigDecimal totalInterest, int tenureMonths, BigDecimal principal) {
        
        super(accountNumber, AccountType.LOAN, ownerName);
        this.principal = principal;
        this.totalInterest = totalInterest;
        this.tenureMonths = tenureMonths;
        this.balance = principal.add(totalInterest); 
    }

    @Override 
    public BigDecimal getDailyLimit() {
        return BigDecimal.ZERO; //NO outflow from a loan account, so daily limit is 0
    }

    public void repay(BigDecimal amount) {debit(amount); }
    public boolean isActive() { return balance.signum() > 0; }

    public BigDecimal getPrincipal() { return principal; }
    public BigDecimal getTotalInterest() { return totalInterest; }
    public BigDecimal getTotalRepayable() { return principal.add(totalInterest); }
    public int getTenureMonths() { return tenureMonths; }

    public BigDecimal getMonthlyInstallment() {
        return getTotalRepayable().divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
    }

}
