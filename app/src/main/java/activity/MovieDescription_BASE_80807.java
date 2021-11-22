package activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cinemaFreak.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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
import util.Constants;
import util.YouTubeConfig;

public class MovieDescription extends YouTubeBaseActivity {

    private static final int PERMISSIONS_COARSE_LOCATION = 1;

    String title, description, countryCode;
    String url, posterUrl, trailerLink;
    TextView movieTitle, movieDescription, streamHeading;
    LinearLayout linearLayout;
    ImageView poster;
    JSONObject movieDetails;
    InputStream inputStream;
    Bitmap bitmap;

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

        Log.i("DescriptionActivity", "OnCreate");

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String movieId = bundle.getString("movieId");

        queue = Volley.newRequestQueue(this);
        movieTitle = (TextView) findViewById(R.id.movie_title);
        movieDescription = (TextView) findViewById(R.id.movie_description);
        streamHeading = (TextView) findViewById(R.id.stream_heading);
        mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.movie_trailer);
        poster = (ImageView) findViewById(R.id.poster);
        linearLayout = (LinearLayout) findViewById(R.id.providers);
        poster.setImageBitmap(null);

        url = "https://" + Constants.TMDB_HOST_URL + Constants.MOVIE_PATH + "/" + movieId + "?" + Constants.API_KEY_PARAM + "=" +Constants.API_KEY+ Constants.VIDEOS_WATCH_PROVIDERS;
        posterUrl = Constants.TMDB_POSTER_PATH;


        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("DescriptionActivity", "Movie API received");
                        movieDetails = response;
                        getDetails();
                        getTrailer();
                        getLocation();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("DescriptionActivity", "response error");
                error.printStackTrace();
            }
        });

//        locationRequest = new LocationRequest();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        queue.add(jsonObjectRequest);
    }

    private void getDetails() {
        try {
            title = (String) movieDetails.get("title");
            description = (String) movieDetails.get("overview");
            movieTitle.setText(title);
            movieDescription.setText(description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getProviders(String countryCode) {

        JSONArray providers = null;
        ArrayList<String> providerNames = new ArrayList<>();
        ArrayList<String> providerImages = new ArrayList<>();

        try {
            providers = movieDetails.getJSONObject("watch/providers").getJSONObject("results").getJSONObject(countryCode).getJSONArray("flatrate");
            for (int i = 0; i < providers.length(); i++){
                JSONObject provider = providers.getJSONObject(i);
                providerNames.add(provider.getString("provider_name"));
                providerImages.add(provider.getString("logo_path"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("DescriptionActivity",providerNames.toString());

        getProviderLogos(providerImages, providerNames);
    }

    private void getProviderLogos(ArrayList<String> providerImages, ArrayList<String> providerNames) {

        if (providerImages.size() == 0){
            return;
        }

        ArrayList<Bitmap> providerLogos = new ArrayList<>();
        ArrayList<ImageView> providerImageViews = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < providerImages.size(); i++){
            int finalI = i;
            providerImageViews.add(new ImageView(MovieDescription.this));
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {
                        Log.i("DescriptionActivity", "Loading logos");
                        String imagePath = providerImages.get(finalI);
                        if (imagePath.length() > 0) {
                            inputStream = new URL(posterUrl + imagePath).openStream();
                            bitmap = BitmapFactory.decodeStream(inputStream);
                            providerLogos.add(bitmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        Log.i("DescriptionActivity", "Logo count = " + String.valueOf(providerLogos.size()));

        for (int i = 0; i < threads.size(); i++){
            try {
                threads.get(i).join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        streamHeading.setVisibility(View.VISIBLE);
        for (int i = 0; i < providerLogos.size(); i++){

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,0,10,0);
            providerImageViews.get(i).setLayoutParams(params);
            providerImageViews.get(i).setImageBitmap(providerLogos.get(i));
            linearLayout.addView(providerImageViews.get(i));
        }
    }

    private void getTrailer() {
        trailerLink = "";
        try {
            JSONArray videos = movieDetails.getJSONObject("videos").getJSONArray("results");
            for (int i = 0; i < videos.length(); i++){
                JSONObject video = videos.getJSONObject(i);
                if (video.getString("type").equals("Trailer") && video.getString("site").equals("YouTube")){
                    trailerLink = video.getString("key");
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("DescriptionActivity", "Trailer or poster");
        if (trailerLink.length() > 0){
            Log.i("DescriptionActivity", "Load Trailer");
            loadTrailer();
        } else {
            loadPoster();
        }
    }

    private void loadPoster() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    Log.i("DescriptionActivity", "Loading image");
                    String imagePath = movieDetails.getString("backdrop_path");
                    if (imagePath.length() > 0) {
                        inputStream = new URL(posterUrl + imagePath).openStream();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                poster.setImageBitmap(bitmap);
                                mYouTubePlayerView.setVisibility(View.INVISIBLE);
                                poster.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void loadTrailer() {

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(trailerLink);
                Log.i("DescriptionActivity", "Trailer loaded");

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        mYouTubePlayerView.initialize(YouTubeConfig.getApiKey(), mOnInitializedListener);
    }

    private void getLocation() {
        Log.i("DescriptionActivity", "getLocation called");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.i("DescriptionActivity", "Location Permission granted");
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.i("DescriptionActivity", "Location received");
                    Geocoder geocoder = new Geocoder(MovieDescription.this);
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        countryCode = addresses.get(0).getCountryCode();
                        Log.i("DescriptionActivity", "Country code assigned");
                        getProviders(countryCode);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                Log.i("DescriptionActivity", "Location Permission requested");
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_COARSE_LOCATION);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("DescriptionActivity", "onRequest function called");
        getLocation();
    }

}