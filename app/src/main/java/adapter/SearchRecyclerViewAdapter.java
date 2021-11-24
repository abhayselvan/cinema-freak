package adapter;/*
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

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinemaFreak.R;

import java.util.List;

import activity.MovieDescription;
import data.MovieItem;
import data.Result;
import util.Constants;

public class SearchRecyclerViewAdapter
        extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {

    Context mContext;
    private final List<MovieItem> movies;
    private List<MovieItem> exampleListFull;

    public SearchRecyclerViewAdapter(Context context, List<MovieItem> movies) {
        this.mContext = context;
        this.movies = movies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.select_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MovieItem item = movies.get(position);
        holder.name.setText(item.getTitle());
        String imageUrl = Constants.TMDB_POSTER_PATH+item.getImageUrl();
        Glide.with(mContext).load(imageUrl).into(holder.image);
        holder.layout.setOnClickListener(
                v -> onClickRecommendedMovie(item));
    }

    public void clear() {
        int size = movies.size();
        movies.clear();
        notifyItemRangeRemoved(0, size);

    }

    public void onClickRecommendedMovie(MovieItem item) {
        Intent intent = new Intent(mContext, MovieDescription.class);
        intent.putExtra("movieItem", item);
        mContext.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }


    /**
     * ViewHolder to display one movie in list view of recommendation result.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final TextView name;
        public Result result;
        public LinearLayout layout;
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.name =
                    view.findViewById(R.id.name);
            this.layout= view.findViewById(R.id.movieHolder);
            this.image = view.findViewById(R.id.image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }

    }


}
