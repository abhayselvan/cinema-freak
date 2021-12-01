package Fragments;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import adapter.SearchRecyclerViewAdapter;
import data.Config;
import data.FileUtil;
import data.MovieItem;
import service.MovieDetailsCallback;
import service.MovieDetailsService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Search#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Search extends Fragment implements View.OnClickListener, MovieDetailsCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "Search fragment";
    private MovieDetailsService movieDetailsService;
    private ServiceConnection serviceConnection;
    private boolean isServiceConnected;
    RecyclerView searchRecyclerView;
    ArrayList<MovieItem> allMovies = new ArrayList<>();
    private Config config;
    private static final String CONFIG_PATH = "config.json";  // Default config path in assets.
    Button searchButton;
    EditText query;
    TextView textView;
    ImageView emptyState;
    ImageView emptyStateNoResult;
    SearchRecyclerViewAdapter adapter;
    public Search() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Search.
     */
    // TODO: Rename and change types and number of parameters
    public static Search newInstance(String param1, String param2) {
        Search fragment = new Search();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            config = FileUtil.loadConfig(getContext().getAssets(), CONFIG_PATH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading config %s: %s.", CONFIG_PATH, ex));
        }
        //setHasOptionsMenu(true);
        loadMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        searchRecyclerView = view.findViewById(R.id.search_list);
        searchRecyclerView.setLayoutManager(layoutManager);
        query = view.findViewById(R.id.searchquery);
        textView = view.findViewById(R.id.noMovie);
        searchButton = view.findViewById(R.id.search_button);
        emptyState = view.findViewById(R.id.emptyState);
        emptyStateNoResult = view.findViewById(R.id.noResult);
        searchButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart.activity.MovieRecommendation");
        bindMovieDetailsService();
    }

    @Override
    public void onPause() {
        super.onPause();
        query.setText("");
        emptyState.setVisibility(View.VISIBLE);
        emptyStateNoResult.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
//        adapter.clear();
    }

    private void bindMovieDetailsService(){
        Intent serviceIntent = new Intent(getActivity(), MovieDetailsService.class);
        getActivity().startService(serviceIntent);

        if(serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "On service connected with component");
                    isServiceConnected = true;
                    MovieDetailsService.MovieDetailsBinder binder = (MovieDetailsService.MovieDetailsBinder) service;
                    movieDetailsService = binder.getService();
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "On service disconnected");
                    isServiceConnected = false;
                }
            };
            Log.i(TAG, "Service bound");
            getActivity().bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void loadMovies() {
        try {
            Collection<MovieItem> collection =
                    FileUtil.loadMovieList(getContext().getAssets(), config.movieList);
            allMovies.clear();
            for (MovieItem item : collection) {
                allMovies.add(item);
            }
            Log.v(TAG, "Candidate list loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public ArrayList<MovieItem> searchAllMovies(String query){
        ArrayList<MovieItem> result = (ArrayList<MovieItem>) allMovies.stream().filter(p-> p.getTitle().toLowerCase().contains((query.toLowerCase()))).collect(Collectors.toList());
        return result;
    }


    @Override
    public void onClick(View v) {
        String queryTxt = query.getText().toString();
        emptyState.setVisibility(View.INVISIBLE);
        if(queryTxt.equals("")){
            Toast.makeText(getActivity(),"Enter a movie name to search",Toast.LENGTH_SHORT).show();
        } else{
            ArrayList<MovieItem> searchedMovies = searchAllMovies(queryTxt);
            if(searchedMovies.size()>0){
                if (textView.getVisibility() == View.VISIBLE || emptyStateNoResult.getVisibility() == View.VISIBLE ) {
                    textView.setVisibility(View.INVISIBLE);
                    emptyStateNoResult.setVisibility(View.INVISIBLE);
                }
                try {
                    movieDetailsService.getMoviesDetails(
                            searchedMovies.stream().map(MovieItem::getId).collect(Collectors.toList()), this);
                }catch (Exception e){
                    Exception exception = e;
                }

//                searchRecyclerView.setAdapter(
//                        new SearchRecyclerViewAdapter(getContext(),searchedMovies));
            }else{
                adapter = new SearchRecyclerViewAdapter(getContext(),searchedMovies);
                adapter.clear();
                searchRecyclerView.setAdapter(null);
                textView.setVisibility(View.VISIBLE);
                emptyStateNoResult.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void dbMovieDetails(List<MovieItem> searchedMovies) {
        adapter = new SearchRecyclerViewAdapter(getContext(),searchedMovies);
        //adapter.clear();
        searchRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isServiceConnected){
            return;
        }
        Log.i(TAG, "unbinding service from recommendation page");
        getActivity().unbindService(serviceConnection);
        serviceConnection = null;
        isServiceConnected = false;
    }
}