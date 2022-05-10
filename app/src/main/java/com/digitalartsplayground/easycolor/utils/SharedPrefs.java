 package com.digitalartsplayground.easycolor.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SharedPrefs {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EXPIRE_DATE = "expireDate";
    private static final String RANDOM_SEED = "randomSeed";
    public static final String BANNER_CLICK_COUNTER = "bannerClickCounter";
    public static final String MODEL_VIEW_COUNT = "modelViewCount";
    public static final String FIRESTORE_MAP_FETCHED_TIME = "firestoreTime";

    private static SharedPrefs instance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static SharedPrefs getInstance(Context context){
        if(instance == null){
            instance = new SharedPrefs(context);
        }
        return instance;
    }

    private SharedPrefs(@NotNull Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void resetAdPrefs(){
        setBannerClickCounter(0);
        setExpireDate(0);
    }

    public long getFirestoreFetchedTime() {
        return sharedPreferences.getLong(FIRESTORE_MAP_FETCHED_TIME, 0); }

    public void setFirestoreFetchedTime(long fetchedTime) {
        editor.putLong(FIRESTORE_MAP_FETCHED_TIME, fetchedTime).apply();
    }

    public long getExpireDate() { return sharedPreferences.getLong(EXPIRE_DATE, -1); }

    public void setExpireDate(long expireDate) {
        editor.putLong(EXPIRE_DATE, expireDate).apply();
    }

    public long getRandomSeed() {return sharedPreferences.getLong(RANDOM_SEED, 0);}

    public void setRandomSeed(long seedValue) {
        editor.putLong(RANDOM_SEED, seedValue).apply();
    }


    public int getBannerClickCounter() { return sharedPreferences.getInt(BANNER_CLICK_COUNTER, 0); }

    public void setBannerClickCounter(int count) {
        editor.putInt(BANNER_CLICK_COUNTER, count).apply();
    }

    public int getModelViewCount() {
        return sharedPreferences.getInt(MODEL_VIEW_COUNT, 0);
    }

    public void setModelViewCount(int count) {
        editor.putInt(MODEL_VIEW_COUNT, count).apply();
    }
}
