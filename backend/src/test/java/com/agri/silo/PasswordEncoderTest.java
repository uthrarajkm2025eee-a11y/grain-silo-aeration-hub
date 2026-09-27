package com.agri.silo;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {

    @Test
    public void generateHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String adminHash = encoder.encode("admin123");
        String accHash = encoder.encode("acc123");
        System.out.println("ADMIN_HASH: " + adminHash);
        System.out.println("ACC_HASH: " + accHash);
    }
}
