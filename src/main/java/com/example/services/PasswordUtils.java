package com.example.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.util.Random;

public class PasswordUtils {
    public static String randomPwd(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public static String hash(String pwd) {
        return BCrypt.withDefaults().hashToString(12, pwd.toCharArray());
    }
}
