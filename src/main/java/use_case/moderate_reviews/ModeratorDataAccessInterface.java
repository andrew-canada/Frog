package use_case.moderate_reviews;

/**
 * Authorization gateway for moderation: answers whether a user is allowed to
 * moderate reviews. Implemented by the user data access objects.
 */
public interface ModeratorDataAccessInterface {

    /**
     * @return whether the named user has moderator privileges.
     */
    boolean isModerator(String username);
}
