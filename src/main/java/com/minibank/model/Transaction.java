package com.minibank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String id;
    private final String accountNumber;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime timestamp;
    private final String narration;

    public Transaction(String id, String accountNumber, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime timestamp, String narration) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
        this.narration = narration;
    }

    public String getId(){return id;}
    public String getAccountNumber(){return accountNumber;}
    public TransactionType getType(){return type;}
    public BigDecimal getAmount(){return amount;}
    public BigDecimal getBalanceAfter(){return balanceAfter;}
    public LocalDateTime getTimestamp(){return timestamp;}
    public String getNarration(){return narration;}
    public String getFormattedTime(){return timestamp.format(FMT);}
}
