package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import data.MovieItem;

public class User implements Serializable {
    private String id;
    private String name;
    private String age;
    private String contact;
    private String email;
    private String username;
    private String password;
    private List<MovieItem> likedMovies = new ArrayList<>();
    private List<MovieItem> dislikedMovies = new ArrayList<>();
    private List<MovieItem> bookmarkedMovies = new ArrayList<>();

    public User(){}

    public User(String id,String name, String age, String contact, String email,  String password){
        this.id = id;
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.email = email;
        this.password = password;

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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<MovieItem> getLikedMovies() {
        return likedMovies;
    }

    public void addLikedMovieItem(MovieItem movieItem) {
        this.likedMovies.add(movieItem);
    }

    public void removeLikedMovieItem(int movieItemId){
        this.likedMovies = this.likedMovies.stream()
                .filter(movie -> movieItemId != movie.getId()).collect(Collectors.toList());
    }

    public void addAllLikedMovieItem(List<MovieItem> likedMovieItems) {
        this.likedMovies.addAll(likedMovieItems);
    }


    public List<MovieItem> getDislikedMovies() {
        return dislikedMovies;
    }

    public void addDislikedMovieItem(MovieItem movieItem) {
        this.dislikedMovies.add(movieItem);
    }

    public void removeDislikedMovie(int movieItemId){
        this.dislikedMovies = this.dislikedMovies.stream()
                .filter(movie -> movieItemId != movie.getId()).collect(Collectors.toList());
    }

    public List<MovieItem> getBookmarkedMovies() {
        return bookmarkedMovies;
    }

    public void addBookmarkedMovies(MovieItem movieItem) {
        this.bookmarkedMovies.add(movieItem);
    }

    public void removeBookmarkedMovies(int movieItemId){
        this.bookmarkedMovies = this.bookmarkedMovies.stream()
                .filter(movie -> movieItemId != movie.getId()).collect(Collectors.toList());
    }



    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", dislikedMovies='" + dislikedMovies + '\'' +
                ", bookmarkedMovies='" + bookmarkedMovies + '\'' +
                ", likedMovies=" + likedMovies +
                '}';
    }
}
