package com.digitalartsplayground.easycolor.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import com.digitalartsplayground.easycolor.utils.DefaultValues;
import com.digitalartsplayground.easycolor.utils.PaintProvider;

public class PathModel {

    public static final int NO_FILL_COLOR = 0;
    public static final int YES_FILL_COLOR = 1;

    private int fillColor;
    private int fillColorStatus;
    private final float trimPathStart, trimPathEnd, trimPathOffset;
    private Path originalPath;
    private Path path;
    private Paint pathPaint;
    private Matrix scaleMatrix;


    public void drawPath(Canvas canvas){

        if(!(fillColorStatus == NO_FILL_COLOR))
            canvas.drawPath(path, pathPaint);

        canvas.drawPath(path, PaintProvider.strokePaint);
    }

    public PathModel() {
        fillColorStatus = NO_FILL_COLOR;
        fillColor = DefaultValues.PATH_FILL_COLOR;
        trimPathStart = DefaultValues.PATH_TRIM_PATH_START;
        trimPathEnd = DefaultValues.PATH_TRIM_PATH_END;
        trimPathOffset = DefaultValues.PATH_TRIM_PATH_OFFSET;
        initPathPaint();
    }

    private void initPathPaint() {
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(Color.WHITE);
    }

    public void buildPath(String pathData) {
        originalPath = androidx.core.graphics.PathParser.createPathFromPathData(pathData);
        path = new Path(originalPath);
    }

    public void makeFillPaint() {

        if(fillColorStatus == YES_FILL_COLOR)
            pathPaint.setColor(fillColor);
        else
            pathPaint.setColor(Color.WHITE);
    }

    public void transform(Matrix matrix) {
        scaleMatrix = matrix;
        trimPath();
    }

    public void trimPath() {

        Path trimmedPath;

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

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public void setFillColorStatus(int status){
        fillColorStatus = status;
        makeFillPaint();
    }
}
