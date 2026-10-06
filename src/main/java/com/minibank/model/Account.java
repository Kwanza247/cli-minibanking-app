package com.minibank.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.minibank.util.IdGenerator;

public abstract class Account {
    private final String accountNumber;
    private final AccountType type;
    private final String ownerName;
    protected BigDecimal balance = BigDecimal.ZERO;
    private final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber, AccountType type, String ownerName) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.ownerName = ownerName; 
    }

    /** Max total of withdrawals + transfers out per calendar day. */
    public abstract BigDecimal getDailyLimit();

    public String getAccountNumber() { return accountNumber; }
    public AccountType getType() { return type; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }

    public void credit(BigDecimal amount) { balance = balance.add(amount); }
    public void debit(BigDecimal amount) { balance = balance.subtract(amount); }
    
    /** Records a transaction with new transaction ID */
    public Transaction post(TransactionType type, BigDecimal amount, String narration) {
        return post(IdGenerator.newTransactionId(), type, amount, narration);
    }

    /** Records a transaction with a given ID(used so both legs of a transfer share one ID). */
    public Transaction post(String id, TransactionType type, BigDecimal amount, String narration){
        Transaction t = new Transaction(id, accountNumber, type, amount, balance, LocalDateTime.now(), narration);
        transactions.add(t);
        return t;
    }public BigDecimal getOutflowToday() {
        LocalDate today = LocalDate.now();
        BigDecimal total = BigDecimal.ZERO;
        for(Transaction t : transactions) {
            boolean outflow = t.getType() == TransactionType.WITHDRAWAL || t.getType() == TransactionType.TRANSFER_OUT;
            if (outflow && t.getTimestamp().toLocalDate().equals(today)) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    public BigDecimal getRemainingDailyLimit() {
        return getDailyLimit().subtract(getOutflowToday()).max(BigDecimal.ZERO);
    }
}
