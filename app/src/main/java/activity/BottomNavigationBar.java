package activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cinemaFreak.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationBar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation_bar);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {


            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent in;
                switch (item.getItemId()) {
                    case R.id.account:
                        Toast.makeText(BottomNavigationBar.this, "Recents", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.search:
                        Toast.makeText(BottomNavigationBar.this, "Favorites", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.watch_later:
                        Toast.makeText(BottomNavigationBar.this, "Nearby", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.home:
                        Log.i("matching", "matching inside1 matching" +  item.getItemId());
                        in=new Intent(getBaseContext(),MovieRecommendation.class);
                        startActivity(in);
                        overridePendingTransition(0, 0);
                        //Toast.makeText(BottomNavigationBar.this, "Nearby", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }
}