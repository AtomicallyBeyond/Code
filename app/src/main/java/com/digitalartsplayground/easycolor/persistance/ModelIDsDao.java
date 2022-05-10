package com.digitalartsplayground.easycolor.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import java.util.List;

@Dao
public interface ModelIDsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFirestoreMap(FirestoreMap firestoreMap);

    @Query("SELECT * FROM model_id_map ORDER BY model_id ASC")
    LiveData<FirestoreMap> getLiveFirestoreMap();
}
