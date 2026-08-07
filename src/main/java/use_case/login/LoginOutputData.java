package use_case.login;

public record LoginOutputData(boolean success, String username, boolean moderator, String message) {
}
