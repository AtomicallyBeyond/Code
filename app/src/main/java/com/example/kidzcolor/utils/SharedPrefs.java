package com.example.kidzcolor.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LAST_MODIFIED = "lastModified";
    public static final String LAST_VISIBLE = "lastVisible";
    public static final String END_REACHED = "endReached";

    private static SharedPrefs instance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static SharedPrefs getInstance(Context context){
        if(instance == null){
            instance = new SharedPrefs(context);
        }
        return instance;
    }

    private SharedPrefs(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public int getLastModified() {
        return sharedPreferences.getInt(LAST_MODIFIED, 0);
    }

    public int getLastVisible() {
        return sharedPreferences.getInt(LAST_VISIBLE, 0);
    }

    public boolean getEndReached() {
        return sharedPreferences.getBoolean(END_REACHED, false);
    }

    public void setLastModified(int lastModified) {
        editor.putInt(LAST_MODIFIED, lastModified).apply();
    }

    public void setLastVisible(int lastVisible) {
        editor.putInt(LAST_VISIBLE, lastVisible).apply();
    }

    public void setEndReached(boolean endReached) {
        editor.putBoolean(END_REACHED, endReached).apply();
    }
}
