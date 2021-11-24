/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package data;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A movie item representing recommended content.
 */
public class MovieItem implements Serializable {

    public static final String JOINER = " | ";

    private final int id;
    private final String title;
    private final List<String> genres;
    private String imageUrl;
    private String description;
    private String trailerID;
    private String wallPaperUrl;
    private int likes;
    private int dislikes;
    public boolean selected = false; // For UI selection. Default item is not selected.

    private MovieItem() {
        this(0, "", new ArrayList<>(), "", "", "", "", 0,0);
    }

    public MovieItem(int id, String title, List<String> genres, String imageUrl, String description, String trailerID, String wallPaperUrl, Integer likes, Integer dislikes) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.imageUrl = imageUrl;
        this.description = description;
        this.trailerID = trailerID;
        this.wallPaperUrl = wallPaperUrl;
        this.likes = likes==null?0:likes;
        this.dislikes = dislikes==null?0:dislikes;
    }

    public String getTrailerID() {
        return trailerID;
    }

    public void setTrailerID(String trailerID) {
        this.trailerID = trailerID;
    }

    public String getWallPaperUrl() {
        return wallPaperUrl;
    }

    public void setWallPaperUrl(String wallPaperUrl) {
        this.wallPaperUrl = wallPaperUrl;
    }

    @Override
    public String toString() {
        return String.format(
                "Id: %d, title: %s, genres: %s, selected: %s",
                id, title, TextUtils.join(JOINER, genres), selected);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void updateLikes(int delta){
        this.likes = Math.max(0, likes+delta);
    }

    public void updateDislikes(int delta){
        this.dislikes = Math.max(0, dislikes+delta);
    }
}
