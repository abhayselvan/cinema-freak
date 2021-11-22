package activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import adapter.GenreRecyclerViewAdapter;
import client.RecommendationClient;
import data.Config;
import data.FileUtil;
import data.ItemDetailsWrapper;
import data.MovieItem;
import data.Result;
import database.DatabaseInstance;
import model.User;
import service.MovieDetailsCallback;
import service.MovieDetailsService;
import util.Constants;

public class MovieRecommendation extends AppCompatActivity implements Serializable, MovieDetailsCallback {
    private static final String TAG = "CinemaFreak-MovieRecommendationActivity";
    private static final String CONFIG_PATH = "config.json";  // Default config path in assets.
    List<String> genres;
    TreeMap<String, List<MovieItem>> movieGenreMap;
    //private RecommendationFragment recommendationFragment;
    private Handler handler;
    private RecommendationClient client;
    private List<MovieItem> movies;
    private Config config;
    private RecyclerView genreRecyclerView;
    private MovieDetailsService movieDetailsService;
    private ServiceConnection serviceConnection;
    private boolean isServiceConnected;
    private ProgressBar progressBar;
    private String userId;
    private User activeUser;
    private List<Result> recommendations;
    private boolean fetchRecommendations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_recommendation);

        movieGenreMap = new TreeMap<>();
        recommendations = new ArrayList<>();
        handler = new Handler();

        progressBar = findViewById(R.id.pBar);
        progressBar.setVisibility(View.VISIBLE);

        userId = getIntent().getStringExtra(Constants.ACTIVE_USER_KEY);
        loadActiveUser(userId);
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


        client = new RecommendationClient(this, config);
        handler.post(() -> client.load());

    }

    /**
     * Sends selected movie list and get recommendations.
     */
    private void executeRecommendationEngine() {
        Log.i(TAG, "Executing recommendation engine for selected movies");
            // Run inference with TF Lite.
            Log.d(TAG, "Run inference with TFLite model.");
            recommendations = client.recommend(movies);
            Log.d(TAG, "Recommendations loaded");
            if (isServiceConnected && !fetchRecommendations) {
                Log.d(TAG, "Entered from db");
                fetchMovieDetailsForRecommendations();
            }
    }

    private void fetchMovieDetailsForRecommendations() {
        fetchRecommendations = true;
        List<Integer> selectedMovies = movies.stream().map(MovieItem::getId).collect(Collectors.toList());
        recommendations = recommendations.stream().filter(result -> !selectedMovies.contains(result.item.getId())).collect(Collectors.toList());
        Log.i(TAG, "Fetching movie details for all recommendations");
        movieDetailsService.getMoviesDetails(
                recommendations.stream().map(r -> r.item.getId()).collect(Collectors.toList()), this);
    }

    /**
     * Shows result on the screen.
     */
    private void showResult(final List<MovieItem> recommendations) {
        loadMap(recommendations);
        genreRecyclerView.setAdapter(
                new GenreRecyclerViewAdapter(MovieRecommendation.this, movieGenreMap, genres, activeUser));
        progressBar.setVisibility(View.GONE);
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

    private void loadActiveUser(String userId) {
        handler.post(() -> {
            Log.i(TAG, "Fetching user details from database");
            DatabaseInstance.DATABASE.getReference().child("users").child(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    activeUser = task.getResult().getValue(User.class);
                    Log.d(TAG, "User " + userId + " fetched from database: " + activeUser);
                    movies = activeUser.getLikedMovies();
                    executeRecommendationEngine();
                } else {
                    Log.e(TAG, "Unable to fetch active user");
                }
            });
        });
    }

    @SuppressWarnings("AndroidJdkLibsChecker")
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart.activity.MovieRecommendation");
        handler.post(() -> client.load());
        bindMovieDetailsService();
    }

    private void bindMovieDetailsService() {
        Intent serviceIntent = new Intent(this, MovieDetailsService.class);
        startService(serviceIntent);

        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "Service bound with activity");
                    isServiceConnected = true;
                    MovieDetailsService.MovieDetailsBinder binder = (MovieDetailsService.MovieDetailsBinder) service;
                    movieDetailsService = binder.getService();
                    if (recommendations.size() > 0 && !fetchRecommendations) {
                        fetchMovieDetailsForRecommendations();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "On service disconnected");
                    isServiceConnected = false;
                }

            };
            Log.i(TAG, "Service bound");
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void dbMovieDetails(List<MovieItem> movieItems) {
        // Show result on screen
        Log.i(TAG, "Movie details for recommendations fetched. Displaying cards");
        showResult(movieItems);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isServiceConnected) {
            return;
        }
        Log.i(TAG, "unbinding service from recommendation page");
        unbindService(serviceConnection);
        serviceConnection = null;
        isServiceConnected = false;
    }
}