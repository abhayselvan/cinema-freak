package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;

import java.util.List;
import java.util.Map;

import model.User;
import data.MovieItem;

public class GenreRecyclerViewAdapter extends RecyclerView.Adapter<GenreRecyclerViewAdapter.ViewHolder> {

    Context mContext;
    private final Map<String, List<MovieItem>> movieGenreMap;
    private final List<String> genres;

    public GenreRecyclerViewAdapter(Context context, Map<String, List<MovieItem>> movieGenreMap, List<String> genres) {
        this.mContext = context;
        this.movieGenreMap = movieGenreMap;
        this.genres = genres;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_recommendation_genre_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GenreRecyclerViewAdapter.ViewHolder holder, int position) {
        String genre = (String) movieGenreMap.keySet().toArray()[position];
        if (movieGenreMap.containsKey(genre)) {
            List<MovieItem> movies = movieGenreMap.get(genre);
            holder.genre.setText(genre);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            Context temp = holder.recyclerView.getContext();
            holder.recyclerView.setAdapter(
                    //pass user
                    new RecommendationRecyclerViewAdapter(temp, movies));
        }
    }


    @Override
    public int getItemCount() {
        return movieGenreMap.size();
    }

    /**
     * ViewHolder to display one movie in list view of recommendation result.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        //public final View view;
        public final TextView genre;
        public RecyclerView recyclerView;

        public ViewHolder(View view) {
            super(view);
            //this.view = view;
            this.genre =
                    view.findViewById(R.id.genreText);
            this.recyclerView = view.findViewById(R.id.recommendation_list);
            this.setIsRecyclable(false);
        }
    }
}
