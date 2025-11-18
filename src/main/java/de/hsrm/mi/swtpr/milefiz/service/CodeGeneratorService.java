package de.hsrm.mi.swtpr.milefiz.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class CodeGeneratorService {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-";
    private final SecureRandom random = new SecureRandom();

    public String generateCode() {
        StringBuilder b = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            b.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return b.toString();
    }
}
