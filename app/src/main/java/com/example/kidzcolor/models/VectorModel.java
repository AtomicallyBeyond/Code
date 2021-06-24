package com.example.kidzcolor.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.kidzcolor.ShadedPathsDepletedListener;
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
import java.util.Set;
import java.util.TreeMap;

public class VectorModel {

    //new variables
    private Context context;
    String assetVectorName;
    private XmlPullParser xpp;
    private boolean useLegacyParser = true;
    Bitmap patternMap;
    private Paint strokePaint;
    private List<PathModel> shadedModels = null;
    private ShadedPathsDepletedListener shadedPathsDepletedListener = null;
    float xC;
    float yC;
    boolean yCircle = false;
    RectF testRect;
    boolean drawRect = false;

    public void setRectDraw(RectF rectf) {
        testRect = rectf;
        drawRect = true;
    }

    public void setCircle (float x, float y, boolean yCircle) {
        xC = x;
        yC = y;
        this.yCircle = yCircle;
    }

    //old variables
    private float width, height;
    private float viewportWidth, viewportHeight;
    final private ArrayList<PathModel> pathModels = new ArrayList<>();
    private Map<Integer, List<PathModel>> fillPathsMap = new TreeMap<>();
    private Map<Integer, List<PathModel>> shadeAndColorMap = new TreeMap<>();
    private Path fullpath = new Path();

    public VectorModel(Context context, String assetVectorName) {
        this.context = context;
        this.assetVectorName = assetVectorName;
        init();
    }

    private void init() {
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(2.0f);
        buildVectorModel();
    }

    private void buildVectorModel() {

        PathModel pathModel = null;
        int tempPosition;


        InputStream inputStream = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            AssetManager assetManager = context.getAssets();
            inputStream = assetManager.open(assetVectorName);
            xpp.setInput(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return;
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

                            tempPosition = getAttrPosition(xpp, "name");
                            pathModel.setName((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                            tempPosition = getAttrPosition(xpp, "fillColor");
                            pathModel.setFillColor((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_FILL_COLOR);

                            tempPosition = getAttrPosition(xpp, "fillType");
                            pathModel.setFillType((tempPosition != -1) ? Utils.getFillTypeFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_FILL_TYPE);

                            tempPosition = getAttrPosition(xpp, "pathData");
                            String tem = xpp.getAttributeValue(tempPosition);
                            pathModel.setPathData((tempPosition != -1) ? xpp.getAttributeValue(tempPosition) : null);

                            tempPosition = getAttrPosition(xpp, "strokeColor");
                            pathModel.setStrokeColor((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_COLOR);


                            tempPosition = getAttrPosition(xpp, "strokeWidth");
                            pathModel.setStrokeWidth((tempPosition != -1) ? Float.parseFloat(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_STROKE_WIDTH);


                            tempPosition = getAttrPosition(xpp, "patternColor");
                            pathModel.setPatternColor((tempPosition != -1) ? Utils.getColorFromString(xpp.getAttributeValue(tempPosition)) : DefaultValues.PATH_PATTERN_COLOR);

                            pathModel.buildPath();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("path")) {
                            if (pathModel != null)
                                addPathModel(pathModel);
                            fullpath.addPath(pathModel.getPath());
                        }
                        break;
                }
                event = xpp.next();

            } //end while loop

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public void drawPatternMap() {

        int width = Utils.dpToPx((int) getWidth());
        int height = Utils.dpToPx((int) getHeight());
        patternMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(patternMap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        for (PathModel aPathModel : pathModels) {

            if (!aPathModel.getName().equals("outline")) {
                paint.setColor(aPathModel.getPatternColor());
                canvas.drawPath(aPathModel.getPath(), paint);
            }
        }

        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(20f);
        border.setColor(Color.GREEN);
        canvas.drawRect(0, 0, width, height, border);
        int a = 1;

    }

    public Bitmap getPatternMap() {
        return patternMap;
    }

    public boolean checkIfCordInPatternMap(int xCoord, int yCoord) {
        int width = patternMap.getWidth();
        int height = patternMap.getHeight();

        if(xCoord < (width - 1) && yCoord < (height - 1)) {
            if(xCoord > 0 && yCoord > 0) {
                return true;
            }
        }
        return false;
    }

    public int getPixelColorFromPatternMap(int xCoord, int yCoord) {
        int pixel = patternMap.getPixel(xCoord,yCoord);
        return pixel;
    }

    public boolean paintShadedPath(int pixelPatternColor) {

        if(shadedModels != null) {
            int i = 0;
            int actualColor = shadedModels.get(0).getFillColor();
            int patternColor;
            for(PathModel pathModel : shadedModels){
                patternColor = pathModel.getPatternColor();

                if(patternColor == pixelPatternColor) {
                    pathModel.setFillColorStatus(PathModel.YES_FILL_COLOR);
                    shadedModels.remove(i);

                    if(shadedModels.isEmpty()) {
                        shadeAndColorMap.remove(actualColor);
                        shadedPathsDepletedListener.notifyShadedPathsDepleted();
                    }
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    public void setShadedPathDepletedListener(ShadedPathsDepletedListener shadedPathDepletedListener) {
        this.shadedPathsDepletedListener = shadedPathDepletedListener;
    }


    public List<Integer> getColorKeys() {
        return new ArrayList<>(shadeAndColorMap.keySet());
    }



    public void shadePaths(int colorKey) {
        shadedModels = shadeAndColorMap.get(colorKey);

        if(shadedModels != null) {
            for(PathModel pathModel : shadedModels)
                pathModel.setFillColorStatus(PathModel.SHADE_FILL_COLOR);
        }

    }

    public void unShadePaths(){

        if(shadedModels != null) {
            for(PathModel pathModel : shadedModels)
                pathModel.resetPaint();
            shadedModels = null;
        }
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

        if(yCircle) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawCircle(xC, yC, 40f, paint);
        }

        if(drawRect) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(10.0f);
            canvas.drawRect(testRect, paint);
        }
    }

    public RectF getNextShadedPathBounds() {
        if(shadedModels != null){
            RectF rectF = new RectF();
            Path path = shadedModels.get(0).getPath();
            path.computeBounds(rectF, true);

            return rectF;
        }
        return null;
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

        int fillColor = pathModel.getFillColor();

        if(fillPathsMap.containsKey(fillColor)) {
            fillPathsMap.get(fillColor).add(pathModel);
            shadeAndColorMap.get(fillColor).add(pathModel);
        } else {
            List<PathModel> newList = new ArrayList<>();
            List<PathModel> newList2 = new ArrayList<>();
            newList.add(pathModel);
            fillPathsMap.put(fillColor, newList);
            shadeAndColorMap.put(fillColor, newList2);
        }
    }

    public ArrayList<PathModel> getPathModels() {
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
