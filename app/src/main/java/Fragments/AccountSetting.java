package Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.cinemaFreak.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import activity.Login;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountSetting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountSetting extends Fragment {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    private Button logout;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            logout = (Button)getView().findViewById(R.id.logout);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    FirebaseAuth.getInstance().signOut();
//                    startActivity(new Intent(this, Login.class));
                    Intent redirect=new Intent(getActivity(),Login.class);
                    getActivity().startActivity(redirect);

                }
            });

//            user = FirebaseAuth.getInstance().getCurrentUser();
//            reference = FirebaseDatabase.getInstance().getReference("Users");
//            userID = user.getUid();

            EditText nameView,emailView,ageView,contactView,passwordView;
            nameView = (EditText) getView().findViewById(R.id.name2);
            emailView = (EditText) getView().findViewById(R.id.editEmail3);
            ageView = (EditText) getView().findViewById(R.id.age3);
            contactView = (EditText) getView().findViewById(R.id.contact3);
            passwordView = (EditText) getView().findViewById(R.id.password3);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_setting, container, false);
    }
}

