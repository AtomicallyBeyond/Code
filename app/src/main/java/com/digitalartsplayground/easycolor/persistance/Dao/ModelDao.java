package com.digitalartsplayground.easycolor.persistance.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.digitalartsplayground.easycolor.models.VectorEntity;

import java.util.List;

@Dao
public interface ModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVector(VectorEntity vectorEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVectorModels(List<VectorEntity> vectorEntities);

    @Query("SELECT id FROM models WHERE in_progress=1")
    LiveData<List<Integer>> getArtworkIDs();

    @Query("SELECT * FROM models WHERE id=:id")
    VectorEntity getModel(int id);

    @Query("SELECT * FROM models WHERE id=:id")
    LiveData<VectorEntity> getLiveModel(int id);

    @Query("SELECT * FROM models ORDER BY id ASC")
    List<VectorEntity> getModels();

    @Query("SELECT * FROM models ORDER BY id ASC")
    LiveData<List<VectorEntity>> getLiveModels();

    @Query("SELECT * FROM models WHERE in_progress=1 ORDER BY id ASC")
    List<VectorEntity> getModelsInProgress();

    @Query("SELECT * FROM models WHERE in_progress=1 ORDER BY id ASC")
    LiveData<List<VectorEntity>> getLiveModelsInProgress();

    @Query("SELECT id FROM models")
    List<Integer> getAllIds();
}
