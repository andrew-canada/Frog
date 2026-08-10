package app;

import interface_adapter.account.AccountViewModel;
import interface_adapter.busyness.BusynessViewModel;
import interface_adapter.directions.MapViewModel;
import interface_adapter.filter.FilterViewModel;
import interface_adapter.sort_washrooms.SortWashroomViewModel;
import interface_adapter.status_report.StatusReportViewModel;
import interface_adapter.view_reviews.WashroomListViewModel;

/** Feature state outside authentication and reviews. */
final class FeatureModels {
    private final AccountViewModel account = new AccountViewModel();
    private final StatusReportViewModel status = new StatusReportViewModel();
    private final BusynessViewModel busyness = new BusynessViewModel();
    private final MapViewModel map = new MapViewModel();
    private final WashroomListViewModel list = new WashroomListViewModel();
    private final FilterViewModel filter = new FilterViewModel();
    private final SortWashroomViewModel sortWashroom = new SortWashroomViewModel();

    AccountViewModel account() {
        return account;
    }

    StatusReportViewModel status() {
        return status;
    }

    BusynessViewModel busyness() {
        return busyness;
    }

    MapViewModel map() {
        return map;
    }

    WashroomListViewModel list() {
        return list;
    }

    FilterViewModel filter() {
        return filter;
    }

    SortWashroomViewModel sortWashroom() {
        return sortWashroom;
    }
}
