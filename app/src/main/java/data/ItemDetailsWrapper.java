package data;

import java.io.Serializable;
import java.util.List;

public class ItemDetailsWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<MovieItem> itemDetails;

    public ItemDetailsWrapper(List<MovieItem> items) {
        this.itemDetails = items;
    }

    public List<MovieItem> getItemDetails() {
        return itemDetails;
    }
}
