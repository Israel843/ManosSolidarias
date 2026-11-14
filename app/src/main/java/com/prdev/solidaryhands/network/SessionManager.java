package com.prdev.solidaryhands.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SolidaryHandsSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PHONE = "userPhone";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;

    public SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveSession(String accessToken, String userId, String email, String fullName, String phone) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        if (fullName != null) editor.putString(KEY_USER_NAME, fullName);
        if (phone != null) editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    public void updateUserName(String fullName) {
        editor.putString(KEY_USER_NAME, fullName);
        editor.apply();
    }

    public void updateUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getAccessToken() {
        return pref.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "Usuario");
    }

    public String getUserPhone() {
        return pref.getString(KEY_USER_PHONE, "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
