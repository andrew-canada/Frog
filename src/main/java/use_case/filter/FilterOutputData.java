package use_case.filter;

import java.util.List;

import entity.Washroom;

public record FilterOutputData(boolean success, List<Washroom> washrooms, double latitude, double longitude) {
}
