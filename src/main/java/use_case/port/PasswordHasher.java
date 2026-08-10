package use_case.port;

/**
 * Password hashing boundary; cryptographic library choice remains outside use cases.
 */
public interface PasswordHasher {
    /**
     * Performs this operation.
     *
     * @param password parameter value.
     *
     * @return the operation result.
     */
    String hash(String password);

    /**
     * Performs this operation.
     *
     * @param password parameter value.
     *
     * @param storedHash parameter value.
     *
     * @return the operation result.
     */
    boolean matches(String password, String storedHash);

    /**
     * Performs this operation.
     *
     * @param storedHash parameter value.
     *
     * @return the operation result.
     */
    boolean isCurrentHash(String storedHash);
}
