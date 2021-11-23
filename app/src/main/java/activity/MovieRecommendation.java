package activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cinemaFreak.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import Fragments.AccountSetting;
import Fragments.HomeScreen;
import Fragments.Search;
import Fragments.WatchLater;

public class MovieRecommendation extends AppCompatActivity {
    private static final String TAG = "CinemaFreak-OnDeviceRecommendationDemo";
    AccountSetting accountSetting = new AccountSetting();
    WatchLater watchLater = new WatchLater();
    Search search = new Search();
    HomeScreen home = new HomeScreen();
    FragmentManager fm = getSupportFragmentManager();
    Fragment active = home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_recommendation);

        fm.beginTransaction().add(R.id.frameLayout, accountSetting, "3").hide(accountSetting).commit();
        fm.beginTransaction().add(R.id.frameLayout, watchLater, "2").hide(watchLater).commit();
        fm.beginTransaction().add(R.id.frameLayout, search, "4").hide(search).commit();
        fm.beginTransaction().add(R.id.frameLayout, home, "1").commit();

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
                    fm.beginTransaction().hide(active).show(watchLater).commit();
                    active = watchLater;
                    return true;

            }
            return true;
        });
    }
}