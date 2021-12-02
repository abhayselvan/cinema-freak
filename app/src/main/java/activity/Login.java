package activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinemaFreak.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.Config;
import data.FileUtil;
import data.MovieItem;
import util.Constants;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CinemaFreak-Login";
    private static final String CONFIG_PATH = "config.json";
    private TextView register,forgotPassword;
    private EditText editTextUsername, editTextPassword;
    private Button signIn;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private FirebaseAuth mAuth;
    ImageView imageView;
    Config config;
    private final List<MovieItem> allMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        register = (TextView)findViewById(R.id.registerHere);
        register.setOnClickListener(this);

        forgotPassword = (TextView)findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);

        signIn = (Button) findViewById(R.id.login);
        signIn.setOnClickListener(this);

        editTextUsername = (EditText) findViewById(R.id.userName);
        editTextPassword = (EditText) findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();

        // Load config file.
        try {
            config = FileUtil.loadConfig(getAssets(), CONFIG_PATH);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading config %s: %s.", CONFIG_PATH, ex));
        }
        // Load movies list.
        try {
            allMovies.clear();
            allMovies.addAll(FileUtil.loadMovieList(getAssets(), config.movieSelectionList));
        } catch (IOException ex) {
            Log.e(TAG, String.format("Error occurs when loading movies %s: %s.", config.movieSelectionList, ex));
        }
        imageView = findViewById(R.id.imageview);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.registerHere:
                startActivity(new Intent(this,Register.class));
                break;

            case R.id.login:
                userLogin();
                break;

            case R.id.forgotPassword:
                startActivity(new Intent(this,ForgotPassword.class));
                break;
        }
    }

    private void userLogin() {
        String email = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(email.isEmpty())
        {
            editTextUsername.setError("Please provide username");
            editTextUsername.requestFocus();
            return;
        }

        if(password.isEmpty())
        {
            editTextPassword.setError("Please provide password");
            editTextPassword.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextUsername.setError("Please provide valid email");
            editTextUsername.requestFocus();
            return;
        }
        if(password.length() < 6)
        {
            editTextPassword.setError("Minimum password length is 6 characters!");
            editTextPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                Intent intent = new Intent(Login.this, MovieRecommendation.class);
                intent.putExtra(Constants.ACTIVE_USER_KEY, mAuth.getCurrentUser().getUid());
                startActivity(intent);
            }
            else{
                Toast.makeText(Login.this,"Failed to login! Please check your credentials!",Toast.LENGTH_LONG).show();
                Log.e(TAG, "Login failed with error: "+task.getException());
            }
        });

    }
}