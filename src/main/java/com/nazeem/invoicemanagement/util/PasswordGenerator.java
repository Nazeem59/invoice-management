package com.nazeem.invoicemanagement.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder(12);

        String rawPassword = "Finance123";

        String hashedPassword =
                encoder.encode(rawPassword);

        System.out.println("Raw Password: " + rawPassword);
        System.out.println("BCrypt Hash: " + hashedPassword);

        System.out.println(
                "Password Valid: " +
                        encoder.matches(rawPassword, hashedPassword)
        );
    }
}
