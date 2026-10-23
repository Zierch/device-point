package com.zierch.devicepoint;

import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2 hashing for the local {@code users} table, so no password is ever stored in
 * plain text and comparison is constant-time.
 *
 * <p>Stored format: {@code <algorithm>$<iterations>$<base64 salt>$<base64 hash>}
 *
 * <p>{@code PBKDF2WithHmacSHA256} only exists from API 26 on, so on older devices the
 * hasher falls back to {@code PBKDF2WithHmacSHA1}; the algorithm name travels with the
 * stored hash, which keeps verification correct on either.
 *
 * <p>ponytail: 120k iterations keeps a login under a few hundred ms on an API 24
 * device. Raise {@link #ITERATIONS} when only recent hardware has to be supported.
 */
final class PasswordHasher {

    private static final String SHA256 = "PBKDF2WithHmacSHA256";
    private static final String SHA1 = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private PasswordHasher() {
    }

    static String hash(String password) {
        String algorithm = prefersSha256() ? SHA256 : SHA1;
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(algorithm, password, salt, ITERATIONS);
        return algorithm + "$" + ITERATIONS + "$" + encode(salt) + "$" + encode(hash);
    }

    static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = decode(parts[2]);
            byte[] expected = decode(parts[3]);
            return MessageDigest.isEqual(expected, derive(parts[0], password, salt, iterations));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean prefersSha256() {
        try {
            SecretKeyFactory.getInstance(SHA256);
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    private static byte[] derive(String algorithm, String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
            return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 unavailable: " + algorithm, e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static byte[] decode(String value) {
        return Base64.decode(value, Base64.NO_WRAP);
    }
}
