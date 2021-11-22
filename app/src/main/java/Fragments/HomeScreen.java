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

import adapter.GenreRecyclerViewAdapter;
import client.RecommendationClient;
import data.Config;
import data.FileUtil;
import data.ItemDetailsWrapper;
import data.MovieItem;
import data.Result;
import service.MovieDetailsCallback;
import service.MovieDetailsService;

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
    private static final String TAG = "CinemaFreak-OnDeviceRecommendationDemo";
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        movieGenreMap = new TreeMap<>();
        ItemDetailsWrapper wrap = (ItemDetailsWrapper) getActivity().getIntent().getSerializableExtra("reco");
        movies = wrap.getItemDetails();

        // Load config file.
        try {
            config = FileUtil.loadConfig(getContext().getAssets(), CONFIG_PATH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading config %s: %s.", CONFIG_PATH, ex));
        }
        loadGenres();
        handler = new Handler();
        client = new RecommendationClient(getContext(), config);
        handler.post(
                () -> {
                    client.load();
                });
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
    /**
     * Sends selected movie list and get recommendations.
     */
    private void recommend(final List<MovieItem> movies) {
        handler.post(
                () -> {
                    // Run inference with TF Lite.
                    Log.d(TAG, "Run inference with TFLite model.");
                    List<Result> recommendations = client.recommend(movies);

                    List<Integer> selectedMovies = movies.stream().map(MovieItem::getId).collect(Collectors.toList());
                    recommendations = recommendations.stream().filter(result -> !selectedMovies.contains(result.item.getId())).collect(Collectors.toList());

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
                    recommend(movies);
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
        // Show result on screen
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
}