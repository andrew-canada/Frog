import entity.*;

final class TestSupport {
    private TestSupport() { }
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static Washroom washroom(){return new Washroom("w1","Test washroom",new Building("BA","Bahen",43.66,-79.39),"2nd",true,
            Washroom.Gender.ALL_GENDER,3,2,"By elevators",new ReviewSummary(4.5,4.0,2));}
}
