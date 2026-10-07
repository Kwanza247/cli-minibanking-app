package com.minibank.service;

import com.minibank.exception.BankException;
import com.minibank.model.Account;
import java.math.BigDecimal;
import com.minibank.model.Customer;
import com.minibank.model.LoanAccount;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionType;
import com.minibank.repository.BankRepository;
import com.minibank.util.IdGenerator;
import com.minibank.util.Money;

public class AccountService {
    private final BankRepository repo; // dependency injection (auto wiring)

    public AccountService(BankRepository repo) { // contructor injection
        this.repo = repo; 
    }
 
    /** On a loan account, a deposit is a cash repayment. */
    public Transaction deposit(Account account, BigDecimal amount) {
        requirePositive(amount);
        if(account instanceof LoanAccount) {
            LoanAccount loan = (LoanAccount) account;
            if(!loan.isActive()) throw new BankException("This loan is already fully repaid.");
            if(amount.compareTo(loan.getBalance()) > 0) {
                throw new BankException("Amount exceeds the outstanding loan of " + Money.format(loan.getBalance()) + ".");
            }
            loan.repay(amount);
            return loan.post(TransactionType.LOAN_REPAYMENT, amount, " Cash withdrawal ");
        }
        account.credit(amount);
        return account.post(TransactionType.DEPOSIT, amount, "Cash deposit");
    }

    public Transaction withdraw(Account account, BigDecimal amount) {
        requirePositive(amount);
        requireNotLoan(account);
        checkFundsAndLimit(account, amount);
        account.debit(amount);
        return account.post(TransactionType.WITHDRAWAL, amount, " Cash withdrawal ");
    }

    public Transaction transfer(Account from, String toAccountNumber, BigDecimal amount, String narration) {
        requirePositive(amount);
        requireNotLoan(from);
        Account to = repo.findAccount(toAccountNumber);
        if(to == null) throw new BankException("Destination account not found.");
        if(to.getAccountNumber().equals(from.getAccountNumber())) {
            throw new BankException("You cannot transfer to the same account.");
        }
        checkFundsAndLimit(from, amount);

        String txnId = IdGenerator.newTransactionId(); //both legs share one ID
        String note = (narration == null || narration.isEmpty()) ? "" : " - " + narration;
        from.debit(amount);
        to.credit(amount);
        Transaction out = from.post(TransactionType.TRANSFER_OUT, amount, 
            "Transfer to " + to.getOwnerName() + " (" + to.getAccountNumber() + ")" + note);
        to.post(txnId, TransactionType.TRANSFER_IN, amount, 
            "Transfer from " + from.getOwnerName() + " (" + from.getAccountNumber() + ")" + note);
        return out;
    }
    public Account lookupAccount(String accountNumber) {
        return repo.findAccount(accountNumber);
    }

    /** Searches all of the customer's accounts for a transaction ID */
    public Transaction findTransaction(Customer customer, String transactionId) {
        for(Account a : customer.getAccounts()) {
            for(Transaction t : a.getTransactions()){
                if(t.getId().equalsIgnoreCase(transactionId)) {
                    return t;
                }
            }
        }
        return null;
    }                                                                     

    private void requirePositive(BigDecimal amount) {
        if(amount == null || amount.signum() <= 0) throw new BankException("Amount must must be greater than zero.");
    }

    private void requireNotLoan(Account account) {
        if(account instanceof LoanAccount) throw new BankException("withdrawal and transfers are not allowed on a loan account.");
    }

    private void checkFundsAndLimit(Account account, BigDecimal amount) {
        if(account.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient funds. Available balance:  " + Money.format(account.getBalance()) + ".");
        }
        if(amount.compareTo(account.getRemainingDailyLimit()) > 0) {
            throw new BankException("Daily limit exceeded. Limit " + Money.format(account.getDailyLimit())
                + ", remaining today " + Money.format(account.getRemainingDailyLimit()) + ".");
        }
    }
}
