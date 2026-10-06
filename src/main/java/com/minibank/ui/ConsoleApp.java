package com.minibank.ui;

import com.minibank.config.BankConfig;
import com.minibank.exception.BankException;
import com.minibank.model.Account;
import com.minibank.model.AccountType;
import com.minibank.model.Customer;
import com.minibank.repository.BankRepository;
import com.minibank.service.AccountService;
import com.minibank.service.CustomerService;
import com.minibank.service.LoanService;
import com.minibank.util.ConsoleInput;
import com.minibank.util.Money;
import com.minibank.util.Validator;

import java.util.NoSuchElementException;

public class ConsoleApp {
    private final ConsoleInput in = new ConsoleInput();
    private final BankRepository repo = new BankRepository();
    private final CustomerService customerService = new CustomerService(repo);
    private final AccountService accountService = new AccountService(repo);
    private final LoanService loanService = new LoanService(repo);

    public void run() {
        System.out.println("==================================================");
        System.out.println("        Welcome to " + BankConfig.BANK_NAME);
        System.out.println("==================================================");
        try {
            boolean running = true;
            while (running) {
                System.out.println("\n1. Register new customer");
                System.out.println("2. Login");
                System.out.println("0. Exit");
                int choice = in.readInt("Choose an option: ", 0, 2);
                switch (choice) {
                    case 1: register(); break;
                    case 2: login(); break;
                    default: running = false;
                }
            }
        } catch (NoSuchElementException e) {
            // input stream closed (Ctrl+D / Ctrl+Z); exit quietly
        }
        System.out.println("\nThank you for banking with us. Goodbye!");
    }

    private void register() {
        System.out.println("\n=== Customer Registration ===");
        String fullName = in.readValid("Full name: ", Validator::isValidFullName,
                "Enter first and last name (letters only).");
        String email = in.readValid("Email (optional, press Enter to skip): ", Validator::isValidEmail,
                "Invalid email format.");
        String phone = in.readValid("Phone number (11 digits, e.g. 08012345678): ", Validator::isValidPhone,
                "Phone must be 11 digits and start with 07, 08 or 09.");
        String nin = in.readValid("NIN (11 digits): ", Validator::isValidNin, "NIN must be exactly 11 digits (numbers only).");
        String bvn = in.readValid("BVN (11 digits): ", Validator::isValidBvn, "BVN must be exactly 11 digits (numbers only).");
        String address = in.readValid("Address: ", Validator::isValidAddress, "Address is too short.");

        String pin;
        while (true) {
            pin = in.readValid("Create a 4-digit PIN: ", Validator::isValidPin, "PIN must be exactly 4 digits.");
            if (pin.equals(in.readLine("Confirm PIN: "))) break;
            System.out.println("  [!] PINs do not match. Try again.");
        }

        System.out.println("Open your first account:  1. Savings   2. Current");
        AccountType type = in.readInt("Choose account type: ", 1, 2) == 1 ? AccountType.SAVINGS : AccountType.CURRENT;

        try {
            Customer c = customerService.register(fullName, email, phone, nin, bvn, address, pin, type);
            Account a = c.getAccounts().get(0);
            System.out.println("\n--- Registration successful ---");
            System.out.println("Customer ID    : " + c.getId());
            System.out.println("Name           : " + c.getFullName());
            System.out.println("Your account has been generated:");
            System.out.println("Account Number : " + a.getAccountNumber());
            System.out.println("Account Type   : " + a.getType());
            System.out.println("Balance        : " + Money.format(a.getBalance()));
            System.out.println("You can now log in with your phone number and PIN.");
        } catch (BankException e) {
            System.out.println("\n[!] Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        System.out.println("\n=== Login ===");
        for (int attempt = 1; attempt <= BankConfig.MAX_PIN_ATTEMPTS; attempt++) {
            String phone = in.readLine("Phone number: ");
            String pin = in.readLine("PIN: ");
            try {
                Customer customer = customerService.authenticate(phone, pin);
                new DashboardMenu(customer, in, customerService, accountService, loanService).run();
                return;
            } catch (BankException e) {
                System.out.println("[!] " + e.getMessage() + " (Attempt " + attempt + " of " + BankConfig.MAX_PIN_ATTEMPTS + ")");
            }
        }
        System.out.println("[!] Too many failed attempts. Returning to main menu.");
    }
}