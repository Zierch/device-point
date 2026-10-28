package com.zierch.devicepoint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PasswordHasherTest {

    @Test
    public void hashAndVerifyRoundTrip() {
        String stored = PasswordHasher.hash("admin123");

        assertTrue(PasswordHasher.verify("admin123", stored));
        assertFalse(PasswordHasher.verify("admin124", stored));
    }

    @Test
    public void storedValueDoesNotContainThePassword() {
        String stored = PasswordHasher.hash("rahasia123");

        assertFalse(stored.contains("rahasia123"));
        assertTrue(stored.startsWith("PBKDF2WithHmac"));
        assertTrue(stored.split("\\$").length == 4);
    }

    @Test
    public void everyHashUsesAFreshSalt() {
        assertNotEquals(PasswordHasher.hash("admin123"), PasswordHasher.hash("admin123"));
    }

    @Test
    public void tamperedHashIsRejected() {
        String[] parts = PasswordHasher.hash("admin123").split("\\$");
        String tampered = parts[0] + "$" + parts[1] + "$" + parts[2] + "$"
                + (parts[3].charAt(0) == 'A' ? 'B' : 'A') + parts[3].substring(1);

        assertFalse(PasswordHasher.verify("admin123", tampered));
    }

    @Test
    public void unsupportedAlgorithmIsRejected() {
        String stored = PasswordHasher.hash("admin123");

        assertFalse(PasswordHasher.verify("admin123", stored.replaceFirst("^[^$]+", "PBKDF2WithHmacMD5")));
    }

    @Test
    public void malformedOrMissingHashIsRejected() {
        assertFalse(PasswordHasher.verify("admin123", null));
        assertFalse(PasswordHasher.verify("admin123", ""));
        assertFalse(PasswordHasher.verify("admin123", "bukan-format-hash"));
        assertFalse(PasswordHasher.verify(null, PasswordHasher.hash("admin123")));
    }
}
