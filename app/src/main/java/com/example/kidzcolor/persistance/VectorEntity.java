package com.example.kidzcolor.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "models")
public class VectorEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "model")
    public String model;


}
