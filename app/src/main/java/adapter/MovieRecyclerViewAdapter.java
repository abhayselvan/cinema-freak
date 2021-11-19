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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;

import java.util.List;

import activity.MovieFragment;
import data.MovieItem;

/**
 * adapter.MovieRecyclerViewAdapter: a {@link RecyclerView.Adapter} that can display a {@link MovieItem} for
 * users to select from, and makes a call to the specified {@link
 * MovieFragment.OnListFragmentInteractionListener}.
 */
public class MovieRecyclerViewAdapter
        extends RecyclerView.Adapter<MovieRecyclerViewAdapter.ViewHolder> {
    private final List<MovieItem> values;
    private final MovieFragment.OnListFragmentInteractionListener listener;

    public MovieRecyclerViewAdapter(
            List<MovieItem> items, MovieFragment.OnListFragmentInteractionListener listener) {
        values = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.tfe_re_fragment_selection, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = values.get(position);
        holder.movieSwitch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Switch sw = ((Switch) v);
                        // Use checked status.
                        boolean selected = sw.isChecked();
                        holder.setSelected(selected);
                    }
                });
        holder.movieTitle.setText(values.get(position).title);

        holder.view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toggle checked status.
                        boolean selected = !holder.movieSwitch.isChecked();
                        holder.setSelected(selected);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    /**
     * ViewHolder to display one movie in list view of the selection.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final Switch movieSwitch;
        public final TextView movieTitle;
        public final MovieFragment.OnListFragmentInteractionListener listener;
        public MovieItem item;

        public ViewHolder(View view, MovieFragment.OnListFragmentInteractionListener listener) {
            super(view);
            this.view = view;
            this.movieSwitch = view.findViewById(R.id.movie_switch);
            this.movieTitle = view.findViewById(R.id.movie_title);
            this.listener = listener;
        }

        public void setSelected(boolean selected) {
            item.selected = selected;
            if (movieSwitch.isChecked() != selected) {
                movieSwitch.setChecked(selected);
            }
            if (null != listener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                listener.onItemSelectionChange(item);
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + movieTitle.getText() + "'";
        }
    }
}
