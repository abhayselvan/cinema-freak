package activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cinemaFreak.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button Register;
    private TextView nameTextView,usernameTextView,ageTextView,sexTextView,contactTextView,emailTextView,passwordTextView;
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
//
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
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user = new User(name,age,contact,email,password);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(Register.this,"User has been registered successfully!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(Register.this,MovieSelection.class));
                                    }
                                    else{
                                        Toast.makeText(Register.this,"Failed to register! Try again!",Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                        }else{
                            Toast.makeText(Register.this,"Failed to register! Try again!",Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}