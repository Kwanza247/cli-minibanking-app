package com.minibank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PinHasher {
    private PinHasher(){}

    public static String hash(String pin){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(("minibank:"+ pin).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(byte b:bytes)sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String pin, String hash){
        return hash(pin).equals(hash);
    }
}
