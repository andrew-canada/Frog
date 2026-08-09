package use_case.port;

/** Password hashing boundary; cryptographic library choice remains outside use cases. */
public interface PasswordHasher {
    String hash(String password);

    boolean matches(String password, String storedHash);

    boolean isCurrentHash(String storedHash);
}
