package com.example.kidzcolor.models;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.example.kidzcolor.utils.Utils;

public class VectorMasterDrawable extends Drawable {

    private final VectorModel vectorModel;
    private int width = -1, height = -1;
    private Matrix scaleMatrix;
    private Boolean drawHD = false;

    public void setDrawHD() {
        drawHD = true;
    }

    public VectorMasterDrawable(VectorModel vectorModel) {
        this.vectorModel = vectorModel;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {

        super.onBoundsChange(bounds);

        if (bounds.width() != 0 && bounds.height() != 0) {

            width = bounds.width();
            height = bounds.height();

            buildScaleMatrix();
            scaleAllPaths();
            scaleAllStrokes();
        }
    }




    @Override
    public void draw(Canvas canvas) {

/*        for(PathModel pathModel : vectorModel.pathModels)
            canvas.drawPath(pathModel.getPath(), paint);*/
            if(!drawHD)
                vectorModel.drawPaths(canvas);
            else
                vectorModel.drawHDPaths(canvas);
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
        return Utils.dpToPx((int) vectorModel.getWidth());
    }

    @Override
    public int getIntrinsicHeight() {
        return Utils.dpToPx((int) vectorModel.getHeight());
    }

    private void buildScaleMatrix() {
        scaleMatrix = new Matrix();

        scaleMatrix.postTranslate(width / 2 - vectorModel.getViewportWidth() / 2, height / 2 - vectorModel.getViewportHeight() / 2);

        float widthRatio = width / vectorModel.getViewportWidth();
        float heightRatio = height / vectorModel.getViewportHeight();
        float ratio = Math.min(widthRatio, heightRatio);
        scaleMatrix.postScale(ratio, ratio, width / 2, height / 2);
    }

    private void scaleAllPaths() {
        vectorModel.scaleAllPaths(scaleMatrix);
    }

    private void scaleAllStrokes() {
        float strokeRatio;
        strokeRatio = Math.min(width / vectorModel.getWidth(), height / vectorModel.getHeight());
        vectorModel.scaleAllStrokeWidth(strokeRatio);
    }

}