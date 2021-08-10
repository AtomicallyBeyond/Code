package com.example.kidzcolor.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "backup_vectors")
public class BackupVector {

    @PrimaryKey
    @ColumnInfo(name = "savedID")
    private int savedID;

    @ColumnInfo(name = "saved_model")
    private String model;

    public BackupVector() {

    }

    public BackupVector(VectorEntity vectorEntity){
        this.savedID = vectorEntity.getId();
        this.model = vectorEntity.getModel();
    }

    public int getSavedID() {
        return savedID;
    }


    public void setSavedID(int savedID) {
        this.savedID = savedID;
    }


    public String getModel() {
        return model;
    }


    public void setModel(String model) {
        this.model = model;
    }
}
