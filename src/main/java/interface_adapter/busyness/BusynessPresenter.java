package interface_adapter.busyness;

import use_case.busyness.BusynessStatsOutputBoundary;
import use_case.busyness.BusynessStatsOutputData;

public final class BusynessPresenter implements BusynessStatsOutputBoundary {
    private final BusynessViewModel model;

    public BusynessPresenter(BusynessViewModel model) {
        this.model = model;
    }

    @Override
    public void present(BusynessStatsOutputData d) {
        model.setState(new BusynessViewModel.State(d.buckets(), d.sourceNote()));
    }
}
