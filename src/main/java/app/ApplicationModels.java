package app;

/** All mutable presentation state shared by the application. */
final class ApplicationModels {
    private final AuthModels auth = new AuthModels();
    private final ReviewModels reviews = new ReviewModels();
    private final FeatureModels features = new FeatureModels();

    AuthModels auth() {
        return auth;
    }

    ReviewModels reviews() {
        return reviews;
    }

    FeatureModels features() {
        return features;
    }

    String username() {
        return auth.username();
    }

    boolean loggedIn() {
        return auth.loggedInUser();
    }
}
