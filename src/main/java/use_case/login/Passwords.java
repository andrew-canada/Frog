package use_case.login;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * JDK-only salted password hashing; MongoDB stores only the encoded PBKDF2 result.
 */
public final class Passwords {
    private static final String FIELD__ = ":";
    private static final int MAGIC_16 = 16;
    private static final int ITERATIONS = 120_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Passwords() {
    }

    /**
     * Performs this operation.
     *
     * @param raw parameter value.
     *
     * @return the operation result.
     * @throws IllegalArgumentException if the raw password is empty.
     */
    public static String hash(final String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        final byte[] salt = new byte[MAGIC_16];
        RANDOM.nextBytes(salt);
        return ITERATIONS + FIELD__ + Base64
            .getEncoder()
            .encodeToString(salt) + FIELD__ + Base64
            .getEncoder()
            .encodeToString(derive(raw, salt, ITERATIONS));
    }

    /**
     * Performs this operation.
     *
     * @param raw parameter value.
     *
     * @param encoded parameter value.
     *
     * @return the operation result.
     */
    public static boolean matches(final String raw, final String encoded) {
        boolean result = false;
        if (raw == null || encoded == null) {
            result = false;
        }
        else {
            try {
            final String[] parts = encoded.split(FIELD__, 3);
            final int rounds = Integer.parseInt(parts[0]);
            final byte[] salt = Base64
                .getDecoder()
                .decode(parts[1]);
            final byte[] expected = Base64
                .getDecoder()
                .decode(parts[2]);
                result = MessageDigest.isEqual(expected, derive(raw, salt, rounds));
            }
            catch (final RuntimeException malformed) {
                result = false;
            }
        }
        return result;
    }

    private static byte[] derive(final String raw, final byte[] salt, final int rounds) {
        final PBEKeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, rounds, 256);
        try {
            return SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();
        }
        catch (final Exception unavailable) {
            throw new IllegalStateException("PBKDF2 is unavailable", unavailable);
        }
        finally {
            spec.clearPassword();
        }
    }
}
