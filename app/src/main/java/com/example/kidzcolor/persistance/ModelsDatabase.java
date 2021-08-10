package com.example.kidzcolor.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {VectorEntity.class, BackupVector.class}, version = 1, exportSchema = false)
public abstract class ModelsDatabase extends RoomDatabase {

    private static ModelsDatabase instance;
    public static final String DATABASE_NAME = "models_db";

    public static ModelsDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ModelsDatabase.class,
                    DATABASE_NAME
            ).build();
        }
        return instance;
    }

    public abstract ModelDao getModelsDao();
    public abstract BackupModelDao getBackupModelsDao();
}
