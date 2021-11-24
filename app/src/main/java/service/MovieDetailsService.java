package service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import database.DatabaseInstance;
import service.model.Genre;
import service.model.MovieDetails;
import service.model.Result;
import util.MovieDetailsServiceUtil;
import util.TmdbIdMapper;

public class MovieDetailsService extends Service {

    private Binder movieDetailsBinder;
    private RequestQueue volleyRequestQueue;
    private final String TAG = "CinemaFreak--MovieDetailsService";

    public MovieDetailsService() {
        this.movieDetailsBinder = new MovieDetailsBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create service invoked");
        volleyRequestQueue = Volley.newRequestQueue(getApplicationContext());
        TmdbIdMapper.getInstance().loadCsv(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "On start service invoked");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return movieDetailsBinder;
    }

    public void getMoviesDetails(List<Integer> movieIds, MovieDetailsCallback callback){
        Log.d(TAG, "Fetch movie details invoked");
        List<Integer> tmdbIds = movieIds.stream()
                .map(movieId -> TmdbIdMapper.getInstance().getTmdbId(getApplicationContext(), movieId))
                .filter(movieId -> movieId != -1)
                .collect(Collectors.toList());

        List<MovieDetails> moviesInDatabase = new ArrayList<>();
        List<Integer> tmdbIdNotInDatabase =new ArrayList<>();

        DatabaseReference childRef = DatabaseInstance.DATABASE.getReference("movies");
        childRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(int tmdbId: tmdbIds){
                    if(snapshot.hasChild(tmdbId+"")){
                        Log.d(TAG, "Movie exists in DB. Fetching details for "+tmdbId);
                        MovieDetails md = mapDataToMovieDetails(tmdbId, snapshot.child(tmdbId+""));
                        moviesInDatabase.add(md);

                    } else {
                        tmdbIdNotInDatabase.add(tmdbId);
                    }
                }

                if(tmdbIdNotInDatabase.size() > 0)
                    fetchDetailsFromTmdb(tmdbIdNotInDatabase);

                callback.dbMovieDetails(MovieDetailsServiceUtil.mapMovieDetailsToMovieItem(moviesInDatabase));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "error in reading db: "+error.getMessage());
            }
        });
    }

    private MovieDetails mapDataToMovieDetails(int tmdbId, DataSnapshot snapshot){
        MovieDetails md = new MovieDetails();
        md.setId(tmdbId);
        md.setOverview(snapshot.child("overview").getValue(String.class));
        md.setTitle(snapshot.child("title").getValue(String.class));
        md.setPoster(snapshot.child("poster").getValue(String.class));
        md.setBackDrop(snapshot.child("backdrop").getValue(String.class));
        md.setTrailer(snapshot.child("trailer").getValue(String.class));
        md.setLikes(snapshot.child("likes").getValue(Integer.class));
        md.setDislikes(snapshot.child("dislikes").getValue(Integer.class));
        String genres = snapshot.child("genres").getValue(String.class);
        if(genres != null){
            List<Genre> genreList = new ArrayList<>();
            for(String genre: genres.split(",")){
                genreList.add(new Genre(genre));
            }
            md.setGenres(genreList);
        }
        Log.d(TAG, "Movie mapped: "+md);
        return md;
    }

    public void fetchDetailsFromTmdb(List<Integer> tmdbIds) {
        Log.d(TAG, "fetchDetailsFromTmdb invoked");
        List<MovieDetails> movieDetails = new ArrayList<>();

        for(int tmdbId : tmdbIds){
            Thread thread = new Thread(() -> {
                String url = MovieDetailsServiceUtil.buildMovieDetailsUrl(tmdbId);
                RequestFuture<MovieDetails> future = RequestFuture.newFuture();
                volleyRequestQueue.add(new VolleyRequestAdapter<>(url, MovieDetails.class, null, future, future));
                try {
                    Log.d(TAG, "Fetching details for tmdb id "+tmdbId+" on thread "+Thread.currentThread().getName());
                    MovieDetails response = future.get(60, TimeUnit.SECONDS);
                    Log.i(TAG, "Got response : " + response);
                    insertMovieDetailsInDatabase(response);
                    movieDetails.add(response);
                } catch (Exception e) {
                    Log.e(TAG, "unable to call tmdb: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            thread.start();
        }

        Log.i(TAG, "Updated database with missing tmdb ids");
    }

    private void insertMovieDetailsInDatabase(MovieDetails response){
        DatabaseReference childRef = DatabaseInstance.DATABASE.getReference("movies").child(response.getId()+"");
        childRef.child("overview").setValue(response.getOverview());
        childRef.child("title").setValue(response.getTitle());
        childRef.child("poster").setValue(response.getPoster());
        childRef.child("backdrop").setValue(response.getBackDrop());
        StringBuilder sb= new StringBuilder();
        response.getGenres().forEach(m -> sb.append(m).append(","));
        if(sb.length()>0)
            sb.delete(sb.length()-1, sb.length());
        childRef.child("genres").setValue(sb.toString());

        String trailerLink = "";
        List<Result> results = response.getVideos().getResults();
        for (int i = 0; i < results.size(); i++){
            String type = results.get(i).getType();
            String site = results.get(i).getSite();
            if (type.equals("Trailer") && site.equals("YouTube")){
                trailerLink = results.get(i).getKey();
                break;
            }
        }
        if (trailerLink.length() > 0) {
            childRef.child("trailer").setValue(trailerLink);
        } else {
            childRef.child("trailer").setValue("");
        }
        Log.i(TAG, "Completed updating DB for tmdb Id "+response.getId());
    }

    public class MovieDetailsBinder extends Binder {

        public MovieDetailsService getService(){
            return MovieDetailsService.this;
        }
    }


}