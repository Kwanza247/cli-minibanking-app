package com.minibank.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.minibank.model.Account;
import com.minibank.model.Customer;

/** in memory storage. Data is lost when program exists */
public class BankRepository {
    private final Map<String, Customer> customersByPhone = new HashMap<>();
    private final Set<String> nins = new HashSet<>();
    private final Set<String> bvns = new HashSet<>();
    private final Map<String, Account> accounts = new HashMap<>();

    public void saveCustomer(Customer c) {
        customersByPhone.put(c.getPhone(), c);
        nins.add(c.getNin());
        bvns.add(c.getBvn());
    }

    public Customer findByPhone(String phone) { return customersByPhone.get(phone); }
    public boolean phoneExists(String phone) { return customersByPhone.containsKey(phone); }
    public boolean ninExists(String nin) { return nins.contains(nin); }
    public boolean bvnExists(String bvn) { return bvns.contains(bvn); }

    public void saveAccount(Account a) { accounts.put(a.getAccountNumber(), a); }
    public Account findAccount(String number) { return accounts.get(number); }
    public boolean accountExists(String number) { return accounts.containsKey(number); }
}
