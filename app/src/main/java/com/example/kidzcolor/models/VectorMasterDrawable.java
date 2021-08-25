package com.example.kidzcolor.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.example.kidzcolor.models.PathModel;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.utils.DefaultValues;
import com.example.kidzcolor.utils.Utils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorMasterDrawable extends Drawable {

    private VectorModel vectorModel;
    private int width = -1, height = -1;
    private int left = 0, top = 0;
    private int tempSaveCount;
    private float offsetX = 0.0f, offsetY = 0.0f;
    private float scaleX = 1.0f, scaleY = 1.0f;
    private float scaleRatio, strokeRatio;
    private Matrix scaleMatrix;


    public VectorMasterDrawable(VectorModel vectorModel) {
        this.vectorModel = vectorModel;
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
    public void draw(Canvas canvas) {
        vectorModel.drawPaths(canvas, offsetX, offsetY, scaleX, scaleY);
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
}