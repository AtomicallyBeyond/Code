package com.example.kidzcolor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.kidzcolor.models.PathModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ColoringReplay extends AppCompatImageView {

    private int drawIndex = -1;
    private int listSize = 0;
    private List<PathModel> pathsList;
    private Paint outlinePaint;



    public ColoringReplay(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(Color.BLACK);
    }

    public void setPathsList(List<PathModel> pathsList) {
        this.pathsList = pathsList;
        listSize = pathsList.size();
    }

    public void startReplay() {
        new Timer().scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                drawIndex++;

                if( drawIndex == listSize){
                    drawIndex = -1;
                    this.cancel();
                }

                if(drawIndex < listSize && drawIndex >= 0)
                    ColoringReplay.this.invalidate();
            }
        }, 0, 1000);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

            int index = 0;
            for(PathModel pathModel : pathsList) {

                if(index <= drawIndex)
                    canvas.drawPath(pathModel.getPath(), pathModel.getPathPaint());
                else
                    canvas.drawPath(pathModel.getPath(), outlinePaint);

                index++;
            }
    }

}
