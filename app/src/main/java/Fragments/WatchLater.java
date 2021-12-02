package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;

import java.util.List;

import adapter.MovieSelectionRecyclerViewAdapter;
import data.MovieItem;
import main.CinemaFreakApplication;
import model.User;

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
    private GridLayoutManager gridLayoutManager;
    private MovieSelectionRecyclerViewAdapter adapter;
    private User activeUser;
    private List<MovieItem> watchLaterMoviesList;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        recyclerView = view.findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MovieSelectionRecyclerViewAdapter(getContext(), watchLaterMoviesList);
        recyclerView.setAdapter(adapter);
        return view;
    }

    public void displayMovies(){
        activeUser = ((CinemaFreakApplication)getActivity().getApplication()).getActiveSessionUser();
        if(!activeUser.getBookmarkedMovies().isEmpty())
            watchLaterMoviesList.addAll(activeUser.getBookmarkedMovies());
        adapter = new MovieSelectionRecyclerViewAdapter(getContext(), watchLaterMoviesList);
        recyclerView.setAdapter(adapter);

    }
}
