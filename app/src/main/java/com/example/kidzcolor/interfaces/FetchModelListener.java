package com.example.kidzcolor.interfaces;

import com.example.kidzcolor.persistance.VectorEntity;

public interface FetchModelListener {
    void fetchModel(VectorEntity vectorEntity, int position);
}
