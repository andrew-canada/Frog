package use_case.sort_washrooms;

import java.util.List;

import entity.Washroom;

public record SortWashroomsOutputData(boolean success, List<Washroom> washrooms, double latitude, double longitude) {
    public SortWashroomsOutputData {
        washrooms = List.copyOf(washrooms);
    }
}
