package com.example.kidzcolor.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.persistance.VectorEntity;

import java.util.List;

@Dao
public interface ModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVectorModels(VectorEntity... vectorEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVector(VectorEntity vectorEntity);

    @Query("SELECT * FROM models ORDER BY id DESC")
    LiveData<List<VectorEntity>> getModels();

    @Query("SELECT * FROM models WHERE id=:id ")
    VectorEntity getModelByID(int id);

    @Update
    void update(VectorEntity vectorEntity);
}
