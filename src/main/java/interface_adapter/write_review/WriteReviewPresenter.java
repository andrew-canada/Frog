package interface_adapter.write_review;

import use_case.write_review.WriteReviewOutputBoundary;
import use_case.write_review.WriteReviewOutputData;

public final class WriteReviewPresenter implements WriteReviewOutputBoundary {
    private final WriteReviewViewModel model;

    public WriteReviewPresenter(WriteReviewViewModel model) {
        this.model = model;
    }

    @Override
    public void present(WriteReviewOutputData output) {
        model.setState(new WriteReviewViewModel.State(output.success(), output.message()));
    }
}
