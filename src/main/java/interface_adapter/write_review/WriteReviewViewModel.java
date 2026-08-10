package interface_adapter.write_review;

import interface_adapter.common.AbstractViewModel;

public final class WriteReviewViewModel extends AbstractViewModel<WriteReviewViewModel.State> {
    public WriteReviewViewModel() {
        super(new State(false, ""));
    }

    public record State(boolean success, String message) {
    }
}
