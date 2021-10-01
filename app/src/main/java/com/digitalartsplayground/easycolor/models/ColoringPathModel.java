package com.digitalartsplayground.easycolor.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;

import com.digitalartsplayground.easycolor.utils.DefaultValues;
import com.digitalartsplayground.easycolor.utils.PaintProvider;
import com.digitalartsplayground.easycolor.utils.ShadeMap;

public class ColoringPathModel {
    public static final int NO_FILL_COLOR = 0;
    public static final int YES_FILL_COLOR = 1;
    public static final int SHADE_FILL_COLOR = 2;

    private String pathData;
    private String fillColorString;
    private int fillColor;
    private int fillColorStatus;
    private int patternColor;
    private final float trimPathStart, trimPathEnd, trimPathOffset;
    private Path originalPath;
    private Path path;
    private Paint pathPaint;
    private Matrix scaleMatrix;


    public void drawHDPath(Canvas canvas){

        if(!(fillColorStatus == NO_FILL_COLOR))
            canvas.drawPath(path, pathPaint);

        canvas.drawPath(path, PaintProvider.hdStrokePaint);
    }

    public ColoringPathModel() {
        fillColorStatus = NO_FILL_COLOR;
        fillColor = DefaultValues.PATH_FILL_COLOR;
        trimPathStart = DefaultValues.PATH_TRIM_PATH_START;
        trimPathEnd = DefaultValues.PATH_TRIM_PATH_END;
        trimPathOffset = DefaultValues.PATH_TRIM_PATH_OFFSET;
        initPathPaint();
    }

    public ColoringPathModel(ColoringPathModel coloringPathModel){
        originalPath = coloringPathModel.originalPath;
        path = coloringPathModel.path;
        fillColorStatus = coloringPathModel.fillColorStatus;
        fillColor = coloringPathModel.fillColor;
        trimPathStart = coloringPathModel.trimPathStart;
        trimPathEnd = coloringPathModel.trimPathEnd;
        trimPathOffset = coloringPathModel.trimPathOffset;
        initPathPaint();
    }

    private void initPathPaint() {
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(Color.WHITE);
    }

    public void buildPath() {

        originalPath = androidx.core.graphics.PathParser.createPathFromPathData(pathData);
        path = new Path(originalPath);
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
    }

    public void setFillColorString(String fillColorString){
        this.fillColorString = fillColorString;
    }

    public String getFillColorString(){
        return fillColorString;
    }

    public String getPathData() {
        return pathData;
    }

    public void setPathData(String pathData) {
        this.pathData = pathData;
    }

    public void setFillColorStatus(int status){
        if(fillColorStatus == SHADE_FILL_COLOR)
            pathPaint.setShader(null);
        fillColorStatus = status;
        makeFillPaint();
    }

    public int getFillColorStatus() {
        return fillColorStatus;
    }
}
