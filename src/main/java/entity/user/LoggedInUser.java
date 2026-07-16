package entity.user;

public class LoggedInUser implements User{

    private final String name;
    private final String password;
    public LoggedInUser(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.name;
    }

}
