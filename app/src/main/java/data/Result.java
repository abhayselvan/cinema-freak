package data;

/**
 * An immutable result returned by a client.RecommendationClient.
 */
public class Result {

    /**
     * Predicted id.
     */
    public int id;

    /**
     * Recommended item.
     */
    public MovieItem item;

    /**
     * A sortable score for how good the result is relative to others. Higher should be better.
     */
    public float confidence;

    public Result(int id, MovieItem item, float confidence) {
        this.id = id;
        this.item = item;
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return String.format("[%d] confidence: %.3f, item: %s", id, confidence, item);
    }
}