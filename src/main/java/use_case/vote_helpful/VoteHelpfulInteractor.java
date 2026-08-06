package use_case.vote_helpful;

/**
 * The interactor for the Vote Review Helpful use case.
 * <p/>
 * A command use case: it toggles the vote in the data store. The visible result
 * (updated count + green button + re-ranking) comes from re-running View Reviews,
 * which reads the new vote state and helpful count.
 */
public final class VoteHelpfulInteractor implements VoteHelpfulInputBoundary {

    private final HelpfulVoteDataAccessInterface votes;

    public VoteHelpfulInteractor(HelpfulVoteDataAccessInterface votes) {
        this.votes = votes;
    }

    @Override
    public void toggle(VoteHelpfulInputData input) {
        if (votes.hasVoted(input.reviewId(), input.username())) {
            votes.removeVote(input.reviewId(), input.username());
        } else {
            votes.addVote(input.reviewId(), input.username());
        }
    }
}
