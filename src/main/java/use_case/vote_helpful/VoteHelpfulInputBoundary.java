package use_case.vote_helpful;

/** The input boundary for the Vote Review Helpful use case. */
public interface VoteHelpfulInputBoundary {

    /**
     * Toggles the user's helpful vote: adds it if absent, removes it (undo) if present.
     * @param input the review and the voting user
     */
    void toggle(VoteHelpfulInputData input);
}
