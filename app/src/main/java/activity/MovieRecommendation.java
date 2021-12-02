package activity;

import android.content.Intent;
import android.os.Bundle;
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

public class MovieRecommendation extends AppCompatActivity {

    public FirebaseAuth mAuth;

    private static final String TAG = "CinemaFreak-MovieRecommendation";
    AccountSetting accountSetting = new AccountSetting();
    WatchLater watchLater = new WatchLater();
    Search search = new Search();
    HomeScreen home = new HomeScreen();
    FragmentManager fm = getSupportFragmentManager();
    Fragment active = home;


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
                    fm.beginTransaction().hide(active).show(watchLater).commit();
                    active = watchLater;
                    return true;
            }
            return true;
        });
    }


//    public void editDetails(View view) {
//        EditText nameView,ageView,contactView,passwordView;
//        nameView = (EditText) findViewById(R.id.name2);
//        ageView = (EditText) findViewById(R.id.age3);
//        contactView = (EditText)findViewById(R.id.contact3);
//        passwordView = (EditText) findViewById(R.id.password3);
//
//        Button edit;
//
//        edit = view.findViewById(R.id.editBtn);
//        edit.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                nameView.setFocusable(false);
//                ageView.setFocusable(false);
//                contactView.setFocusable(false);
//                passwordView.setFocusable(false);
//            }
//        });
//    }
}

