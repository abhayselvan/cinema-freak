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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.tensorflow.lite.examples.recommendation.R;

import java.util.List;

import data.MovieItem;
import data.Result;

public class RecommendationRecyclerViewAdapter
        extends RecyclerView.Adapter<RecommendationRecyclerViewAdapter.ViewHolder> {

    Context mContext;
    private final List<MovieItem> movies;

    public RecommendationRecyclerViewAdapter(Context context, List<MovieItem> movies) {
        this.mContext = context;
        this.movies = movies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_recommendation_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MovieItem item = movies.get(position);
        holder.name.setText(item.title);
        String imageUrl = "";
        if (item.imageUrl.equals("")) {
            imageUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BNDIwMDIxNzk3Ml5BMl5BanBnXkFtZTgwMTg0MzQ4MDE@._V1_SX1500_CR0,0,1500,999_AL_.jpg";
        } else {
            imageUrl = item.imageUrl;
        }
        // holder.scoreView.setText(String.format("[%d]", item.id));

        Glide.with(mContext).load(imageUrl).into(holder.image);
//
        holder.image.setOnClickListener(
                v -> onClickRecommendedMovie(item));
    }


    public void onClickRecommendedMovie(MovieItem item) {
        // Show message for the clicked movie.
        String message = String.format("Clicked recommended movie: %s.", item.title);
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
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
        public final TextView scoreView;
        public final TextView name;
        public Result result;
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.scoreView = view.findViewById(R.id.recommendation_score);
            this.name =
                    view.findViewById(R.id.name);
            this.image = view.findViewById(R.id.image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }
    }
}
