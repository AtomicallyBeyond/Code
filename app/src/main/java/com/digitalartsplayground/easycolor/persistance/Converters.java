package com.digitalartsplayground.easycolor.persistance;

import androidx.room.TypeConverter;

import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {

    @TypeConverter
    public String fromIntegerList(List<Integer> integerList) {
        if (integerList == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {}.getType();
        String json = gson.toJson(integerList, type);
        return json;
    }

    @TypeConverter
    public List<Integer> toIntegerList(String integerListString) {
        if (integerListString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> countryLangList = gson.fromJson(integerListString, type);
        return countryLangList;
    }
}
