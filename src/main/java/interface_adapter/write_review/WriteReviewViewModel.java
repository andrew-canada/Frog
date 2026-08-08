package interface_adapter.write_review;

import interface_adapter.common.ViewModel;

public final class WriteReviewViewModel extends ViewModel<WriteReviewViewModel.State> {
    public WriteReviewViewModel() {
        super(new State(false, ""));
    }

    public record State(boolean success, String message) {
    }
}
