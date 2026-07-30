package entity.user;

public class LoggedInUserFactory implements UserFactory {
    @Override
    public LoggedInUser create(String name, String password) {
        return new LoggedInUser(name, password);
    }
}