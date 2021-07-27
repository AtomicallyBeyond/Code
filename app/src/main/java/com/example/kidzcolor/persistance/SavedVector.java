package com.example.kidzcolor.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "in_progress")
public class SavedVector extends VectorEntity{

    public SavedVector() {

    }

    public SavedVector(VectorEntity vectorEntity){
        this.id = vectorEntity.getId();
        this.model = vectorEntity.getModel();
    }

    public SavedVector(int id, String model){
        this.id = id;
        this.model = model;
    }

    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "model")
    private String model;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void setModel(String model) {
        this.model = model;
    }
}
