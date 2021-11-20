package activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.recommendation.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import adapter.GenreRecyclerViewAdapter;
import client.RecommendationClient;
import data.Config;
import data.FileUtil;
import data.ItemDetailsWrapper;
import data.MovieItem;
import data.Result;
import service.MovieDetailsCallback;
import service.MovieDetailsService;

public class MovieRecommendation extends AppCompatActivity implements Serializable, MovieDetailsCallback {
    private static final String TAG = "CinemaFreak-OnDeviceRecommendationDemo";
    private static final String CONFIG_PATH = "config.json";  // Default config path in assets.
    List<String> genres;
    HashMap<String, List<MovieItem>> movieGenreMap;
    //private RecommendationFragment recommendationFragment;
    private Handler handler;
    private RecommendationClient client;
    private List<MovieItem> movies;
    private Config config;
    private RecyclerView genreRecyclerView;
    private MovieDetailsService movieDetailsService;
    private ServiceConnection serviceConnection;


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

                    movieDetailsService.getMoviesDetails(
                            recommendations.stream().map(r -> r.item.getId()).collect(Collectors.toList()), this);

                });
    }

    /**
     * Shows result on the screen.
     */
    private void showResult(final List<MovieItem> recommendations) {
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

    private void loadMap(List<MovieItem> recommendations) {
        for (MovieItem movieItem : recommendations) {
            for (String genre : movieItem.getGenres()) {
                if (!movieGenreMap.containsKey(genre)) {
                    List<MovieItem> list = new ArrayList<>();
                    movieGenreMap.put(genre, list);
                }
                movieGenreMap.get(genre).add(movieItem);
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
        bindMovieDetailsService();
    }

    private void bindMovieDetailsService(){
        Intent serviceIntent = new Intent(this, MovieDetailsService.class);
        startService(serviceIntent);

        if(serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "On service connected with component");
                    MovieDetailsService.MovieDetailsBinder binder = (MovieDetailsService.MovieDetailsBinder) service;
                    movieDetailsService = binder.getService();
                    recommend(movies);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "On service disconnected");
                }

            };
            Log.i(TAG, "Service bound");
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void dbMovieDetails(List<MovieItem> movieItems) {
        // Show result on screen
        showResult(movieItems);
    }
}