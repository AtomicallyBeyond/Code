package com.example.kidzcolor.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kidzcolor.models.VectorModel;

import java.util.List;

@Dao
public interface ModelDAO {

    @Insert
    void insertVectorModels(VectorEntity... vectorEntities);

    @Query("SELECT * FROM models")
    LiveData<List<VectorModel>> getModels();

    @Query("SELECT * FROM models WHERE id = :id")
    VectorModel getModelWithID(int id);

    @Update
    void update(VectorEntity vectorEntity);
}
