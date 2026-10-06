package com.minibank.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {
    private final String id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String nin;
    private final String bvn;
    private final String address;
    private final String pinHash;
    private final List<Account> accounts = new ArrayList<>();


    public Customer(String id, String fullName, String email, String phone, String nin, String bvn, String address,
            String pinHash) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.nin = nin;
        this.bvn = bvn;
        this.address = address;
        this.pinHash = pinHash;
    }


    public String getId() { return id; }
    public String getFullName() {return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getNin() { return nin; }
    public String getBvn() { return bvn; }
    public String getAddress() { return address; }
    public String getPinHash() { return pinHash; }
    public List<Account> getAccounts() { return Collections.unmodifiableList(accounts); }

    public void addAccount(Account account) { accounts.add(account); }

    public LoanAccount getActiveLoan() {
        for (Account a: accounts ){
            if(a instanceof LoanAccount && ((LoanAccount) a).isActive())
                return (LoanAccount) a;
        }
        return null;
    }
    public boolean hasActiveLoan() { return getActiveLoan() != null; }
}
