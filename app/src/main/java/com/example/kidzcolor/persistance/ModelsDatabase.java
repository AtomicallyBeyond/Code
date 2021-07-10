package com.example.kidzcolor.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {VectorEntity.class}, version = 1)
public abstract class ModelsDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "models_db";

    private static ModelsDatabase instance;

    public static ModelsDatabase getInstance(final Context context) {
        if(instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ModelsDatabase.class,
                    DATABASE_NAME
            ).build();
        }
        return instance;
    }

    public abstract ModelDAO getModelDAO();

}
