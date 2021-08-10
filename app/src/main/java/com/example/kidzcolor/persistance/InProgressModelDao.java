package com.example.kidzcolor.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InProgressModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVectorModels(InProgressVector... inProgressVectors);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVector(InProgressVector inProgressVector);

    @Query("SELECT * FROM in_progress_vectors ORDER BY id DESC")
    LiveData<List<VectorEntity>> getModels();
}
