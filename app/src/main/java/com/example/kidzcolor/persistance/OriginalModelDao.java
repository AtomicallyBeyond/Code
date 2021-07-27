package com.example.kidzcolor.persistance;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface OriginalModelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVectorModels(SavedVector... vectorEntities);

    @Query("SELECT * FROM models WHERE id=:id ")
    SavedVector getModelByID(int id);
}
