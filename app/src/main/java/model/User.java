package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import data.MovieItem;

public class User implements Serializable {
    private String id;
    private String name;
    private String age;
    private String contact;
    private String email;
    private String username;
    private String password;
    private ArrayList<MovieItem> likedMovies;

    public User(){}

    public User(String id,String name, String age, String contact, String email,  String password){
        this.id = id;
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.email = email;
        this.password = password;
        likedMovies = new ArrayList<>();

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

    public void addLikedMovieItem(MovieItem movieItem) {
        this.likedMovies.add(movieItem);
    }

    public void addAllLikedMovieItem(List<MovieItem> likedMovieItems) {
        this.likedMovies.addAll(likedMovieItems);
    }

    public void setLikedMovies(ArrayList<MovieItem> likedMovies) {
        this.likedMovies = likedMovies;
    }

    public ArrayList<MovieItem> getLikedMovies() {
        return likedMovies;
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
                ", likedMovies=" + likedMovies +
                '}';
    }
}
