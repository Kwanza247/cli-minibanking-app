package com.minibank.ui;

import com.minibank.config.BankConfig;
import com.minibank.exception.BankException;
import com.minibank.model.*;
import com.minibank.service.AccountService;
import com.minibank.service.CustomerService;
import com.minibank.service.LoanService;
import com.minibank.util.ConsoleInput;
import com.minibank.util.Money;
import com.minibank.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DashboardMenu {
    private final Customer customer;
    private final ConsoleInput in;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final LoanService loanService;
    private Account active;

    public DashboardMenu(Customer customer, ConsoleInput in, CustomerService customerService,
                         AccountService accountService, LoanService loanService) {
        this.customer = customer;
        this.in = in;
        this.customerService = customerService;
        this.accountService = accountService;
        this.loanService = loanService;
        this.active = customer.getAccounts().get(0);
    }

    public void run() {
        boolean running = true;
        while (running) {
            printHeader();
            printMenu();
            int choice = in.readInt("Choose an option: ", 0, 10);
            try {
                switch (choice) {
                    case 1: deposit(); break;
                    case 2: withdraw(); break;
                    case 3: transfer(); break;
                    case 4: checkBalance(); break;
                    case 5: history(); break;
                    case 6: findTransaction(); break;
                    case 7: switchAccount(); break;
                    case 8: openAccount(); break;
                    case 9: applyForLoan(); break;
                    case 10: repayLoan(); break;
                    default: running = false;
                }
            } catch (BankException e) {
                System.out.println("\n[!] " + e.getMessage());
            }
        }
        System.out.println("\nLogged out. See you again, " + customer.getFullName() + "!");
    }

    // ---------- display ----------

    private void printHeader() {
        String label = active.getType() == AccountType.LOAN ? "Outstanding Loan" : "Balance";
        System.out.println("\n==================================================");
        System.out.println("Hello " + customer.getFullName() + "!");
        System.out.println("Account Number : " + active.getAccountNumber() + " (" + active.getType() + ")");
        System.out.println(label + " : " + Money.format(active.getBalance()));
        System.out.println("==================================================");
    }

    private void printMenu() {
        System.out.println(" 1. Deposit");
        System.out.println(" 2. Withdraw");
        System.out.println(" 3. Transfer");
        System.out.println(" 4. Check balance / account details");
        System.out.println(" 5. Transaction history");
        System.out.println(" 6. Find transaction by ID");
        System.out.println(" 7. Switch account");
        System.out.println(" 8. Open new savings/current account");
        System.out.println(" 9. Apply for a loan");
        System.out.println("10. Repay loan");
        System.out.println(" 0. Logout");
    }

    private void printReceipt(Transaction t) {
        System.out.println("\n--- Transaction successful ---");
        System.out.println("Transaction ID : " + t.getId());
        System.out.println("Type           : " + t.getType());
        System.out.println("Amount         : " + Money.format(t.getAmount()));
        System.out.println("Balance After  : " + Money.format(t.getBalanceAfter()));
        System.out.println("Date/Time      : " + t.getFormattedTime());
        System.out.println("Narration      : " + t.getNarration());
    }

    // ---------- operations ----------

    private void deposit() {
        String prompt = active.getType() == AccountType.LOAN ? "Repayment amount: NGN " : "Amount to deposit: NGN ";
        printReceipt(accountService.deposit(active, in.readAmount(prompt)));
    }

    private void withdraw() {
        if (active.getType() == AccountType.LOAN) {
            throw new BankException("Withdrawals are not allowed on a loan account. Switch to a savings/current account.");
        }
        BigDecimal amount = in.readAmount("Amount to withdraw: NGN ");
        confirmPin();
        printReceipt(accountService.withdraw(active, amount));
    }

    private void transfer() {
        if (active.getType() == AccountType.LOAN) {
            throw new BankException("Transfers are not allowed from a loan account. Switch to a savings/current account.");
        }
        String dest = in.readValid("Destination account number (10 digits): ", Validator::isValidAccountNumber,
                "Account number must be exactly 10 digits.");
        Account to = accountService.lookupAccount(dest);
        if (to == null) throw new BankException("Destination account not found.");
        System.out.println("Account name   : " + to.getOwnerName());
        BigDecimal amount = in.readAmount("Amount to transfer: NGN ");
        String narration = in.readLine("Narration (optional): ");
        if (!in.confirm("Send " + Money.format(amount) + " to " + to.getOwnerName() + "?")) {
            System.out.println("Transfer cancelled.");
            return;
        }
        confirmPin();
        printReceipt(accountService.transfer(active, dest, amount, narration));
    }

    private void checkBalance() {
        System.out.println("\n--- Account summary ---");
        System.out.println("Account Number : " + active.getAccountNumber());
        System.out.println("Account Type   : " + active.getType());
        if (active instanceof LoanAccount) {
            LoanAccount loan = (LoanAccount) active;
            System.out.println("Principal      : " + Money.format(loan.getPrincipal()));
            System.out.println("Interest (" + BankConfig.LOAN_MONTHLY_RATE.multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString()
                    + "%/month): " + Money.format(loan.getTotalInterest()));
            System.out.println("Total Repayable: " + Money.format(loan.getTotalRepayable()));
            System.out.println("Tenure         : " + loan.getTenureMonths() + " month(s)");
            System.out.println("Monthly Instal.: " + Money.format(loan.getMonthlyInstallment()));
            System.out.println("Outstanding    : " + Money.format(loan.getBalance()));
        } else {
            System.out.println("Balance        : " + Money.format(active.getBalance()));
            System.out.println("Daily Limit    : " + Money.format(active.getDailyLimit()));
            System.out.println("Used Today     : " + Money.format(active.getOutflowToday()));
            System.out.println("Remaining Today: " + Money.format(active.getRemainingDailyLimit()));
        }
    }

    private void history() {
        List<Transaction> list = active.getTransactions();
        if (list.isEmpty()) {
            System.out.println("\nNo transactions on this account yet.");
            return;
        }
        System.out.println("\n--- Transaction history (" + active.getAccountNumber() + ") ---");
        for (int i = list.size() - 1; i >= 0; i--) {
            Transaction t = list.get(i);
            System.out.println(t.getFormattedTime() + " | " + t.getId() + " | " + t.getType());
            System.out.println("    Amount: " + Money.format(t.getAmount()) + " | Balance: " + Money.format(t.getBalanceAfter()));
            System.out.println("    " + t.getNarration());
        }
    }

    private void findTransaction() {
        String id = in.readLine("Enter transaction ID: ");
        Transaction t = accountService.findTransaction(customer, id);
        if (t == null) {
            System.out.println("No transaction with that ID was found on your accounts.");
        } else {
            printReceipt(t);
            System.out.println("Account        : " + t.getAccountNumber());
        }
    }

    private void switchAccount() {
        List<Account> accounts = customer.getAccounts();
        System.out.println("\n--- Your accounts ---");
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            String label = a.getType() == AccountType.LOAN ? "Outstanding" : "Balance";
            System.out.println((i + 1) + ". " + a.getAccountNumber() + " | " + a.getType()
                    + " | " + label + ": " + Money.format(a.getBalance())
                    + (a == active ? "  <-- current" : ""));
        }
        int choice = in.readInt("Select account (0 to cancel): ", 0, accounts.size());
        if (choice > 0) {
            active = accounts.get(choice - 1);
            System.out.println("Switched to account " + active.getAccountNumber() + " (" + active.getType() + ").");
        }
    }

    private void openAccount() {
        System.out.println("1. Savings   2. Current   0. Cancel");
        int choice = in.readInt("Choose account type: ", 0, 2);
        if (choice == 0) return;
        Account a = customerService.openAccount(customer, choice == 1 ? AccountType.SAVINGS : AccountType.CURRENT);
        active = a;
        System.out.println("\n--- New account generated ---");
        System.out.println("Account Number : " + a.getAccountNumber());
        System.out.println("Account Type   : " + a.getType());
        System.out.println("Balance        : " + Money.format(a.getBalance()));
        System.out.println("(Switched to this account.)");
    }

    private void applyForLoan() {
        if (customer.hasActiveLoan()) {
            throw new BankException("You already have an active loan. Repay it before applying for another.");
        }
        System.out.println("\n--- Loan application ---");
        System.out.println("Max amount: " + Money.format(BankConfig.MAX_LOAN_AMOUNT)
                + " | Max tenure: " + BankConfig.MAX_LOAN_TENURE_MONTHS + " months"
                + " | Interest: " + BankConfig.LOAN_MONTHLY_RATE.multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString()
                + "% per month");
        BigDecimal amount = in.readAmount("Loan amount: NGN ");
        int tenure = in.readInt("Tenure in months (1-" + BankConfig.MAX_LOAN_TENURE_MONTHS + "): ",
                1, BankConfig.MAX_LOAN_TENURE_MONTHS);
        loanService.validateOffer(customer, amount, tenure);

        Account target = chooseNonLoanAccount("Select the account to receive the loan");
        BigDecimal interest = LoanService.calculateInterest(amount, tenure);
        BigDecimal total = amount.add(interest);
        System.out.println("\n--- Loan offer ---");
        System.out.println("Principal       : " + Money.format(amount));
        System.out.println("Interest        : " + Money.format(interest));
        System.out.println("Total repayable : " + Money.format(total));
        System.out.println("Monthly instal. : " + Money.format(total.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP)));
        System.out.println("Disburse to     : " + target.getAccountNumber() + " (" + target.getType() + ")");
        if (!in.confirm("Accept this offer?")) {
            System.out.println("Loan application cancelled.");
            return;
        }
        confirmPin();
        LoanAccount loan = loanService.apply(customer, amount, tenure, target);
        System.out.println("\n--- Loan approved ---");
        System.out.println("Loan account generated : " + loan.getAccountNumber());
        System.out.println("Amount credited to     : " + target.getAccountNumber());
        System.out.println("New balance there      : " + Money.format(target.getBalance()));
        System.out.println("Outstanding loan       : " + Money.format(loan.getBalance()));
    }

    private void repayLoan() {
        LoanAccount loan = customer.getActiveLoan();
        if (loan == null) throw new BankException("You have no active loan.");
        Account funding = active.getType() == AccountType.LOAN
                ? chooseNonLoanAccount("Select the account to pay from") : active;
        System.out.println("Outstanding loan: " + Money.format(loan.getBalance()));
        System.out.println("Paying from     : " + funding.getAccountNumber() + " (balance " + Money.format(funding.getBalance()) + ")");
        BigDecimal amount = in.readAmount("Repayment amount: NGN ");
        confirmPin();
        printReceipt(loanService.repayFromAccount(funding, loan, amount));
        System.out.println("Remaining loan : " + Money.format(loan.getBalance()));
    }

    // ---------- helpers ----------

    private Account chooseNonLoanAccount(String prompt) {
        List<Account> options = new ArrayList<>();
        for (Account a : customer.getAccounts()) {
            if (a.getType() != AccountType.LOAN) options.add(a);
        }
        if (options.size() == 1) return options.get(0);
        System.out.println(prompt + ":");
        for (int i = 0; i < options.size(); i++) {
            Account a = options.get(i);
            System.out.println((i + 1) + ". " + a.getAccountNumber() + " | " + a.getType() + " | " + Money.format(a.getBalance()));
        }
        return options.get(in.readInt("Choice: ", 1, options.size()) - 1);
    }

    private void confirmPin() {
        for (int attempt = 1; attempt <= BankConfig.MAX_PIN_ATTEMPTS; attempt++) {
            String pin = in.readLine("Enter your 4-digit PIN: ");
            if (customerService.verifyPin(customer, pin)) return;
            System.out.println("  [!] Incorrect PIN (attempt " + attempt + " of " + BankConfig.MAX_PIN_ATTEMPTS + ").");
        }
        throw new BankException("Too many incorrect PIN attempts. Transaction cancelled.");
    }
}