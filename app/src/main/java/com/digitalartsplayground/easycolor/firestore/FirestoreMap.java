package com.digitalartsplayground.easycolor.firestore;

import androidx.annotation.NonNull;
import androidx.lifecycle.Transformations;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Entity(tableName = "model_id_map")
public class FirestoreMap {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "model_id")
    public List<Integer> index;

    public FirestoreMap() {

    }

    public FirestoreMap(List<Integer> index) {
        this.index = index;
    }

    public void shuffleList(long seed) {
        if(index != null)
            Collections.shuffle(index, new Random(seed));
    }

}
