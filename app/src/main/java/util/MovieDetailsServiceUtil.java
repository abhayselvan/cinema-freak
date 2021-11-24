package util;

import android.util.Log;

import org.apache.http.client.utils.URIBuilder;

import java.util.ArrayList;
import java.util.List;

import data.MovieItem;
import service.model.MovieDetails;

public class MovieDetailsServiceUtil {
    private static final String TAG = "CinemaFreak-MovieDetailsServiceUtil";

    public static String buildMovieDetailsUrl(int tmdbId){
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https");
        builder.setHost(Constants.TMDB_HOST_URL);
        builder.setPath(Constants.MOVIE_PATH+"/"+tmdbId);
        builder.addParameter(Constants.API_KEY_PARAM, Constants.API_KEY);
        builder.addParameter(Constants.VIDEOS_PARAM, Constants.VIDEOS);

        String url = "";
        try {
            url = builder.build().toURL().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Url for movie details formed: "+url);
        return url;
    }


    public static List<MovieItem> mapMovieDetailsToMovieItem(List<MovieDetails> movieDetails){
        List<MovieItem> movieItems = new ArrayList<>();
        for(MovieDetails md : movieDetails){
            movieItems.add(new MovieItem(md.getId(), md.getTitle(), md.getGenres(), md.getPoster(), md.getOverview(), md.getTrailer(), md.getBackDrop(), md.getLikes(), md.getDislikes()));
        }
        return movieItems;
    }

}
