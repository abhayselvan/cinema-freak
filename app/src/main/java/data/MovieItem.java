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

    public boolean selected = false; // For UI selection. Default item is not selected.

    private MovieItem() {
        this(0, "", new ArrayList<>(), "", "");
    }

    public MovieItem(int id, String title, List<String> genres, String imageUrl, String description) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.imageUrl = imageUrl;
        this.description = description;
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
}
