package com.digitalartsplayground.easycolor.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleModel(VectorEntity vectorEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVectorModels(VectorEntity... vectorEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVector(VectorEntity vectorEntity);

    @Query("SELECT * FROM models ORDER BY id DESC")
    LiveData<List<VectorEntity>> getModelsLive();

    @Query("SELECT * FROM models ORDER BY id DESC")
    List<VectorEntity> getModels();

    @Query("SELECT * FROM models WHERE in_progress=1 ORDER BY id DESC")
    List<VectorEntity> getModelsInProgress();

    @Query("SELECT id FROM models")
    List<Integer> getAllIds();
}
