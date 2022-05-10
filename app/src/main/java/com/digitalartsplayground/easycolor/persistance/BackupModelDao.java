package com.digitalartsplayground.easycolor.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BackupModelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleModel(BackupVector backupVector);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVectorModels(BackupVector... vectorEntities);

    @Query("SELECT * FROM backup_vectors WHERE savedID=:id ")
    BackupVector getModelByID(int id);

    @Query("SELECT * FROM backup_vectors WHERE savedID=:modelID ")
    LiveData<BackupVector> getLiveModel(int modelID);

}
