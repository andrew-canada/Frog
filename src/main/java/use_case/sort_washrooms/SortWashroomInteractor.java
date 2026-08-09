package use_case.sort_washrooms;

import data_access.washroom.WashroomDataAccessInterface;
import entity.Washroom;

import java.util.ArrayList;
import java.util.Comparator;

public class SortWashroomInteractor implements SortWashroomsInputBoundary {
    private final WashroomDataAccessInterface washroomDAO;
    private final SortWashroomsOutputBoundary presenter;

    public SortWashroomInteractor(WashroomDataAccessInterface washroomDAO,
                                  SortWashroomsOutputBoundary presenter) {
        this.washroomDAO = washroomDAO;
        this.presenter = presenter;
    }

    private static double distance(double a, double b, double c, double d) {
        double x = Math.toRadians(d - b) * Math.cos(Math.toRadians((a + c) / 2));
        double y = Math.toRadians(c - a);
        return Math.sqrt(x * x + y * y) * 6_371_000;
    }

    @Override
    public void execute(SortWashroomInputData inputData) {
        String sortingOrder = inputData.sortBy();
        Comparator<entity.Washroom> comparator;
        if (sortingOrder.equals("Highest Rated")) {
            comparator = Comparator.comparing(
                    (entity.Washroom washroom) ->
                            washroom.reviewSummary().averageRating()).reversed();
        } else if (sortingOrder.equals("Nearest")) {
            comparator = Comparator.comparing(
                    (entity.Washroom washroom) ->
                            distance(inputData.lat(),
                                    inputData.lng(),
                                    washroom.building().latitude(),
                                    washroom.building().longitude())
            );
        } else {
            comparator = Comparator.comparing(
                    (entity.Washroom washroom) ->
                            washroom.name());
        }
        ArrayList<Washroom> sortedWashroom = new ArrayList<>();
        for (String id : inputData.washroomIdList()) {
            sortedWashroom.add(washroomDAO.getById(id).get());
        }
        sortedWashroom.sort(comparator);
        presenter.present(new SortWashroomsOutputData(
                true,
                sortedWashroom,
                inputData.lat(),
                inputData.lng()));
    }

}