package entity.washroom;

public class GenericWashroomFactory {

    public static GenericWashroom create(String floor) {
        return new GenericWashroom(floor);
    }
}
