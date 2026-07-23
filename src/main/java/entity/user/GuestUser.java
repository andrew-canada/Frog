package entity.user;

public class GuestUser implements User {
    private final String name;

    public GuestUser(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
