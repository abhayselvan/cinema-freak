package activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import data.MovieItem;
import database.DatabaseInstance;
import main.CinemaFreakApplication;
import model.User;
import util.Constants;
import util.YouTubeConfig;

public class MovieDescription extends YouTubeBaseActivity {

    private static final int PERMISSIONS_COARSE_LOCATION = 1;
    public static final String TAG = "DescriptionActivity";
    private Handler handler;
    private String title, description, countryCode, countryName;
    private String providerUrl, posterUrl, trailerLink;
    private TextView movieTitle, movieDescription, streamHeading;
    private LinearLayout linearLayout;
    private ImageView poster, like, bookmark, dislike;
    private TextView likeCount;
    private JSONObject providerDetails;
    private InputStream inputStream;
    private Bitmap bitmap;
    private boolean isMovieLiked, isMovieDisliked, isMovieBookmarked;

    MovieItem movie;
    User activeUser;

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

        movie = (MovieItem) getIntent().getExtras().get("movieItem");
        activeUser = ((CinemaFreakApplication)getApplication()).getActiveSessionUser();

        queue = Volley.newRequestQueue(this);
        movieTitle = findViewById(R.id.movie_title);
        movieDescription = findViewById(R.id.movie_description);
        streamHeading = findViewById(R.id.stream_heading);
        mYouTubePlayerView = findViewById(R.id.movie_trailer);
        poster = findViewById(R.id.poster);
        linearLayout = findViewById(R.id.providers);
        like = findViewById(R.id.like);
        bookmark = findViewById(R.id.bookmark);
        dislike = findViewById(R.id.dislike);
        likeCount = findViewById(R.id.like_count);

        updateLikeText();
        poster.setImageBitmap(null);
        isMovieLiked = activeUser.getLikedMovies().stream().map(MovieItem::getId).collect(Collectors.toList()).contains(movie.getId());
        isMovieDisliked = activeUser.getDislikedMovies().stream().map(MovieItem::getId).collect(Collectors.toList()).contains(movie.getId());
        isMovieBookmarked = activeUser.getBookmarkedMovies().stream().map(MovieItem::getId).collect(Collectors.toList()).contains(movie.getId());

        updateIcons();
        bookmark.setOnClickListener(view -> onBookmarkPress());
        like.setOnClickListener(view -> onLikePressed());
        dislike.setOnClickListener(view -> onDislikePressed());

        handler = new Handler();
        providerUrl = "https://" + Constants.TMDB_HOST_URL + Constants.MOVIE_PATH + "/" + movie.getId() + Constants.WATCH_PROVIDERS + "?" + Constants.API_KEY_PARAM + "=" + Constants.API_KEY;
        posterUrl = Constants.TMDB_POSTER_PATH;

        getDetails();
        getTrailer();

        Log.d(TAG, "Url for stream providers: "+providerUrl);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, providerUrl, null, response -> {
                    Log.i(TAG, "Provider details received");
                    providerDetails = response;
                    getLocation();
                }, error -> {
                    Log.i(TAG, "response error");
                    error.printStackTrace();
                });
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        queue.add(jsonObjectRequest);
    }

    private void updateLikeText(){
        if(movie.getLikes() > 0)
            likeCount.setText(movie.getLikes()+ "K people liked this movie!");
    }

    private void onLikePressed(){
        if (isMovieLiked) {
            removeLike();
        } else {
            if(isMovieDisliked)
                removeDislike();
            activeUser.addLikedMovieItem(movie);
            like.setImageResource(R.drawable.ic_baseline_thumb_up_30_red);
            movie.updateLikes(1);
            isMovieLiked=true;
        }
    }

    private void onDislikePressed(){
        if (isMovieDisliked) {
            removeDislike();
        } else {
            if(isMovieLiked)
                removeLike();
            activeUser.addDislikedMovieItem(movie);
            dislike.setImageResource(R.drawable.ic_baseline_thumb_down_30_red);
            movie.updateDislikes(1);
            isMovieDisliked=true;
        }
    }

    private void removeLike(){
        activeUser.removeLikedMovieItem(movie.getId());
        like.setImageResource(R.drawable.ic_baseline_thumb_up_30);
        movie.updateLikes(-1);
        isMovieLiked=false;
    }

    private void removeDislike(){
        activeUser.removeDislikedMovie(movie.getId());
        dislike.setImageResource(R.drawable.ic_baseline_thumb_down_30);
        movie.updateDislikes(-1);
        isMovieDisliked=false;
    }

    private void onBookmarkPress(){
        if (isMovieBookmarked) {
            activeUser.removeBookmarkedMovies(movie.getId());
            bookmark.setImageResource(R.drawable.ic_bookmark);
        } else {
            activeUser.addBookmarkedMovies(movie);
            bookmark.setImageResource(R.drawable.ic_bookmark_fill);
        }
        isMovieBookmarked = !isMovieBookmarked;
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
        if (trailerLink != null){
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
                if (imagePath != null) {
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

                if (location == null) {
                    countryName = "United States";
                    countryCode = "US";
                    getProviders(countryCode);
                    return;
                }
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

    private void updateIcons(){
        if (isMovieLiked)
            like.setImageResource(R.drawable.ic_baseline_thumb_up_30_red);

        if (isMovieDisliked)
            dislike.setImageResource(R.drawable.ic_baseline_thumb_down_30_red);

        if (isMovieBookmarked)
            bookmark.setImageResource(R.drawable.ic_bookmark_fill);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((CinemaFreakApplication)getApplication()).setActiveSessionUser(activeUser);
        handler.post(() -> {
            DatabaseInstance.DATABASE.getReference().child("Users").child(activeUser.getId()).setValue(activeUser).addOnCompleteListener(task -> {
                if (task.isComplete()) {
                    Log.i(TAG, "Updated user in db: " + activeUser);
                } else {
                    Log.i(TAG, "Error in updating db for user: " + task.getException());
                }
            });
            DatabaseReference moviesDbSnapshot = DatabaseInstance.DATABASE.getReference().child("movies").child(movie.getId() + "");
            moviesDbSnapshot.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(isMovieLiked){
                        int existingLikes = snapshot.child("likes").getValue(Integer.class);
                        Log.i(TAG, "Existing likes "+existingLikes+" will be updated to "+existingLikes+1);
                        moviesDbSnapshot.child("likes").setValue(existingLikes+1);
                    }


                    if(isMovieDisliked){
                        int existingDislikes = snapshot.child("dislikes").getValue(Integer.class);
                        Log.i(TAG, "Existing dislikes "+existingDislikes+" will be updated to "+existingDislikes+1);
                        moviesDbSnapshot.child("dislikes").setValue(existingDislikes+1);
                    }
                    Log.i(TAG, "Likes/Dislikes updated");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Unable to fetch like/dislike count. Likes/Dislikes not updated");
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequest function called");
        getLocation();
    }

}