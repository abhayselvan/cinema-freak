package database;

import com.google.firebase.database.FirebaseDatabase;

public class DatabaseInstance {

    private DatabaseInstance(){}

    public static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
}
