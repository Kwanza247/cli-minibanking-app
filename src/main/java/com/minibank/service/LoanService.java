package com.minibank.service;

import com.minibank.config.BankConfig;
import com.minibank.exception.BankException;
import com.minibank.model.*;
import com.minibank.repository.BankRepository;
import com.minibank.util.IdGenerator;
import com.minibank.util.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoanService {
    private final BankRepository repo;

    public LoanService(BankRepository repo) {
        this.repo = repo;
    }

    /** Simple interest: principal x 3% x months. */
    public static BigDecimal calculateInterest(BigDecimal principal, int tenureMonths) {
        return principal.multiply(BankConfig.LOAN_MONTHLY_RATE)
                .multiply(BigDecimal.valueOf(tenureMonths))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void validateOffer(Customer customer, BigDecimal principal, int tenureMonths) {
        if (customer.hasActiveLoan()) {
            throw new BankException("You already have an active loan. Repay it before applying for another.");
        }
        if (principal == null || principal.signum() <= 0) throw new BankException("Loan amount must be greater than zero.");
        if (principal.compareTo(BankConfig.MAX_LOAN_AMOUNT) > 0) {
            throw new BankException("Maximum loan amount is " + Money.format(BankConfig.MAX_LOAN_AMOUNT) + ".");
        }
        if (tenureMonths < 1 || tenureMonths > BankConfig.MAX_LOAN_TENURE_MONTHS) {
            throw new BankException("Loan tenure must be between 1 and " + BankConfig.MAX_LOAN_TENURE_MONTHS + " months.");
        }
    }

    /** Creates the loan account and disburses the principal into a savings/current account. */
    public LoanAccount apply(Customer customer, BigDecimal principal, int tenureMonths, Account disburseTo) {
        validateOffer(customer, principal, tenureMonths);
        if (disburseTo == null || disburseTo instanceof LoanAccount) {
            throw new BankException("Loan must be disbursed into a savings or current account.");
        }
        BigDecimal interest = calculateInterest(principal, tenureMonths);
        String number = IdGenerator.newAccountNumber(repo::accountExists);
        LoanAccount loan = new LoanAccount(number, customer.getFullName(), principal, tenureMonths, interest);
        customer.addAccount(loan);
        repo.saveAccount(loan);

        disburseTo.credit(principal);
        disburseTo.post(TransactionType.LOAN_DISBURSEMENT, principal, "Loan disbursement (loan a/c " + number + ")");
        loan.post(TransactionType.LOAN_DISBURSEMENT, principal,
                "Loan booked. Interest " + Money.format(interest) + " added to outstanding");
        return loan;
    }

    /** Pays the loan from a savings/current account. Repayments don't count toward the daily limit. */
    public Transaction repayFromAccount(Account funding, LoanAccount loan, BigDecimal amount) {
        if (funding instanceof LoanAccount) throw new BankException("Pay from a savings or current account.");
        if (!loan.isActive()) throw new BankException("This loan is already fully repaid.");
        if (amount == null || amount.signum() <= 0) throw new BankException("Amount must be greater than zero.");
        if (amount.compareTo(loan.getBalance()) > 0) {
            throw new BankException("Amount exceeds the outstanding loan of " + Money.format(loan.getBalance()) + ".");
        }
        if (funding.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient funds. Available balance: " + Money.format(funding.getBalance()));
        }
        String txnId = IdGenerator.newTransactionId();
        funding.debit(amount);
        loan.repay(amount);
        Transaction t = funding.post(txnId, TransactionType.LOAN_REPAYMENT, amount,
                "Loan repayment to " + loan.getAccountNumber());
        loan.post(txnId, TransactionType.LOAN_REPAYMENT, amount,
                "Repayment from " + funding.getAccountNumber());
        return t;
    }
}