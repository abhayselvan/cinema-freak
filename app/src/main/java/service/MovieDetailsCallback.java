package service;

import java.util.List;

import data.MovieItem;

public interface MovieDetailsCallback {

    void dbMovieDetails(List<MovieItem> movieItems);
}
