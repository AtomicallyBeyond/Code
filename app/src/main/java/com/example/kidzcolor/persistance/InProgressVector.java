package com.example.kidzcolor.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "in_progress_vectors")
public class InProgressVector {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "model")
    private String model;

    public InProgressVector() {

    }

    public InProgressVector(VectorEntity vectorEntity){
        id = vectorEntity.getId();
        model = vectorEntity.getModel();
    }

    public InProgressVector(BackupVector backupVector) {
        id = backupVector.getSavedID();
        model = backupVector.getModel();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
