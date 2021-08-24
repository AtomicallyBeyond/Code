package com.example.kidzcolor.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.kidzcolor.models.VectorModel;

@Entity(tableName = "models")
public class VectorEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "model")
    private String model;

    @ColumnInfo(name = "in_progress")
    private boolean isInProgress = false;

    @Ignore
    private VectorModel vectorModel;

    @Ignore
    private boolean isModelLoaded = false;

    public VectorEntity() {

    }

    public VectorEntity(BackupVector savedVector) {
        id = savedVector.getSavedID();
        model = savedVector.getModel();
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

    public boolean isInProgress() {
        return isInProgress;
    }

    public void setInProgress(boolean inProgress) {
        isInProgress = inProgress;
    }

    @Ignore
    public void loadModel() {
        vectorModel = new VectorModel(model);
        isModelLoaded = true;
    }

    @Ignore
    public boolean isModelAvailable(){
        return isModelLoaded;
    }

    @Ignore
    public VectorModel getVectorModel(){
        return vectorModel;
    }

    public void refreshVectorModel() {
        vectorModel.resetModel(model);
    }
}
