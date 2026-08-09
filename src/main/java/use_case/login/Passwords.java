package use_case.login;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JDK-only salted password hashing; MongoDB stores only the encoded PBKDF2 result.
 */
public final class Passwords {
    private static final int ITERATIONS = 120_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Passwords() {
    }

    public static String hash(final String raw) {
        if (raw == null || raw.isEmpty()) throw new IllegalArgumentException("Password is required");
        final byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return ITERATIONS + ":" + Base64
            .getEncoder()
            .encodeToString(salt) + ":"
            + Base64
            .getEncoder()
            .encodeToString(derive(raw, salt, ITERATIONS));
    }

    public static boolean matches(final String raw, final String encoded) {
        if (raw == null || encoded == null) return false;
        try {
            final String[] parts = encoded.split(":", 3);
            final int rounds = Integer.parseInt(parts[0]);
            final byte[] salt = Base64
                .getDecoder()
                .decode(parts[1]);
            final byte[] expected = Base64
                .getDecoder()
                .decode(parts[2]);
            return MessageDigest.isEqual(expected, derive(raw, salt, rounds));
        } catch (final RuntimeException malformed) {
            return false;
        }
    }

    private static byte[] derive(final String raw, final byte[] salt, final int rounds) {
        final PBEKeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, rounds, 256);
        try {
            return SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();
        } catch (final Exception unavailable) {
            throw new IllegalStateException("PBKDF2 is unavailable", unavailable);
        } finally {
            spec.clearPassword();
        }
    }
}
