import interface_adapter.view_reviews.ReviewsViewModel;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import use_case.view_reviews.ViewReviewsOutputData;
import use_case.vote_helpful.HelpfulVoteDataAccessInterface;
import use_case.vote_helpful.ReviewScorer;
import use_case.vote_helpful.VoteHelpfulInputBoundary;
import use_case.vote_helpful.VoteHelpfulInputData;
import use_case.vote_helpful.VoteHelpfulInteractor;

final class VoteHelpfulInteractorTest {

    static void run() {
        voteTogglesThroughGateway();
        viewModelPreservesOrderWhenUpdatingHelpfulVote();
        scoreRanksRecencyOverRawVotes();
    }

    private static void voteTogglesThroughGateway() {
        final Set<String> voted = new HashSet<>();
        final HelpfulVoteDataAccessInterface votes = new HelpfulVoteDataAccessInterface() {
            @Override
            public boolean hasVoted(final String r, final String u) {
                return voted.contains(r + "|" + u);
            }

            @Override
            public void addVote(final String r, final String u) {
                voted.add(r + "|" + u);
            }

            @Override
            public void removeVote(final String r, final String u) {
                voted.remove(r + "|" + u);
            }
        };
        final VoteHelpfulInputBoundary interactor = new VoteHelpfulInteractor(votes);

        interactor.toggle(new VoteHelpfulInputData("r1", "user1"));
        TestSupport.check(votes.hasVoted("r1", "user1"), "vote added when the user has not voted yet");

        interactor.toggle(new VoteHelpfulInputData("r1", "user1"));
        TestSupport.check(!votes.hasVoted("r1", "user1"), "vote removed when the user had already voted");
    }

    private static void viewModelPreservesOrderWhenUpdatingHelpfulVote() {
        final ReviewsViewModel model = new ReviewsViewModel();
        final ViewReviewsOutputData.ReviewDisplay first = new ViewReviewsOutputData.ReviewDisplay(
            "first", 4, "first review", 10, LocalDate.now(), "author-a", false, false);
        final ViewReviewsOutputData.ReviewDisplay second = new ViewReviewsOutputData.ReviewDisplay(
            "second", 5, "second review", 2, LocalDate.now(), "author-b", false, false);
        model.setState(new ReviewsViewModel.State("washroom", "Building", "Floor", 4.5, 4.5,
            2, 2, 2, List.of(first, second), null));

        model.toggleHelpfulVote("second");

        final List<ViewReviewsOutputData.ReviewDisplay> updated = model
            .getState()
            .reviews();
        TestSupport.check(updated
                .get(0)
                .reviewId()
                .equals("first") && updated
                .get(1)
                .reviewId()
                .equals("second"),
            "a helpful vote must preserve the current review order");
        TestSupport.check(updated
                .get(1)
                .helpfulCount() == 3 && updated
                .get(1)
                .votedByCurrentUser(),
            "a helpful vote should update only the clicked review");
    }

    private static void scoreRanksRecencyOverRawVotes() {
        final double recentFewVotes = ReviewScorer.score(12, 2);
        final double oldManyVotes = ReviewScorer.score(40, 300);
        TestSupport.check(recentFewVotes > oldManyVotes, "recency lifts a newer review above an older popular one");
    }
}
