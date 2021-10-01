package com.digitalartsplayground.easycolor.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.digitalartsplayground.easycolor.interfaces.ColorDepletedListener;
import com.digitalartsplayground.easycolor.persistance.VectorEntity;
import com.digitalartsplayground.easycolor.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorModelContainer extends ColoringVectorModel {

    private VectorEntity vectorEntity;
    private Bitmap patternMap;
    private ColorDepletedListener colorDepletedListener = null;
    private List<ColoringPathModel> coloredPathsHistory = new ArrayList<>();
    private List<ColoringPathModel> shadedModels = new ArrayList<>();
    private Map<Integer, List<ColoringPathModel>> shadeAndColorMap = new TreeMap<>();


    public VectorModelContainer(VectorEntity vectorEntity) {
        super(vectorEntity.getModel());
        this.vectorEntity = vectorEntity;
        init();
    }

    private void init(){

        int fillColor;

        for(ColoringPathModel coloringPathModel : coloringPathModels){

             fillColor = coloringPathModel.getFillColor();

             if(coloringPathModel.getFillColorStatus() == ColoringPathModel.NO_FILL_COLOR) {
                 if (shadeAndColorMap.containsKey(fillColor)) {
                     shadeAndColorMap.get(fillColor).add(coloringPathModel);
                 } else {
                     List<ColoringPathModel> newList = new ArrayList<>();
                     newList.add(coloringPathModel);
                     shadeAndColorMap.put(fillColor, newList);
                 }
             } else if(coloringPathModel.getFillColorStatus() == ColoringPathModel.YES_FILL_COLOR){
                 coloredPathsHistory.add(coloringPathModel);
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

        for(ColoringPathModel coloringPathModel : coloredPathsHistory) {
            model += "<path android:fillColor=\"" + coloringPathModel.getFillColorString() + "\" "
                    + "android:isFilled=\"" + ((coloringPathModel.getFillColorStatus() == 1) ? 1 : 0) + "\" "
                    + "android:pathData=\"" + coloringPathModel.getPathData()  + "\"/>\n";
        }

        for(Integer integer : shadeAndColorMap.keySet()){
            for(ColoringPathModel coloringPathModel : shadeAndColorMap.get(integer)){
                model += "<path android:fillColor=\"" + coloringPathModel.getFillColorString() + "\" "
                        + "android:isFilled=\"" + ((coloringPathModel.getFillColorStatus() == 1) ? 1 : 0) + "\" "
                        + "android:pathData=\"" + coloringPathModel.getPathData()  + "\"/>\n";
            }
        }

        model += "</vector>";

        vectorEntity.setModel(model);
        vectorEntity.loadDrawable();
    }


    public void drawPatternMap() {

        int width = Utils.dpToPx((int) getWidth());
        int height = Utils.dpToPx((int) getHeight());
        patternMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(patternMap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        for (ColoringPathModel aColoringPathModel : getPathModels()) {

                int color = aColoringPathModel.getPatternColor();
                paint.setColor(color);
                canvas.drawPath(aColoringPathModel.getPath(), paint);
        }

        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(20f);
        border.setColor(Color.GREEN);
        canvas.drawRect(0, 0, width, height, border);
    }

    public boolean checkIfCordInPatternMap(int xCoord, int yCoord) {

        if(patternMap == null)
            drawPatternMap();

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
            for(ColoringPathModel coloringPathModel : shadedModels) {
                coloringPathModel.setFillColorStatus(ColoringPathModel.SHADE_FILL_COLOR);
                coloringPathModel.makeFillPaint();
            }

        }

    }

    public void unShadePaths(){

        if(shadedModels != null) {
            for(ColoringPathModel coloringPathModel : shadedModels){
                coloringPathModel.resetPaint();
                coloringPathModel.makeFillPaint();
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
            for(ColoringPathModel coloringPathModel : shadedModels){
                patternColor = coloringPathModel.getPatternColor();

                if(patternColor == pixelPatternColor) {
                    coloringPathModel.setFillColorStatus(PathModel.YES_FILL_COLOR);
                    coloringPathModel.makeFillPaint();
                    coloredPathsHistory.add(coloringPathModel);
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

    public List<ColoringPathModel> getColoredPathsHistory() {return coloredPathsHistory;}

    public void setShadedPathDepletedListener(ColorDepletedListener shadedPathDepletedListener) {
        this.colorDepletedListener = shadedPathDepletedListener;
    }


}
