package com.example.kidzcolor.utils;

import android.graphics.Color;
import android.graphics.Paint;

public class PaintProvider {

    public final static Paint strokePaint = new Paint();
    public final static Paint hdStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static void createPaint() {
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2.0f);
        strokePaint.setColor(Color.BLACK);
    }

    public static void createHDPaint() {
        hdStrokePaint.setStyle(Paint.Style.STROKE);
        hdStrokePaint.setStrokeWidth(2.0f);
        hdStrokePaint.setColor(Color.BLACK);
    }



}
