package use_case.recommend;

public interface RecommendWashroomOutputBoundary {
    void present(RecommendWashroomOutputData outputData);

    void presentError(String message);
}
