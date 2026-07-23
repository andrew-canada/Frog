package use_case.busyness;
import java.time.DayOfWeek;
public record BusynessStatsInputData(String washroomId, String buildingCode, DayOfWeek dayOfWeek) { }
