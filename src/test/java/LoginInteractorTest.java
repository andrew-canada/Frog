import entity.User;
import use_case.gateway.UserDataAccessInterface;
import use_case.login.*;
import java.util.Optional;

final class LoginInteractorTest {
    static void run(){class Fake implements UserDataAccessInterface{User current;User saved=new User("demo",Passwords.hash("secret"));public Optional<User> get(String n){return Optional.of(saved);}public boolean existsByName(String n){return true;}public void save(User u){}public void setCurrentUser(User u){current=u;}public Optional<User> getCurrentUser(){return Optional.ofNullable(current);}}
        Fake fake=new Fake();final LoginOutputData[] out=new LoginOutputData[1];new LoginInteractor(fake,d->out[0]=d).execute(new LoginInputData("demo","secret"));
        TestSupport.check(out[0].success()&&fake.current!=null,"valid login should set current user");}
}
