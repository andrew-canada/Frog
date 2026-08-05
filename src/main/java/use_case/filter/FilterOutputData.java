package use_case.filter;

import entity.Washroom;

import java.util.List;

public record FilterOutputData (boolean success, List<Washroom> washrooms) {
}
