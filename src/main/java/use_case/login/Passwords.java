package use_case.login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * JDK-only salted password hashing; MongoDB stores only the encoded PBKDF2 result.
 */
public final class Passwords {
    private static final String FIELD_SEPARATOR = ":";
    private static final int FIELD_COUNT = 3;
    private static final int SALT_LENGTH_BYTES = 16;
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
        final byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        return ITERATIONS + FIELD_SEPARATOR + Base64
            .getEncoder()
            .encodeToString(salt) + FIELD_SEPARATOR + Base64
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
            final String[] parts = encoded.split(FIELD_SEPARATOR, 3);
            if (parts.length != FIELD_COUNT) {
                result = false;
            }
            else {
                try {
                    final int rounds = Integer.parseInt(parts[0]);
                    final byte[] salt = Base64
                        .getDecoder()
                        .decode(parts[1]);
                    final byte[] expected = Base64
                        .getDecoder()
                        .decode(parts[2]);
                    result = MessageDigest.isEqual(expected, derive(raw, salt, rounds));
                }
                catch (final IllegalArgumentException malformed) {
                    result = false;
                }
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
        catch (final NoSuchAlgorithmException | InvalidKeySpecException unavailable) {
            throw new IllegalStateException("PBKDF2 is unavailable", unavailable);
        }
        finally {
            spec.clearPassword();
        }
    }
}
