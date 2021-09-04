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
import com.example.kidzcolor.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class ReplayDrawable extends Drawable {

    private int listSize;
    private final List<PathModel> drawingList;
    private final Paint outlinePaint;

    private final VectorModel vectorModel;
    private int width = -1, height = -1;
    private Matrix scaleMatrix;
    private int index = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ReplayDrawable(VectorModelContainer vectorModel) {
        this.vectorModel = vectorModel;

        List<PathModel> pathsList;
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
