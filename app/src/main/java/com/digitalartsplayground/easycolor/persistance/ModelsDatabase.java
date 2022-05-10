package com.digitalartsplayground.easycolor.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.models.VectorEntity;

@Database(entities = {VectorEntity.class, BackupVector.class, FirestoreMap.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class ModelsDatabase extends RoomDatabase {

    private static ModelsDatabase instance;
    public static final String DATABASE_NAME = "models_db";

    public static ModelsDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ModelsDatabase.class,
                    DATABASE_NAME
            )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract ModelDao getModelsDao();
    public abstract BackupModelDao getBackupModelsDao();
    public abstract ModelIDsDao getModelIDsDao();
}
