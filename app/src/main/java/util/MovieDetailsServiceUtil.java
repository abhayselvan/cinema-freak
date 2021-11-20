package util;

import android.util.Log;

import org.apache.http.client.utils.URIBuilder;

public class MovieDetailsServiceUtil {
    private static final String TAG = "CinemaFreak-MovieDetailsServiceUtil";

    public static String buildMovieDetailsUrl(int tmdbId){
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https");
        builder.setHost(Constants.TMDB_HOST_URL);
        builder.setPath(Constants.MOVIE_PATH+"/"+tmdbId);
        builder.addParameter(Constants.API_KEY_PARAM, Constants.API_KEY);
        String url = "";
        try {
            url = builder.build().toURL().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Url for movie details formed: "+url);
        return url;
    }


}
