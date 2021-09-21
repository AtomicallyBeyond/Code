package com.digitalartsplayground.easycolor.utils;

import android.graphics.Color;
import android.graphics.Path;

public class DefaultValues {

    public final static float VECTOR_VIEWPORT_WIDTH = 0.0f;
    public final static float VECTOR_VIEWPORT_HEIGHT = 0.0f;
    public final static float VECTOR_WIDTH = 0.0f;
    public final static float VECTOR_HEIGHT = 0.0f;

    public final static int PATH_FILL_COLOR = Color.TRANSPARENT;
    public final static int PATH_STROKE_COLOR = Color.BLACK;
    public final static float PATH_STROKE_WIDTH = 1.0f;
    public final static float PATH_STROKE_RATIO = 1.0f;
    // WINDING fill type is equivalent to NON_ZERO
    public final static Path.FillType PATH_FILL_TYPE = Path.FillType.WINDING;
    public final static float PATH_TRIM_PATH_START = 0.0f;
    public final static float PATH_TRIM_PATH_END = 1.0f;
    public final static float PATH_TRIM_PATH_OFFSET = 0.0f;
}