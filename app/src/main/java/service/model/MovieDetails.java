package service.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieDetails {

    private int id;
    @SerializedName("original_title")
    private String title;
    private String overview;
    private String poster_path;
    private List<Genre> genres;

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

    public String getPosterPath() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
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
                ", poster_path='" + poster_path + '\'' +
                ", genres=" + genres +
                '}';
    }
}
