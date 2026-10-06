package com.minibank.service;

import com.minibank.exception.BankException;
import com.minibank.model.*;
import com.minibank.repository.BankRepository;
import com.minibank.util.IdGenerator;
import com.minibank.util.PinHasher;
import com.minibank.util.Validator;

public class CustomerService {
    private final BankRepository repo;

    public CustomerService(BankRepository repo) {
        this.repo = repo;
    }

    public Customer register(String fullName, String email, String phone, String nin, String bvn,
                             String address, String pin, AccountType firstAccountType) {
        if (!Validator.isValidFullName(fullName)) throw new BankException("Invalid full name.");
        if (!Validator.isValidEmail(email)) throw new BankException("Invalid email address.");
        if (!Validator.isValidPhone(phone)) throw new BankException("Invalid phone number.");
        if (!Validator.isValidNin(nin)) throw new BankException("NIN must be exactly 11 digits.");
        if (!Validator.isValidBvn(bvn)) throw new BankException("BVN must be exactly 11 digits.");
        if (!Validator.isValidAddress(address)) throw new BankException("Invalid address.");
        if (!Validator.isValidPin(pin)) throw new BankException("PIN must be exactly 4 digits.");

        if (repo.phoneExists(phone)) throw new BankException("A customer with this phone number already exists.");
        if (repo.ninExists(nin)) throw new BankException("A customer with this NIN already exists.");
        if (repo.bvnExists(bvn)) throw new BankException("A customer with this BVN already exists.");

        String cleanEmail = (email == null || email.trim().isEmpty()) ? null : email.trim();
        Customer customer = new Customer(IdGenerator.newCustomerId(), fullName.trim(), cleanEmail,
                phone, nin, bvn, address.trim(), PinHasher.hash(pin));
        repo.saveCustomer(customer);
        openAccount(customer, firstAccountType);
        return customer;
    }

    /** Opens a savings or current account. Loan accounts are created through LoanService. */
    public Account openAccount(Customer customer, AccountType type) {
        String number = IdGenerator.newAccountNumber(repo::accountExists);
        Account account;
        if (type == AccountType.SAVINGS) {
            account = new SavingsAccount(number, customer.getFullName());
        } else if (type == AccountType.CURRENT) {
            account = new CurrentAccount(number, customer.getFullName());
        } else {
            throw new BankException("Loan accounts are created by applying for a loan.");
        }
        customer.addAccount(account);
        repo.saveAccount(account);
        return account;
    }

    public Customer authenticate(String phone, String pin) {
        Customer customer = repo.findByPhone(phone);
        if (customer == null || !PinHasher.matches(pin, customer.getPinHash())) {
            throw new BankException("Invalid phone number or PIN.");
        }
        return customer;
    }

    public boolean verifyPin(Customer customer, String pin) {
        return Validator.isValidPin(pin) && PinHasher.matches(pin, customer.getPinHash());
    }
}