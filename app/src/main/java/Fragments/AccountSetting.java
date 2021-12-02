package Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.cinemaFreak.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import activity.ForgotPassword;
import activity.Login;

import activity.MovieRecommendation;
import activity.Register;
import database.DatabaseInstance;
import model.User;
import util.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountSetting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountSetting extends Fragment implements View.OnClickListener {
    private static final String TAG = "AccountSetting";
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userId;
    private User activeUser;


    private Button logout, edit, save;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Handler handler;

    public AccountSetting() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountSetting.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountSetting newInstance(String param1, String param2) {
        AccountSetting fragment = new AccountSetting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);







//            user = FirebaseAuth.getInstance().getCurrentUser();
//            reference = FirebaseDatabase.getInstance().getReference("Users");
//            userID = user.getUid();

//            EditText nameView,emailView,ageView,contactView,passwordView;
//            nameView = (EditText) getView().findViewById(R.id.name2);
//            emailView = (EditText) getView().findViewById(R.id.editEmail3);
//            ageView = (EditText) getView().findViewById(R.id.age3);
//            contactView = (EditText) getView().findViewById(R.id.contact3);
//            passwordView = (EditText) getView().findViewById(R.id.password3);



//            reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    User userProfile = snapshot.getValue(User.class);
//
//                    if(userProfile != null){
//                        String Name = userProfile.getName();
//                        String email = userProfile.getEmail();
//                        String age = userProfile.getAge();
//                        String contact = userProfile.getContact();
//                        String password = userProfile.getPassword();
//
//                        Log.i(TAG,"Name");
//                        Log.i(TAG,"email");
//                        nameView.setHint(Name);
//                        emailView.setHint(email);
//                        ageView.setHint(age);
//                        contactView.setHint(contact);
//                        passwordView.setHint(password);
//
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });


        }


    }



    private void loadDetails(String userId) {

        handler.post(() -> {
            Log.i(TAG, "Fetching user details from database");
            DatabaseInstance.DATABASE.getReference().child("Users").child(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    activeUser = task.getResult().getValue(User.class);
                    Log.d(TAG, "User " + userId + " fetched from database: " + activeUser);
                    String name = activeUser.getName();
                    String email = activeUser.getEmail();
                    String age = activeUser.getAge();
                    String contact = activeUser.getContact();
                    String password = activeUser.getPassword();


                    EditText nameView,emailView,ageView,contactView,passwordView;
                    nameView = getView().findViewById(R.id.name2);
                    emailView = getView().findViewById(R.id.editEmail3);
                    ageView = getView().findViewById(R.id.age3);
                    contactView = getView().findViewById(R.id.contact3);
                    passwordView = getView().findViewById(R.id.password3);


                    nameView.setText(name);
                    emailView.setText(email);
                    ageView.setText(age);
                    contactView.setText(contact);
//                    passwordView.setText(password);

                    nameView.setFocusable(false);
                    emailView.setFocusable(false);
                    ageView.setFocusable(false);
                    contactView.setFocusable(false);
                    passwordView.setFocusable(false);


                } else {
                    Log.e(TAG, "Unable to fetch active user");
                }
            });
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_setting, container, false);
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(this);

        edit = view.findViewById(R.id.editBtn);
        edit.setOnClickListener(this);

        save = view.findViewById(R.id.save);
        save.setOnClickListener(this);

        Log.v(TAG, "onStart.activity.AccountSetting");
        userId = getActivity().getIntent().getStringExtra(Constants.ACTIVE_USER_KEY);
        Log.i("AccountSettings", userId);
        loadDetails(userId);

        return view;

    }

    @Override
    public void onClick(View view) {

            switch (view.getId()){
                case R.id.logout:
                    Log.i(TAG, "Logging out");
                    ((MovieRecommendation)getActivity()).mAuth.signOut();
                    startActivity(new Intent(getActivity(), Login.class));
                    break;

                case R.id.editBtn:
                    EditText nameView,ageView,contactView,passwordView;
                    nameView = getView().findViewById(R.id.name2);
                    ageView = getView().findViewById(R.id.age3);
                    contactView = getView().findViewById(R.id.contact3);
                    passwordView = getView().findViewById(R.id.password3);
                    nameView.setFocusableInTouchMode(true);
                    ageView.setFocusableInTouchMode(true);
                    contactView.setFocusableInTouchMode(true);
                    nameView.setTextColor(Color.parseColor("#AB0800"));
                    ageView.setTextColor(Color.parseColor("#a81b1b"));
                    contactView.setTextColor(Color.parseColor("#a81b1b"));
//                    nameView.setBackground(Drawable.createFromPath("@drawable/edittext_frame"));
//                    nameView.requestFocus();

//                    nameView.setTextColor(Color.parseColor("#a81b1b"));
//                    ageView.setTextColor(Color.parseColor("#a81b1b"));
//                    contactView.setTextColor(Color.parseColor("#a81b1b"));
//                    passwordView.setFocusableInTouchMode(true);

                    break;

                case R.id.save:
                    save();
                    break;
            }
        }

    private void save() {

        EditText nameView,ageView,contactView,emailView,passwordView;
        nameView = (EditText) getView().findViewById(R.id.name2);
        emailView = (EditText) getView().findViewById(R.id.editEmail3);
        ageView = (EditText) getView().findViewById(R.id.age3);
        contactView = (EditText) getView().findViewById(R.id.contact3);
        passwordView = (EditText) getView().findViewById(R.id.password3);

       

        reference = FirebaseDatabase.getInstance().getReference("Users");

        Toast.makeText(getActivity(),"Account details updated",Toast.LENGTH_LONG).show();


        reference.child(userId).child("name").setValue(nameView.getEditableText().toString());

        reference.child(userId).child("age").setValue(ageView.getEditableText().toString());

        reference.child(userId).child("contact").setValue(contactView.getEditableText().toString());

//        reference.child(userId).child("password").setValue(passwordView.getEditableText().toString());

        nameView.setFocusable(false);
        emailView.setFocusable(false);
        ageView.setFocusable(false);
        contactView.setFocusable(false);
        passwordView.setFocusable(false);

        nameView.setTextColor(Color.parseColor("#E2E2E2"));
        ageView.setTextColor(Color.parseColor("#E2E2E2"));
        contactView.setTextColor(Color.parseColor("#E2E2E2"));



    }


}


