package org.tensorflow.lite.examples.recommendation;

import org.tensorflow.lite.examples.recommendation.data.MovieItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItemDetailsWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<MovieItem> itemDetails;

    public ItemDetailsWrapper(List<MovieItem> items) {
        this.itemDetails = items;
    }

    public List<MovieItem> getItemDetails() {
        return itemDetails;
    }
}
