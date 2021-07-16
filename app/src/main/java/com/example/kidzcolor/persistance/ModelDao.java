package com.example.kidzcolor.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kidzcolor.models.VectorModel;

import java.util.List;

@Dao
public interface ModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVectorModels(VectorEntity... vectorEntities);

    @Query("SELECT * FROM models ORDER BY id DESC")
    LiveData<List<VectorEntity>> getModels();

    @Query("SELECT * FROM models WHERE id = :id")
    VectorModel getModelWithID(int id);

    @Update
    void update(VectorEntity vectorEntity);
}
