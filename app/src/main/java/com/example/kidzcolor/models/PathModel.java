package com.example.kidzcolor.models;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.RegionIterator;

import com.example.kidzcolor.utils.DefaultValues;
import com.example.kidzcolor.utils.ShadeMap;

public class PathModel {

    public static final int NO_FILL_COLOR = 0;
    public static final int YES_FILL_COLOR = 1;
    public static final int SHADE_FILL_COLOR = 2;

    private String pathData;
    private int fillColor;
    private int strokeColor;
    private int fillColorStatus;
    private int patternColor;
    private float strokeWidth;
    private float strokeRatio;
    private float trimPathStart, trimPathEnd, trimPathOffset;
    private Path originalPath;
    private Path path;
    private Path trimmedPath;
    private Paint pathPaint;
    private Path.FillType fillType;
    private boolean isFillAndStroke = false;
    private Matrix scaleMatrix;

    public PathModel() {
        fillColorStatus = NO_FILL_COLOR;
        fillColor = DefaultValues.PATH_FILL_COLOR;
        trimPathStart = DefaultValues.PATH_TRIM_PATH_START;
        trimPathEnd = DefaultValues.PATH_TRIM_PATH_END;
        trimPathOffset = DefaultValues.PATH_TRIM_PATH_OFFSET;
        strokeColor = DefaultValues.PATH_STROKE_COLOR;
        strokeWidth = DefaultValues.PATH_STROKE_WIDTH;
        strokeRatio = DefaultValues.PATH_STROKE_RATIO;
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.FILL);
    }

    public void buildPath() {

        originalPath = androidx.core.graphics.PathParser.createPathFromPathData(pathData);
        path = new Path(originalPath);
    }



    public void updatePaint() {
        pathPaint.setStrokeWidth(strokeWidth * strokeRatio);

        if (fillColor != Color.TRANSPARENT && strokeColor != Color.TRANSPARENT) {
            isFillAndStroke = true;
        } else if (fillColor != Color.TRANSPARENT) {
            pathPaint.setColor(fillColor);
            pathPaint.setStyle(Paint.Style.FILL);
            isFillAndStroke = false;
        } else if (strokeColor != Color.TRANSPARENT) {
            pathPaint.setColor(strokeColor);
            pathPaint.setStyle(Paint.Style.STROKE);
            isFillAndStroke = false;
        } else {
            pathPaint.setColor(Color.TRANSPARENT);
        }
    }

    public void makeStrokePaint() {
        pathPaint.setShader(null);
        pathPaint.setColor(strokeColor);
        pathPaint.setStyle(Paint.Style.STROKE);
    }

    public void makeFillPaint() {

        if(fillColorStatus == NO_FILL_COLOR)
            pathPaint.setColor(Color.WHITE);
        else if(fillColorStatus == YES_FILL_COLOR)
            pathPaint.setColor(fillColor);
        else if(fillColorStatus == SHADE_FILL_COLOR)
            pathPaint.setShader(ShadeMap.instance.getBitmapShader());
    }

    public void resetPaint() {
        pathPaint.setShader(null);
        fillColorStatus = NO_FILL_COLOR;
    }

    public void transform(Matrix matrix) {
        scaleMatrix = matrix;

        trimPath();
    }

    public void trimPath() {
        if (scaleMatrix != null) {
            if (trimPathStart == 0 && trimPathEnd == 1 && trimPathOffset == 0) {
                path = new Path(originalPath);
                path.transform(scaleMatrix);
            } else {
                PathMeasure pathMeasure = new PathMeasure(originalPath, false);
                float length = pathMeasure.getLength();
                trimmedPath = new Path();
                pathMeasure.getSegment((trimPathStart + trimPathOffset) * length, (trimPathEnd + trimPathOffset) * length, trimmedPath, true);
                path = new Path(trimmedPath);
                path.transform(scaleMatrix);
            }
        }
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getScaledAndOffsetPath(float offsetX, float offsetY, float scaleX, float scaleY) {
        Path newPath = new Path(path);
        newPath.offset(offsetX, offsetY);
        newPath.transform(getScaleMatrix(newPath, scaleX, scaleY));
        return newPath;
    }

    public Matrix getScaleMatrix(Path srcPath, float scaleX, float scaleY) {
        Matrix scaleMatrix = new Matrix();
        RectF rectF = new RectF();
        srcPath.computeBounds(rectF, true);
        scaleMatrix.setScale(scaleX, scaleY, rectF.left, rectF.top);
        return scaleMatrix;
    }

    public int getPatternColor() {
        return patternColor;
    }

    public void setPatternColor(int patternColor) {
        this.patternColor = patternColor;
    }

    public Paint getPathPaint() {
        return pathPaint;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
        pathPaint.setColor(fillColor);
    }

    public void setFillType(Path.FillType fillType) {
        this.fillType = fillType;
        if (originalPath != null)
            originalPath.setFillType(fillType);
    }

    public String getPathData() {
        return pathData;
    }

    public void setPathData(String pathData) {
        this.pathData = pathData;
    }


    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        updatePaint();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        updatePaint();
    }


    public void setStrokeRatio(float strokeRatio) {
        this.strokeRatio = strokeRatio;
        updatePaint();
    }

    public boolean isFillAndStroke() {
        return isFillAndStroke;
    }

    public void setFillColorStatus(int status){
        if(fillColorStatus == SHADE_FILL_COLOR)
            pathPaint.setShader(null);
        fillColorStatus = status;
    }
}
