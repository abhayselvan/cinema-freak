package activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import adapter.GenreRecyclerViewAdapter;
import client.RecommendationClient;
import data.Config;
import data.FileUtil;
import data.ItemDetailsWrapper;
import data.MovieItem;
import data.Result;

public class MovieRecommendation extends AppCompatActivity implements Serializable {
    private static final String TAG = "OnDeviceRecommendationDemo";
    private static final String CONFIG_PATH = "config.json";  // Default config path in assets.
    List<String> genres;
    HashMap<String, List<MovieItem>> movieGenreMap;
    private Handler handler;
    private RecommendationClient client;
    private List<MovieItem> movies;
    private Config config;
    private RecyclerView genreRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieGenreMap = new HashMap<>();
        setContentView(R.layout.movie_recommendation);
        ItemDetailsWrapper wrap = (ItemDetailsWrapper) getIntent().getSerializableExtra("reco");
        movies = wrap.getItemDetails();

        // Load config file.
        try {
            config = FileUtil.loadConfig(getAssets(), CONFIG_PATH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading config %s: %s.", CONFIG_PATH, ex));
        }
        loadGenres();

        //image view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        genreRecyclerView = findViewById(R.id.recommendation_genre);
        genreRecyclerView.setLayoutManager(layoutManager);

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
                    List<Result> recommendations = client.recommend(movies);

                    // Show result on screen
                    showResult(recommendations);
                });
    }

    /**
     * Shows result on the screen.
     */
    private void showResult(final List<Result> recommendations) {
        loadMap(recommendations);
        genreRecyclerView.setAdapter(
                new GenreRecyclerViewAdapter(MovieRecommendation.this, movieGenreMap, genres));
    }

    private void loadGenres() {
        try {
            InputStream inputStream = getAssets().open("movie_genre_vocab.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            String genre = new String(buffer);
            genres = Arrays.asList(genre.split("\\r?\\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMap(List<Result> recommendations) {
        for (Result res : recommendations) {
            for (String genre : res.item.genres) {
                if (!movieGenreMap.containsKey(genre)) {
                    List<MovieItem> list = new ArrayList<MovieItem>();
                    movieGenreMap.put(genre, list);
                }
                if(!movies.contains(res.item)){
                    movieGenreMap.get(genre).add(res.item);
                }
            }
        }
    }

    @SuppressWarnings("AndroidJdkLibsChecker")
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart.activity.MovieRecommendation");
        handler.post(
                () -> {
                    client.load();
                });
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.watch_later:
//                return true;
//            case R.id.home:
//                return true;
//
//            case R.id.search:
//                return true;
//
//            case R.id.account:
//                return true;
//        }
//        return false;
//    }
}