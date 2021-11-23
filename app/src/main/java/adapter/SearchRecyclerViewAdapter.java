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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinemaFreak.R;

import java.util.ArrayList;
import java.util.List;

import activity.MovieDescription;
import data.MovieItem;
import data.Result;
import util.Constants;

public class SearchRecyclerViewAdapter
        extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> implements Filterable {

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
        holder.image.setOnClickListener(
                v -> onClickRecommendedMovie(item));
    }


    public void onClickRecommendedMovie(MovieItem item) {
        // Show message for the clicked movie.
//        String message = String.format("Clicked recommended movie: %s.", item.getTitle());
//        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(mContext, MovieDescription.class);
        intent.putExtra("movieId",String.valueOf(item.getId()));
        mContext.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<MovieItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (MovieItem item : exampleListFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            movies.clear();
            movies.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
    /**
     * ViewHolder to display one movie in list view of recommendation result.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final TextView name;
        public Result result;
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
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
