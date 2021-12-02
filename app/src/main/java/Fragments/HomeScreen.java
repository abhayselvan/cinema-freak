package Fragments;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import main.CinemaFreakApplication;
import model.User;
import adapter.GenreRecyclerViewAdapter;
import client.RecommendationClient;
import data.Config;
import data.FileUtil;
import data.MovieItem;
import data.Result;
import database.DatabaseInstance;
import service.MovieDetailsCallback;
import service.MovieDetailsService;
import util.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreen extends Fragment implements Serializable, MovieDetailsCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "CinemaFreak-HomeScreen";
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
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeScreen newInstance(String param1, String param2) {
        HomeScreen fragment = new HomeScreen();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        movieGenreMap = new TreeMap<>();

        userId = getActivity().getIntent().getStringExtra(Constants.ACTIVE_USER_KEY);
        recommendations = new ArrayList<>();
        loadActiveUserFromDb(userId);

        // Load config file.
        try {
            config = FileUtil.loadConfig(getContext().getAssets(), CONFIG_PATH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading config %s: %s.", CONFIG_PATH, ex));
        }
        loadGenres();

        client = new RecommendationClient(getContext(), config);
        handler.post(() -> client.load());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //image view
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);
        progressBar = view.findViewById(R.id.pBar);
        progressBar.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        genreRecyclerView = view.findViewById(R.id.recommendation_genre);
        genreRecyclerView.setLayoutManager(layoutManager);

        return view;
    }

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

    private void showResult(final List<MovieItem> recommendations) {
        loadMap(recommendations);
        genreRecyclerView.setAdapter(
                new GenreRecyclerViewAdapter(getContext(), movieGenreMap, genres));
        progressBar.setVisibility(View.GONE);
    }

    private void loadGenres() {
        try {
            InputStream inputStream = getContext().getAssets().open("movie_genre_vocab.txt");
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
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart.activity.MovieRecommendation");
        handler.post(
                () -> {
                    client.load();
                });
        bindMovieDetailsService();
    }

    private void bindMovieDetailsService(){
        Intent serviceIntent = new Intent(getActivity(), MovieDetailsService.class);
        getActivity().startService(serviceIntent);

        if(serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "On service connected with component");
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
            getActivity().bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void dbMovieDetails(List<MovieItem> movieItems) {
        showResult(movieItems);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isServiceConnected){
            return;
        }
        Log.i(TAG, "unbinding service from recommendation page");
        getActivity().unbindService(serviceConnection);
        serviceConnection = null;
        isServiceConnected = false;
    }

    private void loadActiveUserFromDb(String userId) {
        handler.post(() -> {
            Log.i(TAG, "Fetching user details from database");
            if(userId == null)
                return;
            DatabaseInstance.DATABASE.getReference().child("Users").child(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    activeUser = task.getResult().getValue(User.class);
                    ((CinemaFreakApplication)getActivity().getApplication()).setActiveSessionUser(activeUser);
                    Log.d(TAG, "User " + userId + " fetched from database: " + activeUser);
                    movies = activeUser.getLikedMovies();
                    executeRecommendationEngine();
                } else {
                    Log.e(TAG, "Unable to fetch active user");
                }
            });
        });
    }
}