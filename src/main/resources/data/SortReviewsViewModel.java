package data;

import entity.Review;
import interface_adapter.common.ViewModel;

import java.util.ArrayList;

public class SortReviewsViewModel extends ViewModel<SortReviewsViewModel.State> {
    public SortReviewsViewModel() {
        super(new SortReviewsViewModel.State("", "", "", 0, 0, 0, new ArrayList<Review>()));
    }

    public record State(String id,
                        String name,
                        String description,
                        double averageRating,
                        double averageCleanliness,
                        int reviewCount,
                        ArrayList<Review> reviews){
        public State{

        }
    }

}
