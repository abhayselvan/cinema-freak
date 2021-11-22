package activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cinemaFreak.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import data.MovieItem;
import database.DatabaseInstance;
import model.User;
import util.Constants;
import util.YouTubeConfig;

public class MovieDescription extends YouTubeBaseActivity {

    private static final int PERMISSIONS_COARSE_LOCATION = 1;
    public static final String TAG = "DescriptionActivity";

    String title, description, countryCode, countryName;
    String providerUrl, posterUrl, trailerLink;
    TextView movieTitle, movieDescription, streamHeading;
    LinearLayout linearLayout;
    ImageView poster, like;
    JSONObject providerDetails;
    InputStream inputStream;
    Bitmap bitmap;

    MovieItem movie;
    User user;

    YouTubePlayerView mYouTubePlayerView;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;
    RequestQueue queue;
    JsonObjectRequest jsonObjectRequest;
    
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_description);

        Log.i(TAG, "OnCreate");

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        movie = (MovieItem) bundle.get("movieId");
        user = (User) bundle.get(Constants.ACTIVE_USER_KEY);

        queue = Volley.newRequestQueue(this);
        movieTitle = (TextView) findViewById(R.id.movie_title);
        movieDescription = (TextView) findViewById(R.id.movie_description);
        streamHeading = (TextView) findViewById(R.id.stream_heading);
        mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.movie_trailer);
        poster = (ImageView) findViewById(R.id.poster);
        linearLayout = (LinearLayout) findViewById(R.id.providers);
        like = (ImageView) findViewById(R.id.like);
        poster.setImageBitmap(null);

        providerUrl = "https://" + Constants.TMDB_HOST_URL + Constants.MOVIE_PATH + "/" + movie.getId() + Constants.WATCH_PROVIDERS + "?" + Constants.API_KEY_PARAM + "=" + Constants.API_KEY;
        posterUrl = Constants.TMDB_POSTER_PATH;
        getDetails();
        getTrailer();

        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, providerUrl, null,
                response -> {
                    Log.i(TAG, "Movie API received");
                    providerDetails = response;
                    getLocation();
                }, error -> {
                    Log.i(TAG, "response error");
                    error.printStackTrace();
                });

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        queue.add(jsonObjectRequest);

        like.setOnClickListener(view -> {
            if (!user.getLikedMovies().contains(movie)) {
                user.addLikedMovieItem(movie);
                like.setImageResource(R.drawable.ic_baseline_thumb_up_30_red);
                DatabaseInstance.DATABASE.getReference().child("Users").child(user.getId()).setValue(user);
            }
        });
    }

    private void getDetails() {
        title = movie.getTitle();
        description = movie.getDescription();
        movieTitle.setText(title);
        movieDescription.setText(description);
    }

    private void getProviders(String countryCode) {

        ArrayList<String> providerNames = new ArrayList<>();
        ArrayList<String> providerImages = new ArrayList<>();

        try {
            JSONArray providers = providerDetails.getJSONObject("results").getJSONObject(countryCode).getJSONArray("flatrate");
            for (int i = 0; i < providers.length(); i++){
                JSONObject provider = providers.getJSONObject(i);
                providerNames.add(provider.getString("provider_name"));
                providerImages.add(provider.getString("logo_path"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,providerNames.toString());

        getProviderLogos(providerImages);
    }

    private void getProviderLogos(ArrayList<String> providerImages) {

        if (providerImages.size() == 0){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            TextView textView = new TextView(getApplicationContext());
            textView.setLayoutParams(params);
            textView.setTextColor(Color.rgb(226,226,226));
            textView.setTextSize(15);
            textView.setText("No Streaming options available.");
            linearLayout.addView(textView);
            return;
        }

        ArrayList<Bitmap> providerLogos = new ArrayList<>();
        ArrayList<ImageView> providerImageViews = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < providerImages.size(); i++){
            int finalI = i;
            providerImageViews.add(new ImageView(MovieDescription.this));
            Thread thread = new Thread(() -> {
                try  {
                    Log.i(TAG, "Loading logos");
                    String imagePath = providerImages.get(finalI);
                    if (imagePath.length() > 0) {
                        inputStream = new URL(posterUrl + imagePath).openStream();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        providerLogos.add(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads.add(thread);
            thread.start();
        }
        Log.i(TAG, "Logo count = " + providerLogos.size());

        for (int i = 0; i < threads.size(); i++){
            try {
                threads.get(i).join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        for (int i = 0; i < providerLogos.size(); i++){

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,0,10,0);
            providerImageViews.get(i).setLayoutParams(params);
            providerImageViews.get(i).setImageBitmap(providerLogos.get(i));
            linearLayout.addView(providerImageViews.get(i));
        }
    }

    private void getTrailer() {
        trailerLink = movie.getTrailerID();
        Log.i(TAG, "Trailer or poster");
        if (trailerLink.length() > 0){
            Log.i(TAG, "Load Trailer");
            loadTrailer();
        } else {
            loadPoster();
        }
    }

    private void loadPoster() {
        Thread thread = new Thread(() -> {
            try  {
                Log.i(TAG, "Loading image");
                String imagePath = movie.getWallPaperUrl();
                if (imagePath.length() > 0) {
                    inputStream = new URL(posterUrl + imagePath).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    runOnUiThread(() -> {
                        poster.setImageBitmap(bitmap);
                        mYouTubePlayerView.setVisibility(View.INVISIBLE);
                        poster.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    private void loadTrailer() {

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(trailerLink);
                Log.i(TAG, "Trailer loaded");

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
    }

    private void getLocation() {
        Log.i(TAG, "getLocation called");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Location Permission granted");
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                Log.i(TAG, "Location received");
                Geocoder geocoder = new Geocoder(MovieDescription.this);
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    countryCode = addresses.get(0).getCountryCode();
                    countryName = addresses.get(0).getCountryName();
                    Log.i(TAG, "Country code assigned");
                    getProviders(countryCode);

                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                Log.i(TAG, "Location Permission requested");
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_COARSE_LOCATION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequest function called");
        getLocation();
    }

}