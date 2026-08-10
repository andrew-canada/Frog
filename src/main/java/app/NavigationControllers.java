package app;

import interface_adapter.directions.DirectionsController;
import interface_adapter.directions.DirectionsPresenter;
import interface_adapter.filter.FilterController;
import interface_adapter.filter.FilterPresenter;
import interface_adapter.sort_washrooms.SortWashroomController;
import interface_adapter.sort_washrooms.SortWashroomPresenter;
import use_case.directions.GetDirectionsInteractor;
import use_case.filter.FilterInteractor;
import use_case.sort_washrooms.SortWashroomInteractor;
import views.SwingUiDispatcher;

/** Map, filtering, and washroom-sorting controller construction. */
final class NavigationControllers {
    private final DirectionsController directions;
    private final FilterController filter;
    private final SortWashroomController sortWashroom;

    private NavigationControllers(final DirectionsController directions, final FilterController filter,
                                  final SortWashroomController sortWashroom) {
        this.directions = directions;
        this.filter = filter;
        this.sortWashroom = sortWashroom;
    }

    static NavigationControllers create(final ApplicationInfrastructure infrastructure, final FeatureModels features) {
        final SwingUiDispatcher ui = new SwingUiDispatcher();
        return new NavigationControllers(
            new DirectionsController(new GetDirectionsInteractor(infrastructure.washrooms(), infrastructure.routes(),
                new DirectionsPresenter(features.map(), ui))),
            new FilterController(new FilterInteractor(infrastructure.washrooms(), infrastructure.reviews(),
                infrastructure.reports(), infrastructure.users(),
                new FilterPresenter(features.filter(), features.list(), ui), ApplicationNames.values())),
            new SortWashroomController(new SortWashroomInteractor(infrastructure.washrooms(),
                new SortWashroomPresenter(features.list(), features.sortWashroom(), ui))));
    }

    DirectionsController directions() {
        return directions;
    }

    FilterController filter() {
        return filter;
    }

    SortWashroomController sortWashroom() {
        return sortWashroom;
    }
}
