package com.example.kidzcolor.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.kidzcolor.R;
import com.example.kidzcolor.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReplayDrawable extends Drawable {


    private int drawIndex = -1;
    private int listSize = 0;
    private List<PathModel> pathsList;
    private List<PathModel> drawingList;
    private Paint outlinePaint;

    private VectorModel vectorModel;
    private int width = -1, height = -1;
    private int left = 0, top = 0;
    private float offsetX = 0.0f, offsetY = 0.0f;
    private float scaleX = 1.0f, scaleY = 1.0f;
    private float scaleRatio, strokeRatio;
    private Matrix scaleMatrix;
    private int index = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    public ReplayDrawable(VectorModelContainer vectorModel) {
        this.vectorModel = vectorModel;
        pathsList = vectorModel.getColoredPathsHistory();
        drawingList = new ArrayList<>(pathsList.size());

        PathModel tempPath;

        for(PathModel pathModel : pathsList) {
            tempPath = new PathModel(pathModel);
            tempPath.setFillColorStatus(PathModel.NO_FILL_COLOR);
            tempPath.makeFillPaint();
            drawingList.add(tempPath);
        }

        listSize = pathsList.size();

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(Color.BLACK);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if(index < listSize){
                    drawingList.get(index).setFillColorStatus(PathModel.YES_FILL_COLOR);
                    drawingList.get(index).makeFillPaint();
                    index++;
                    ReplayDrawable.this.invalidateSelf();
                } else {
                    handler.removeCallbacks(runnable);
                }
            } finally {
                handler.postDelayed(runnable, 100);
            }
        }
    };

    public void startReplay() {
        runnable.run();
    }


    @Override
    public void draw(Canvas canvas) {

        for(PathModel pathModel : drawingList) {
            canvas.drawPath(pathModel.getPath(), pathModel.getPathPaint());
            canvas.drawPath(pathModel.getPath(), outlinePaint);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {

        super.onBoundsChange(bounds);

        if (bounds.width() != 0 && bounds.height() != 0) {

            left = bounds.left;
            top = bounds.top;

            width = bounds.width();
            height = bounds.height();

            buildScaleMatrix();
            scaleAllPaths();
            scaleAllStrokes();
        }
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

        scaleRatio = ratio;

        scaleMatrix.postScale(ratio, ratio, width / 2, height / 2);
    }

    private void scaleAllPaths() {
        vectorModel.scaleAllPaths(scaleMatrix);
    }

    private void scaleAllStrokes() {
        strokeRatio = Math.min(width / vectorModel.getWidth(), height / vectorModel.getHeight());
        vectorModel.scaleAllStrokeWidth(strokeRatio);
    }

    public void update() {
        invalidateSelf();
    }

    public float getScaleRatio() {
        return scaleRatio;
    }

    public float getStrokeRatio() {
        return strokeRatio;
    }

    public Matrix getScaleMatrix() {
        return scaleMatrix;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        invalidateSelf();
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        invalidateSelf();
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
        invalidateSelf();
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
        invalidateSelf();
    }


    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
