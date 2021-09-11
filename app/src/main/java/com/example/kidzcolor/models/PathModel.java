package com.example.kidzcolor.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;

import com.example.kidzcolor.utils.PaintProvider;
import com.example.kidzcolor.utils.DefaultValues;
import com.example.kidzcolor.utils.ShadeMap;

public class PathModel {

    public static final int NO_FILL_COLOR = 0;
    public static final int YES_FILL_COLOR = 1;
    public static final int SHADE_FILL_COLOR = 2;

    private String pathData;
    private String fillColorString;
    private int fillColor;
    private final int strokeColor;
    private int fillColorStatus;
    private int patternColor;
    private final float strokeWidth;
    private float strokeRatio;
    private final float trimPathStart, trimPathEnd, trimPathOffset;
    private Path originalPath;
    private Path path;
    private Paint pathPaint;
    private Path.FillType fillType;
    private boolean isFillAndStroke = false;
    private Matrix scaleMatrix;


    public void drawPath(Canvas canvas){

        if(!(fillColorStatus == NO_FILL_COLOR))
            canvas.drawPath(path, pathPaint);

        canvas.drawPath(path, PaintProvider.strokePaint);
    }

    public void drawHDPath(Canvas canvas){

        if(!(fillColorStatus == NO_FILL_COLOR))
            canvas.drawPath(path, pathPaint);

        canvas.drawPath(path, PaintProvider.hdStrokePaint);
    }

    public PathModel() {
        fillColorStatus = NO_FILL_COLOR;
        fillColor = DefaultValues.PATH_FILL_COLOR;
        trimPathStart = DefaultValues.PATH_TRIM_PATH_START;
        trimPathEnd = DefaultValues.PATH_TRIM_PATH_END;
        trimPathOffset = DefaultValues.PATH_TRIM_PATH_OFFSET;
        strokeColor = DefaultValues.PATH_STROKE_COLOR;
        strokeWidth = DefaultValues.PATH_STROKE_WIDTH;
        strokeRatio = DefaultValues.PATH_STROKE_RATIO;
        initPathPaint();
    }

    public PathModel(PathModel pathModel){
        originalPath = pathModel.originalPath;
        path = pathModel.path;
        fillColorStatus = pathModel.fillColorStatus;
        fillColor = pathModel.fillColor;
        trimPathStart = pathModel.trimPathStart;
        trimPathEnd = pathModel.trimPathEnd;
        trimPathOffset = pathModel.trimPathOffset;
        strokeColor = pathModel.strokeColor;
        strokeWidth = pathModel.strokeWidth;
        strokeRatio = pathModel.strokeRatio;
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

    public void updatePaint() {
        pathPaint.setStrokeWidth(strokeWidth * strokeRatio);

        //need to check what this function is doing while coloring in the game

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


    public void setStrokeRatio(float strokeRatio) {
        this.strokeRatio = strokeRatio;
        updatePaint();
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
