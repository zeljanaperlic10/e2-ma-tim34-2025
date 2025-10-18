package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;

    public SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(String userId, String email) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
}
