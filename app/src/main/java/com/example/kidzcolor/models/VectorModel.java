package com.example.kidzcolor.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.kidzcolor.interfaces.ColorDepletedListener;
import com.example.kidzcolor.utils.DefaultValues;
import com.example.kidzcolor.utils.Utils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorModel {

    private int modelID;
    private File xmlFile;
    private XmlPullParser xpp;
    private float width, height;
    private float viewportWidth, viewportHeight;
    private Paint strokePaint;
    protected List<PathModel> pathModels = new ArrayList<>();
    private RectF testRect;
    private boolean drawRect = false;

    public VectorModel(File xmlFile){
            this.xmlFile = xmlFile;
            init();
    }

    private void init() {
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(1.0f);
        buildVectorModel();
    }

    private void buildVectorModel() {

        PathModel pathModel = null;
        int tempPosition;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            FileInputStream fs = new FileInputStream(xmlFile);
            xpp.setInput(new InputStreamReader(fs));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (XmlPullParserException e) {
            return;
        }


        try {

            int event = xpp.getEventType();
            int patternColor = 500;

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = xpp.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals("vector")) {
                            tempPosition = getAttrPosition(xpp, "viewportWidth");
                            setViewportWidth((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_VIEWPORT_WIDTH);

                            tempPosition = getAttrPosition(xpp, "viewportHeight");
                            setViewportHeight((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_VIEWPORT_HEIGHT);

                            tempPosition = getAttrPosition(xpp, "width");
                            setWidth((tempPosition != -1) ? Utils.getFloatFromDimensionString(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_WIDTH);

                            tempPosition = getAttrPosition(xpp, "height");
                            setHeight((tempPosition != -1) ? Utils.getFloatFromDimensionString(xpp.getAttributeValue(tempPosition)) : DefaultValues.VECTOR_HEIGHT);
                        } else if (name.equals("path")) {
                            pathModel = new PathModel();

                            tempPosition = getAttrPosition(xpp, "fillColor");
                            pathModel.setFillColor((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_FILL_COLOR);

                            tempPosition = getAttrPosition(xpp, "fillType");
                            pathModel.setFillType((tempPosition != -1) ? Utils.getFillTypeFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_FILL_TYPE);

                            tempPosition = getAttrPosition(xpp, "pathData");
                            pathModel.setPathData((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                            tempPosition = getAttrPosition(xpp, "strokeColor");
                            pathModel.setStrokeColor((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_COLOR);


                            tempPosition = getAttrPosition(xpp, "strokeWidth");
                            pathModel.setStrokeWidth((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_WIDTH);

                            pathModel.setPatternColor(patternColor);
                            pathModel.buildPath();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("path")) {
                            if (pathModel != null)
                                addPathModel(pathModel);
                        }
                        break;
                }

                patternColor += 10000;
                event = xpp.next();
            } //end while loop

            patternColor = 500;

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }


    public void setRectDraw(RectF rectf) {
        testRect = rectf;
        drawRect = true;
    }

    public void drawPaths(Canvas canvas, float offsetX, float offsetY, float scaleX, float scaleY) {

        for (PathModel pathModel : pathModels) {
            if (pathModel.isFillAndStroke()) {
                pathModel.makeFillPaint();
                canvas.drawPath(pathModel.getScaledAndOffsetPath(offsetX, offsetY, scaleX, scaleY), pathModel.getPathPaint());
                //pathModel.makeStrokePaint();
                canvas.drawPath(pathModel.getScaledAndOffsetPath(offsetX, offsetY, scaleX, scaleY), strokePaint);
            } else {
                canvas.drawPath(pathModel.getScaledAndOffsetPath(offsetX, offsetY, scaleX, scaleY), pathModel.getPathPaint());
            }
        }

        if(drawRect) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(1.0f);
            canvas.drawRect(testRect, paint);
        }
    }




    public void scaleAllPaths(Matrix scaleMatrix) {
        for (PathModel pathModel : pathModels) {
            pathModel.transform(scaleMatrix);
        }
    }

    public void scaleAllStrokeWidth(float ratio) {
        for (PathModel pathModel : pathModels) {
            pathModel.setStrokeRatio(ratio);
        }
    }

    public void addPathModel(PathModel pathModel) {

            pathModels.add(pathModel);
    }

    public List<PathModel> getPathModels() {
        return pathModels;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    private int getAttrPosition(XmlPullParser xpp, String attrName) {
        for (int i = 0; i < xpp.getAttributeCount(); i++) {
            if (xpp.getAttributeName(i).equals(attrName)) {
                return i;
            }
        }
        return -1;
    }

}
