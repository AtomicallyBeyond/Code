package com.digitalartsplayground.easycolor.models;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.digitalartsplayground.easycolor.utils.Utils;

public class ColoringVectorDrawable extends Drawable {

    private final ColoringVectorModel coloringVectorModel;
    private int width = -1, height = -1;
    private Matrix scaleMatrix;

    public ColoringVectorDrawable(ColoringVectorModel coloringVectorModel) {
        this.coloringVectorModel = coloringVectorModel;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {

        super.onBoundsChange(bounds);

        if (bounds.width() != 0 && bounds.height() != 0) {

            width = bounds.width();
            height = bounds.height();

            buildScaleMatrix();
            scaleAllPaths();
        }
    }


    @Override
    public void draw(Canvas canvas) {

            coloringVectorModel.drawHDPaths(canvas);
    }

    @Override
    public void setAlpha(int alpha) { }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) { }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return Utils.dpToPx((int) coloringVectorModel.getWidth());
    }

    @Override
    public int getIntrinsicHeight() {
        return Utils.dpToPx((int) coloringVectorModel.getHeight());
    }

    private void buildScaleMatrix() {
        scaleMatrix = new Matrix();

        scaleMatrix.postTranslate(width / 2 - coloringVectorModel.getViewportWidth() / 2,
                height / 2 - coloringVectorModel.getViewportHeight() / 2);

        float widthRatio = width / coloringVectorModel.getViewportWidth();
        float heightRatio = height / coloringVectorModel.getViewportHeight();
        float ratio = Math.min(widthRatio, heightRatio);
        scaleMatrix.postScale(ratio, ratio, width / 2, height / 2);
    }

    private void scaleAllPaths() {
        coloringVectorModel.scaleAllPaths(scaleMatrix);
    }
}
