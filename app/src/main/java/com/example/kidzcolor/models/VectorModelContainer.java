package com.example.kidzcolor.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.example.kidzcolor.interfaces.ColorDepletedListener;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorModelContainer extends VectorModel {

    private VectorEntity vectorEntity;
    private Bitmap patternMap;
    private ColorDepletedListener colorDepletedListener = null;
    private List<PathModel> coloredPathsHistory = new ArrayList<>();
    private List<PathModel> shadedModels = new ArrayList<>();
    private Map<Integer, List<PathModel>> shadeAndColorMap = new TreeMap<>();

    public VectorModelContainer(VectorEntity vectorEntity) {
        super(vectorEntity.getModel());
        this.vectorEntity = vectorEntity;
        init();
    }

    private void init(){

        int fillColor;

        for(PathModel pathModel : pathModels){

             fillColor = pathModel.getFillColor();

             if(pathModel.getFillColorStatus() == PathModel.NO_FILL_COLOR) {
                 if (shadeAndColorMap.containsKey(fillColor)) {
                     shadeAndColorMap.get(fillColor).add(pathModel);
                 } else {
                     List<PathModel> newList = new ArrayList<>();
                     newList.add(pathModel);
                     shadeAndColorMap.put(fillColor, newList);
                 }
             } else if(pathModel.getFillColorStatus() == PathModel.YES_FILL_COLOR){
                 coloredPathsHistory.add(pathModel);
             }


        }
    }

    public VectorEntity getVectorEntity(){return vectorEntity;}

    public int getId() {
        return vectorEntity.getId();
    }

    public boolean isInProgress(){
        if(coloredPathsHistory.size() > 0)
            return true;
        return false;
    }

    public boolean isCompleted(){
        if(coloredPathsHistory.size() > 0 && shadeAndColorMap.isEmpty())
            return true;
        return false;
    }



    public void saveModel(){
        if(isInProgress())
            vectorEntity.setInProgress(true);

        String model = "";

        model += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "android:height=\"" + (int)getHeight() + "dp\"\n"
                + "android:width=\"" + (int)getWidth() + "dp\"\n"
                + "android:viewportHeight=\"" + (int)getViewportHeight() + "\"\n"
                + "android:viewportWidth=\"" + (int)getViewportWidth() + "\">\n";

        for(PathModel pathModel : coloredPathsHistory) {
            model += "<path android:fillColor=\"" + pathModel.getFillColorString() + "\" "
                    + "android:isFilled=\"" + ((pathModel.getFillColorStatus() == 1) ? 1 : 0) + "\" "
                    + "android:pathData=\"" + pathModel.getPathData()  + "\"/>\n";
        }

        for(Integer integer : shadeAndColorMap.keySet()){
            for(PathModel pathModel : shadeAndColorMap.get(integer)){
                model += "<path android:fillColor=\"" + pathModel.getFillColorString() + "\" "
                        + "android:isFilled=\"" + ((pathModel.getFillColorStatus() == 1) ? 1 : 0) + "\" "
                        + "android:pathData=\"" + pathModel.getPathData()  + "\"/>\n";
            }
        }

        model += "</vector>";

        vectorEntity.setModel(model);
    }


    public void drawPatternMap() {

        int width = Utils.dpToPx((int) getWidth());
        int height = Utils.dpToPx((int) getHeight());
        patternMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(patternMap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        for (PathModel aPathModel : getPathModels()) {

                int color = aPathModel.getPatternColor();
                paint.setColor(color);
                canvas.drawPath(aPathModel.getPath(), paint);
        }

        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(20f);
        border.setColor(Color.GREEN);
        canvas.drawRect(0, 0, width, height, border);
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

    public List<Integer> getColorKeys() {
        return new ArrayList<>(shadeAndColorMap.keySet());
    }


    public void shadePaths(int colorKey) {
        shadedModels = shadeAndColorMap.get(colorKey);

        if(shadedModels != null) {
            for(PathModel pathModel : shadedModels) {
                pathModel.setFillColorStatus(PathModel.SHADE_FILL_COLOR);
                pathModel.makeFillPaint();
            }

        }

    }

    public void unShadePaths(){

        if(shadedModels != null) {
            for(PathModel pathModel : shadedModels){
                pathModel.resetPaint();
                pathModel.makeFillPaint();
            }

            shadedModels = null;
        }
    }

    public RectF getNextShadedPathBounds() {
        if(!shadedModels.isEmpty()){
            RectF rectF = new RectF();
            Path path = shadedModels.get(0).getPath();
            path.computeBounds(rectF, true);
            return rectF;
        }
        return null;
    }

    public boolean paintShadedPath(int pixelPatternColor) {

        if(!shadedModels.isEmpty()) {
            int i = 0;
            int actualColor = shadedModels.get(0).getFillColor();
            int patternColor;
            for(PathModel pathModel : shadedModels){
                patternColor = pathModel.getPatternColor();

                if(patternColor == pixelPatternColor) {
                    pathModel.setFillColorStatus(PathModel.YES_FILL_COLOR);
                    pathModel.makeFillPaint();
                    coloredPathsHistory.add(pathModel);
                    shadedModels.remove(i);

                    if(shadedModels.isEmpty()) {
                        shadeAndColorMap.remove(actualColor);
                        colorDepletedListener.notifyColorDepleted();
                    }
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    public List<PathModel> getColoredPathsHistory() {return coloredPathsHistory;}

    public void setShadedPathDepletedListener(ColorDepletedListener shadedPathDepletedListener) {
        this.colorDepletedListener = shadedPathDepletedListener;
    }


}
