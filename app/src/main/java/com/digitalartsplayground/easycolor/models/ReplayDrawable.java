package com.digitalartsplayground.easycolor.models;

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

import com.digitalartsplayground.easycolor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class /**/ReplayDrawable extends Drawable {

    private int listSize;
    private final List<ColoringPathModel> drawingList;
    private final Paint outlinePaint;

    private final ColoringVectorModel coloringVectorModel;
    private int width = -1, height = -1;
    private Matrix scaleMatrix;
    private int index = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ReplayDrawable(VectorModelContainer coloringVectorModel) {
        this.coloringVectorModel = coloringVectorModel;

        List<ColoringPathModel> pathsList;
        pathsList = coloringVectorModel.getColoredPathsHistory();
        drawingList = new ArrayList<>(pathsList.size());

        ColoringPathModel tempPath;

        for(ColoringPathModel coloringPathModel : pathsList) {
            tempPath = new ColoringPathModel(coloringPathModel);
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

                if(index < listSize){
                    drawingList.get(index).setFillColorStatus(PathModel.YES_FILL_COLOR);
                    drawingList.get(index).makeFillPaint();
                    index++;
                    ReplayDrawable.this.invalidateSelf();
                    handler.postDelayed(runnable, 100);
                }
        }
    };

    public void startReplay() {
        runnable.run();
    }


    @Override
    public void draw(Canvas canvas) {

        for(ColoringPathModel coloringPathModel : drawingList) {
            canvas.drawPath(coloringPathModel.getPath(), coloringPathModel.getPathPaint());
            canvas.drawPath(coloringPathModel.getPath(), outlinePaint);
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
        }
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

        scaleMatrix.postTranslate(width / 2 - coloringVectorModel.getViewportWidth() / 2, height / 2 - coloringVectorModel.getViewportHeight() / 2);

        float widthRatio = width / coloringVectorModel.getViewportWidth();
        float heightRatio = height / coloringVectorModel.getViewportHeight();
        float ratio = Math.min(widthRatio, heightRatio);

        scaleMatrix.postScale(ratio, ratio, width / 2, height / 2);
    }

    private void scaleAllPaths() {
        coloringVectorModel.scaleAllPaths(scaleMatrix);
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
