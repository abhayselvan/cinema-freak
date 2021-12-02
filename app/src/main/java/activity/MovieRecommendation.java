package activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cinemaFreak.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import Fragments.AccountSetting;
import Fragments.HomeScreen;
import Fragments.Search;
import Fragments.WatchLater;
import database.DatabaseInstance;
import main.CinemaFreakApplication;
import model.User;
import util.Constants;

public class MovieRecommendation extends AppCompatActivity {

    public FirebaseAuth mAuth;

    private static final String TAG = "CinemaFreak-MovieRecommendation";
    private AccountSetting accountSetting;
    private WatchLater watchLater;
    private Search search;
    private HomeScreen home;
    private FragmentManager fm;
    private Fragment active;
    private String userId;
    private User activeUser;
    private Handler handler;


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MovieRecommendation.this, MovieRecommendation.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_recommendation);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        userId = getIntent().getStringExtra(Constants.ACTIVE_USER_KEY);
        handler = new Handler();
        loadActiveUserFromDb(userId);

        fm = getSupportFragmentManager();
        home = new HomeScreen();
        search = new Search();
        watchLater = new WatchLater();
        accountSetting = new AccountSetting();
        active = home;
        fm.beginTransaction().add(R.id.frameLayout, accountSetting, "3").hide(accountSetting).commit();
        fm.beginTransaction().add(R.id.frameLayout, watchLater, "2").hide(watchLater).commit();
        fm.beginTransaction().add(R.id.frameLayout, search, "4").hide(search).commit();
        fm.beginTransaction().add(R.id.frameLayout, home, "1").commit();
        mAuth = FirebaseAuth.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic("movies")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.i(TAG,"Subscription to movies topic failed");
                    } else {
                        Log.i(TAG,"Subscribed to movies topic successfully");
                    }
                });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setItemIconTintList(null);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    fm.beginTransaction().hide(active).show(home).commit();
                    active = home;
                    return true;

                case R.id.account:
                    fm.beginTransaction().hide(active).show(accountSetting).commit();
                    active = accountSetting;
                    return true;

                case R.id.search:
                    fm.beginTransaction().hide(active).show(search).commit();
                    active = search;
                    return true;

                case R.id.watch_later:
                    watchLater.displayMovies(activeUser.getBookmarkedMovies());
                    fm.beginTransaction().hide(active).show(watchLater).commit();
                    active = watchLater;
                    return true;
            }
            return true;
        });
    }

    private void loadActiveUserFromDb(String userId) {
        handler.post(() -> {
            Log.i(TAG, "Fetching user details from database");
            if(userId == null){
                Log.i(TAG, "User id NOT found. Not loading active user profile");
                return;
            }
            DatabaseInstance.DATABASE.getReference().child("Users").child(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    activeUser = task.getResult().getValue(User.class);
                    ((CinemaFreakApplication)getApplication()).setActiveSessionUser(activeUser);
                    Log.d(TAG, "User " + userId + " fetched from database: " + activeUser);
                } else {
                    Log.e(TAG, "Unable to fetch active user");
                }
            });
        });
    }


}