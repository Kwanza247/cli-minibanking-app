package com.minibank.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;
import java.util.function.Predicate;

public class ConsoleInput {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Keeps asking until the validator accepts the input. */
    public String readValid(String prompt, Predicate<String> validator, String errorMessage) {
        while (true) {
            String value = readLine(prompt);
            if (validator.test(value)) return value;
            System.out.println(" [!] " + errorMessage);
        }
    }

    public int readInt(String prompt, int min, int max) {
        while(true){
            String value = readLine(prompt);
            try{
                int n = Integer.parseInt(value);
                if (n >= min && n <= max) return n;
            } catch(NumberFormatException ignored) { }
            System.out.println("[!] Enter a numbber between " + min + " and " + max + "."); 
        }
    }

    public BigDecimal readAmount(String prompt) {
        while(true){
            String value = readLine(prompt).replace(",", "");
            try {
                BigDecimal amount = new BigDecimal(value);
                if(amount.signum() > 0 && amount.scale() <=2) {
                    return amount.setScale(2, RoundingMode.UNNECESSARY);
                }
            } catch(NumberFormatException ignored) { }
            System.out.println("[!] Entet a valid amount greater than 0 (max 2 decimal places).");
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String v = readLine(prompt + " (y/n): ").toLowerCase();
            if(v.equals("y") || v.equals("yes")) return true;
            if(v.equals("n") || v.equals("no")) return false;
        }
    }
}
