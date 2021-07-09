package com.example.kidzcolor.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.example.kidzcolor.interfaces.ColorDepletedListener;
import com.example.kidzcolor.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VectorModelContainer extends VectorModel {

    private Bitmap patternMap;
    private ColorDepletedListener colorDepletedListener = null;
    private List<PathModel> coloredPathsHistory = new ArrayList<>();
    private List<PathModel> shadedModels = new ArrayList<>();
    private Map<Integer, List<PathModel>> shadeAndColorMap = new TreeMap<>();

    public VectorModelContainer(File xmlFile) {
        super(xmlFile);
        init();
    }

    private void init(){

        int fillColor;

        for(PathModel pathModel : pathModels){

             fillColor = pathModel.getFillColor();

            if (shadeAndColorMap.containsKey(fillColor)) {
                shadeAndColorMap.get(fillColor).add(pathModel);
            } else {
                List<PathModel> newList = new ArrayList<>();
                newList.add(pathModel);
                shadeAndColorMap.put(fillColor, newList);
            }
        }
    }


    public void drawPatternMap() {

        int width = Utils.dpToPx((int) getWidth());
        int height = Utils.dpToPx((int) getHeight());
        patternMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(patternMap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        for (PathModel aPathModel : getPathModels()) {

                paint.setColor(aPathModel.getPatternColor());
                canvas.drawPath(aPathModel.getPath(), paint);
        }

        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(20f);
        border.setColor(Color.GREEN);
        canvas.drawRect(0, 0, width, height, border);
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
