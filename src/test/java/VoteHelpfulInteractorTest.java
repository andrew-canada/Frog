import interface_adapter.view_reviews.ReviewsViewModel;
import use_case.vote_helpful.*;
import use_case.view_reviews.ViewReviewsOutputData;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class VoteHelpfulInteractorTest {

    static void run() {
        voteTogglesThroughGateway();
        viewModelPreservesOrderWhenUpdatingHelpfulVote();
        scoreRanksRecencyOverRawVotes();
    }

    private static void voteTogglesThroughGateway() {
        final Set<String> voted = new HashSet<>();
        HelpfulVoteDataAccessInterface votes = new HelpfulVoteDataAccessInterface() {
            public boolean hasVoted(String r, String u) { return voted.contains(r + "|" + u); }
            public void addVote(String r, String u) { voted.add(r + "|" + u); }
            public void removeVote(String r, String u) { voted.remove(r + "|" + u); }
        };
        VoteHelpfulInputBoundary interactor = new VoteHelpfulInteractor(votes);

        interactor.toggle(new VoteHelpfulInputData("r1", "user1"));
        TestSupport.check(votes.hasVoted("r1", "user1"), "vote added when the user has not voted yet");

        interactor.toggle(new VoteHelpfulInputData("r1", "user1"));
        TestSupport.check(!votes.hasVoted("r1", "user1"), "vote removed when the user had already voted");
    }

    private static void viewModelPreservesOrderWhenUpdatingHelpfulVote() {
        ReviewsViewModel model = new ReviewsViewModel();
        ViewReviewsOutputData.ReviewDisplay first = new ViewReviewsOutputData.ReviewDisplay(
                "first", 4, "first review", 10, LocalDate.now(), "author-a", false, false);
        ViewReviewsOutputData.ReviewDisplay second = new ViewReviewsOutputData.ReviewDisplay(
                "second", 5, "second review", 2, LocalDate.now(), "author-b", false, false);
        model.setState(new ReviewsViewModel.State("washroom", "Building", "Floor", 4.5, 4.5,
                2, 2, 2, List.of(first, second), null));

        model.toggleHelpfulVote("second");

        List<ViewReviewsOutputData.ReviewDisplay> updated = model.getState().reviews();
        TestSupport.check(updated.get(0).reviewId().equals("first") && updated.get(1).reviewId().equals("second"),
                "a helpful vote must preserve the current review order");
        TestSupport.check(updated.get(1).helpfulCount() == 3 && updated.get(1).votedByCurrentUser(),
                "a helpful vote should update only the clicked review");
    }

    private static void scoreRanksRecencyOverRawVotes() {
        double recentFewVotes = ReviewScorer.score(12, 2);
        double oldManyVotes = ReviewScorer.score(40, 300);
        TestSupport.check(recentFewVotes > oldManyVotes, "recency lifts a newer review above an older popular one");
    }
}
