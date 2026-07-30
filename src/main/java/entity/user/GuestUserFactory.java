package entity.user;

public class GuestUserFactory {
    public User create(String name) {
        return new GuestUser(name);
    }
}
