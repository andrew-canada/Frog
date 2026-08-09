package database.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.mindrot.jbcrypt.BCrypt;

import use_case.login.Passwords;
import use_case.port.PasswordHasher;

/**
 * BCrypt adapter that also verifies hashes from previous application versions.
 */
public final class BCryptPasswordHasher implements PasswordHasher {
    @Override
    public String hash(final String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public boolean matches(final String password, final String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        if (isCurrentHash(storedHash)) {
            try {
                return BCrypt.checkpw(password, storedHash);
            }
            catch (final IllegalArgumentException malformedHash) {
                return false;
            }
        }
        if (storedHash.matches("\\d+:.*")) {
            return Passwords.matches(password, storedHash);
        }
        return MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
            storedHash.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isCurrentHash(final String storedHash) {
        return storedHash != null && storedHash.startsWith("$2");
    }
}
