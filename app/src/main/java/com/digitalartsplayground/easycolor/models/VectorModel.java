package com.digitalartsplayground.easycolor.models;

import android.graphics.Canvas;
import android.graphics.Matrix;

import com.digitalartsplayground.easycolor.utils.DefaultValues;
import com.digitalartsplayground.easycolor.utils.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VectorModel {

    private float width, height;
    private float viewportWidth, viewportHeight;
    protected List<PathModel> pathModels = new ArrayList<>();

    public VectorModel(String model){
            buildVectorModel(model);
    }

    private void buildVectorModel(String model) {
        XmlPullParser xpp;

        PathModel pathModel = null;
        int tempPosition;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            InputStream inputStream = new ByteArrayInputStream(model.getBytes(StandardCharsets.UTF_8));
            xpp.setInput(new InputStreamReader(inputStream));
        } catch (XmlPullParserException e) {
            return;
        }


        try {

            int event = xpp.getEventType();

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

                            tempPosition = getAttrPosition(xpp, "isFilled");
                            pathModel.setFillColorStatus((tempPosition != -1) ? Integer.parseInt(xpp.getAttributeValue(tempPosition)) : 0);

                            tempPosition = getAttrPosition(xpp, "pathData");
                            pathModel.buildPath((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("path")) {
                            if (pathModel != null)
                                addPathModel(pathModel);
                        }
                        break;
                }
                event = xpp.next();
            } //end while loop


        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public void drawPaths(Canvas canvas) {

        for(PathModel pathModel : pathModels)
            pathModel.drawPath(canvas);
    }

    public void scaleAllPaths(Matrix scaleMatrix) {
        for (PathModel pathModel : pathModels) {
            pathModel.transform(scaleMatrix);
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
