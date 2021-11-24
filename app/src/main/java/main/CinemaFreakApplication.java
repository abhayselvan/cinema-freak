package main;

import android.app.Application;
import android.util.Log;

import model.User;

public class CinemaFreakApplication extends Application {

    private static final String TAG = "CinemaFreak-Application";
    private User activeSessionUser;


    public User getActiveSessionUser() {
        return activeSessionUser;
    }

    public void setActiveSessionUser(User activeSessionUser) {
        Log.i(TAG, "Active session user updated: "+activeSessionUser);
        this.activeSessionUser = activeSessionUser;
    }
}
