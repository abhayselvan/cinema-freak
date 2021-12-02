package Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;

import java.util.ArrayList;
import java.util.List;

import adapter.MovieSelectionRecyclerViewAdapter;
import adapter.MovieWatchLaterRecyclerViewAdapter;
import data.MovieItem;
import database.DatabaseInstance;
import main.CinemaFreakApplication;
import model.User;
import util.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WatchLater#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WatchLater extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView recyclerView;
    private final String TAG = "CinemaFreak-WatchLater";
    private Handler handler;
    private GridLayoutManager gridLayoutManager;
    private MovieWatchLaterRecyclerViewAdapter adapter;
    private LoaderDialogFragment loader;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String userId;
    private boolean allowRefresh;
    private ImageView imageView;
    private TextView textView;

    public WatchLater() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WatchLater.
     */
    // TODO: Rename and change types and number of parameters
    public static WatchLater newInstance(String param1, String param2) {
        WatchLater fragment = new WatchLater();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler= new Handler();
        loader = new LoaderDialogFragment();
        userId = getActivity().getIntent().getStringExtra(Constants.ACTIVE_USER_KEY);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_watch_later, container, false);
        imageView = view.findViewById(R.id.watchLater);
        textView = view.findViewById(R.id.noMovieToWatch);
        recyclerView = view.findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "did i come here back?");
        super.onResume();
        loadAndFetch();
    }



    public void loadAndFetch(){
        loader.show(getActivity().getSupportFragmentManager(), "loader");
        fetchBookmarkedMovies();
    }

    public void displayMovies(List<MovieItem> bookmarkedMovies){
        if(bookmarkedMovies.isEmpty()){
            Log.i(TAG, "User does not have any bookmarked movies");
            loader.dismissAllowingStateLoss();
            return;
        }
        imageView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        adapter = new MovieWatchLaterRecyclerViewAdapter(getContext(), bookmarkedMovies);
        recyclerView.setAdapter(adapter);
        loader.dismissAllowingStateLoss();
    }



    public void fetchBookmarkedMovies(){
            handler.post(() -> {
                User activeUser = ((CinemaFreakApplication)getActivity().getApplication()).getActiveSessionUser();
                if(activeUser != null){
                    Log.i(TAG, "found active user in session");
                    displayMovies(activeUser.getBookmarkedMovies());
                    return;
                }
                else {
                    Log.i(TAG, "Fetching user bookmarked movies from DB");
                    DatabaseInstance.DATABASE.getReference().child("Users").child(userId).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User dbUser = task.getResult().getValue(User.class);
                            Log.d(TAG, "User " + userId + " fetched from database: " + dbUser);
                            displayMovies(dbUser.getBookmarkedMovies());
                        } else {
                            Log.e(TAG, "Unable to fetch active user");
                        }
                    });
                }
            });
    }
}
