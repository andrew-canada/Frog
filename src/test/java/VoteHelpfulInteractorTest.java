import use_case.vote_helpful.*;

import java.util.HashSet;
import java.util.Set;

final class VoteHelpfulInteractorTest {

    static void run() {
        voteTogglesThroughGateway();
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

    private static void scoreRanksRecencyOverRawVotes() {
        double recentFewVotes = ReviewScorer.score(12, 2);
        double oldManyVotes = ReviewScorer.score(40, 300);
        TestSupport.check(recentFewVotes > oldManyVotes, "recency lifts a newer review above an older popular one");
    }
}
