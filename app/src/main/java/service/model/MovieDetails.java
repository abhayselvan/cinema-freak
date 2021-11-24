package service.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MovieDetails {

    private int id;
    @SerializedName("title")
    private String title;
    private String overview;
    @SerializedName("poster_path")
    private String poster;
    @SerializedName("backdrop_path")
    private String wallpaper;
    private List<Genre> genres;
    private Videos videos;
    private String trailer;

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public Videos getVideos() {
        return videos;
    }

    public void setVideos(Videos videos) {
        this.videos = videos;
    }

    public String getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(String wallpaper) {
        this.wallpaper = wallpaper;
    }

    public MovieDetails(){
        genres = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String original_title) {
        this.title = original_title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public List<String> getGenres() {
        return genres.stream().map(Genre::getName).collect(Collectors.toList());
    }
    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }


    @Override
    public String toString() {
        return "MovieDetails{" +
                "id=" + id +
                ", original_title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", poster_path='" + poster + '\'' +
                ", genres=" + genres +
                '}';
    }
}
