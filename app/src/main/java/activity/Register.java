package activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cinemaFreak.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import model.User;
import util.Constants;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CinemaFreak-Register";
    private FirebaseAuth mAuth;
    private Button Register;
    private EditText editTextName,editTextPassword,editTextAge, editTextContact, editTextEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        Register = (Button) findViewById(R.id.registerBtn);
        Register.setOnClickListener(this);
        editTextName = (EditText)findViewById(R.id.name);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextAge = (EditText) findViewById(R.id.age);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextContact = (EditText) findViewById(R.id.contact);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.registerBtn:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        String contact = editTextContact.getText().toString().trim();

        if(name.isEmpty())
        {
            editTextName.setError("Name is required");
            editTextName.requestFocus();
            return;
        }
        if(age.isEmpty())
        {
            editTextAge.setError("Age is required");
            editTextAge.requestFocus();
            return;
        }

        if(email.isEmpty())
        {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if(contact.isEmpty())
        {
            editTextContact.setError("Contact is required");
            editTextContact.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please provide valid email");
            editTextEmail.requestFocus();
            return;

        }
        if(!Patterns.PHONE.matcher(contact).matches()){
            editTextContact.setError("Please provide valid contact");
            editTextContact.requestFocus();
            return;

        }
        if(password.isEmpty())
        {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }
        if(password.length()<6)
        {
            editTextPassword.setError("Minimum Password length is 6 characters");
            editTextPassword.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        User user = new User(userId, name,age,contact,email,password);
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(userId)
                                .setValue(user).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        Toast.makeText(Register.this,"User has been registered successfully!", Toast.LENGTH_LONG).show();
                                        Intent movieSelectionIntent = new Intent(Register.this, MovieSelection.class);
                                        movieSelectionIntent.putExtra(Constants.ACTIVE_USER_KEY, user);
                                        startActivity(movieSelectionIntent);
                                    }
                                    else{
                                        Log.e(TAG, "Unable to update database with user details: "+task1.getException());
                                        Toast.makeText(Register.this,"Failed to register! Try again!",Toast.LENGTH_LONG).show();
                                    }

                                });

                    }else{
                        Log.e(TAG, "Unable to authenticate: "+task.getException());
                        Toast.makeText(Register.this,"Failed to register! Try again!",Toast.LENGTH_LONG).show();
                    }
                });

    }
}