package com.example.kidzcolor.persistance;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BackupModelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVectorModels(BackupVector... vectorEntities);

    @Query("SELECT * FROM backup_vectors WHERE savedID=:id ")
    BackupVector getModelByID(int id);

}
