package com.digitalartsplayground.easycolor.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String COUNTER = "counter";
    public static final String EXPIRE_DATE = "expireDate";

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

    public void resetAdPrefs(){
        setCounter(0);
        setExpireDate(0);
    }

    public int getCounter() { return sharedPreferences.getInt(COUNTER, 0); }

    public long getExpireDate() { return sharedPreferences.getLong(EXPIRE_DATE, -1); }

    public void setExpireDate(long expireDate) {
        editor.putLong(EXPIRE_DATE, expireDate).apply();
    }

    public void setCounter(int counter){
        editor.putInt(COUNTER, counter).apply();
    }
}
