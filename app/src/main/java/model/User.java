package model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import data.MovieItem;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private ArrayList<MovieItem> likedMovies;

    public User(){}

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.likedMovies = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<MovieItem> getLikedMovies() {
        return likedMovies;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", likedMovies=" + likedMovies +
                '}';
    }

    public void addLikedMovieItem(MovieItem movieItem) {
        this.likedMovies.add(movieItem);
    }

    public void addAllLikedMovieItem(List<MovieItem> likedMovieItems) {
        this.likedMovies.addAll(likedMovieItems);
    }

}
