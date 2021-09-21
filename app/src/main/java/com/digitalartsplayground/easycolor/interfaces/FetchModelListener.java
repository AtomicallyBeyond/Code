package com.digitalartsplayground.easycolor.interfaces;

import com.digitalartsplayground.easycolor.persistance.VectorEntity;

public interface FetchModelListener {
    void fetchModel(VectorEntity vectorEntity, int position);
}
