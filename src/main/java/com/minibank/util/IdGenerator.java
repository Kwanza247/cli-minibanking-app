package com.minibank.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class IdGenerator {
    private IdGenerator() {}
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicInteger TXN_COUNTER = new AtomicInteger();
    private static final AtomicInteger CUSTOMER_COUNTER = new AtomicInteger();
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    //generate a unique 10 digot account number
    public static String newAccountNumber(Predicate<String> alreadyExists) {
        String number;
        do {
            number = String.format("%010d", Math.floorMod(RANDOM.nextLong(),10_000_000_000L));
        } while (alreadyExists.test(number));
        return number;
    }

    /*e.g TXN20261002101530-0001*/
    public static String newTransactionId(){
        return "TXN" + LocalDateTime.now().format(TS) + "-" + String.format("%04d", TXN_COUNTER.incrementAndGet());
    }
    public static String newCustomerId(){
        return "CUS" + String.format("%05d", CUSTOMER_COUNTER.incrementAndGet());
    }
}
