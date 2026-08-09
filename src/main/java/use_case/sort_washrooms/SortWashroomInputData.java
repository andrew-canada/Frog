package use_case.sort_washrooms;

import java.util.List;

public record SortWashroomInputData(WashroomSortOrder sortOrder, List<String> washroomIdList, double lat, double lng) {
}
