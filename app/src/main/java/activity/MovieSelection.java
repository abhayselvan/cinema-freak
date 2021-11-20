package activity;/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.recommendation.R;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import adapter.MovieSelectionRecyclerViewAdapter;
import client.RecommendationClient;
import data.Config;
import data.FileUtil;
import data.ItemDetailsWrapper;
import data.MovieItem;
import service.MovieDetailsService;
import service.model.MovieDetails;

/**
 * The main activity to provide interactions with users.
 */
public class MovieSelection extends AppCompatActivity implements
         Serializable {
    private static final String TAG = "OnDeviceRecommendationDemo";
    private static final String CONFIG_PATH = "config.json";  // Default config path in assets.

    private Config config;
    private RecommendationClient client;
    private final List<MovieItem> allMovies1 = new ArrayList<>();
    private final List<MovieItem> allMovies = new ArrayList<>();
    private final List<MovieItem> selectedMovies = new ArrayList<>();
    private MovieSelectionRecyclerViewAdapter adapter;

    private Handler handler;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_selection_layout);
        Log.v(TAG, "onCreate.activity.MovieSelection");

        // Load config file.
        try {
            config = FileUtil.loadConfig(getAssets(), CONFIG_PATH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading config %s: %s.", CONFIG_PATH, ex));
        }

        // Load movies list.
        try {
            allMovies1.clear();
            allMovies1.addAll(FileUtil.loadMovieList(getAssets(), config.movieSelectionList));
            for(int i=0; i<21; i++){
                allMovies.add(allMovies1.get(i));
            }
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading movies %s: %s.", config.movieSelectionList, ex));
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(gridLayoutManager);

        client = new RecommendationClient(this, config);
        handler = new Handler();

        adapter = new MovieSelectionRecyclerViewAdapter(this, allMovies);
        recyclerView.setAdapter(adapter);

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart.activity.MovieSelection");


        handler.post(
                () -> {
                    client.load();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop.activity.MovieSelection");
        handler.post(
                () -> {
                    client.unload();
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

                    // Show result on screen
                    ItemDetailsWrapper wrapper = new ItemDetailsWrapper(movies);
                    Intent intent = new Intent(this, MovieRecommendation.class);
                    intent.putExtra("reco", wrapper);
                    startActivity(intent);
                });

    }

    public void onClickRecommend(View view){
        selectedMovies.addAll(adapter.getSelectedMovies());
        if (!selectedMovies.isEmpty()) {
            // Log selected movies.
            StringBuilder sb = new StringBuilder();
            sb.append("Select movies in the following order:\n");
            for (MovieItem movie : selectedMovies) {
                sb.append(String.format("  movie: %s\n", movie));
            }
            Log.d(TAG, sb.toString());

            // Recommend based on selected movies.
            recommend(selectedMovies);
        }
    }

}
