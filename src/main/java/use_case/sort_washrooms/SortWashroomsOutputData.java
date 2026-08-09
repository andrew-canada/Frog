package use_case.sort_washrooms;

import entity.Washroom;

import java.util.List;

public record SortWashroomsOutputData(boolean success, List<Washroom> washrooms, double latitude, double longitude) {

}
