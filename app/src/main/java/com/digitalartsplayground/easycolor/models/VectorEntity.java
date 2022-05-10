package com.digitalartsplayground.easycolor.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.digitalartsplayground.easycolor.interfaces.DrawableAvailable;
import com.digitalartsplayground.easycolor.persistance.BackupVector;
import com.digitalartsplayground.easycolor.utils.AppExecutors;

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
    private VectorDrawable vectorDrawable;

    @Ignore
    private DrawableAvailable drawableAvailable;

    public DrawableAvailable getDrawableAvailable() {
        return drawableAvailable;
    }

    public void setDrawableAvailable(DrawableAvailable drawableAvailable) {
        this.drawableAvailable = drawableAvailable;
    }

    @Ignore
    public VectorDrawable getDrawable() {
        return vectorDrawable;
    }

    @Ignore
    public boolean isDrawableAvailable() {
        if(vectorDrawable == null)
            return false;
        return true;
    }

    @Ignore
    public boolean  isModelAvailable() {
        if(model != null)
            return true;
        return false;
    }

    public VectorEntity() {

    }

    @Ignore
    public VectorEntity(int id) {
        this.id = id;
    }

    @Ignore
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

    public void loadDrawable() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                vectorDrawable = new VectorDrawable(new VectorModel(model));

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(drawableAvailable != null)
                            drawableAvailable.drawableAvailable();
                    }
                });
            }
        });
    }
}
