package use_case.sort_washrooms;

import java.util.List;

public record SortWashroomInputData(String sortBy, List<String> washroomIdList, double lat, double lng) {
}
