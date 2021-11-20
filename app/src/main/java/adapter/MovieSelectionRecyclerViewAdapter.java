package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.tensorflow.lite.examples.recommendation.R;

import java.util.ArrayList;
import java.util.List;

import data.MovieItem;

public class MovieSelectionRecyclerViewAdapter extends RecyclerView.Adapter<MovieSelectionRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<MovieItem> my_data;
    private final List<MovieItem> selectedMovies = new ArrayList<>();

    public MovieSelectionRecyclerViewAdapter(Context context, List<MovieItem> my_data) {
        this.context = context;
        this.my_data = my_data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_selection_card,parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        MovieItem item = my_data.get(position);
        holder.description.setText(item.title);
        Glide.with(context).load(item.imageUrl).into(holder.imageView);
        holder.imageView.setOnClickListener(
                v -> onClickRecommendedMovie(item));


    }

    public void onClickRecommendedMovie(MovieItem item){
        if (!selectedMovies.contains(item)) {
                selectedMovies.add(item);
        }
        else {
            selectedMovies.remove(item);
        }
    }

    @Override
    public int getItemCount() {
        return my_data.size();
    }

    public List<MovieItem> getSelectedMovies(){
        return selectedMovies;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView description;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.description = (TextView) itemView.findViewById(R.id.name);
            this.imageView = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}
