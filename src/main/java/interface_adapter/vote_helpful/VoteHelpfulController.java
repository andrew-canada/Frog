package interface_adapter.vote_helpful;

import use_case.vote_helpful.VoteHelpfulInputBoundary;
import use_case.vote_helpful.VoteHelpfulInputData;

/**
 * Controller for the Vote Review Helpful use case.
 */
public final class VoteHelpfulController {

    private final VoteHelpfulInputBoundary interactor;

    public VoteHelpfulController(VoteHelpfulInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void toggle(String reviewId, String username) {
        interactor.toggle(new VoteHelpfulInputData(reviewId, username));
    }
}
