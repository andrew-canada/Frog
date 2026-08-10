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
        final boolean result;
        if (password == null || storedHash == null || storedHash.isBlank()) {
            result = false;
        }
        else if (isCurrentHash(storedHash)) {
            boolean currentHashMatches;
            try {
                currentHashMatches = BCrypt.checkpw(password, storedHash);
            }
            catch (final IllegalArgumentException malformedHash) {
                currentHashMatches = false;
            }
            result = currentHashMatches;
        }
        else if (storedHash.matches("\\d+:.*")) {
            result = Passwords.matches(password, storedHash);
        }
        else {
            result = MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
        }
        return result;
    }

    @Override
    public boolean isCurrentHash(final String storedHash) {
        return storedHash != null && storedHash.startsWith("$2");
    }
}
