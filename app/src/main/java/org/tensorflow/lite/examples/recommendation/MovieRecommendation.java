package org.tensorflow.lite.examples.recommendation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.tensorflow.lite.examples.recommendation.data.FileUtil;
import org.tensorflow.lite.examples.recommendation.data.MovieItem;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class MovieRecommendation extends AppCompatActivity implements RecommendationFragment.OnListFragmentInteractionListener, Serializable {
    private RecommendationFragment recommendationFragment;
    private Handler handler;
    private RecommendationClient client;
    private static final String TAG = "OnDeviceRecommendationDemo";
    private static final String CONFIG_PATH = "config.json";  // Default config path in assets.
    private List<MovieItem> movies;
    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_recommendation_layout);
        ItemDetailsWrapper wrap = (ItemDetailsWrapper) getIntent().getSerializableExtra("reco");
        movies = wrap.getItemDetails();
        // Load config file.
        try {
            config = FileUtil.loadConfig(getAssets(), CONFIG_PATH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading config %s: %s.", CONFIG_PATH, ex));
        }
        recommendationFragment =
                (RecommendationFragment)
                        getSupportFragmentManager().findFragmentById(R.id.recommendation_fragment);
        handler = new Handler();
        client = new RecommendationClient(this, config);
        handler.post(
                () -> {
                    client.load();
                });
        recommend(movies);


    }

    /**
     * Sends selected movie list and get recommendations.
     */
    private void recommend(final List<MovieItem> movies) {
        handler.post(
                () -> {
                    // Run inference with TF Lite.
                    Log.d(TAG, "Run inference with TFLite model.");
                    List<RecommendationClient.Result> recommendations = client.recommend(movies);

                    // Show result on screen
                    showResult(recommendations);
                });
    }

    /**
     * Shows result on the screen.
     */
    private void showResult(final List<RecommendationClient.Result> recommendations) {
        // Run on UI thread as we'll updating our app UI
        runOnUiThread(() -> recommendationFragment.setRecommendations(recommendations));
    }

    @Override
    public void onClickRecommendedMovie(MovieItem item) {
        // Show message for the clicked movie.
        String message = String.format("Clicked recommended movie: %s.", item.title);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("AndroidJdkLibsChecker")
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart.MovieRecommendation");
        handler.post(
                () -> {
                    client.load();
                });
    }
}