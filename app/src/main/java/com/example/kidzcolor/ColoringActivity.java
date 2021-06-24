package com.example.kidzcolor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidzcolor.adapters.ColorPickerAdapter;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.viewmodels.ColoringViewModel;
import com.example.kidzcolor.zoomageview.ZoomageView;

public class ColoringActivity extends AppCompatActivity implements ImageUpdater{

    private ColoringViewModel coloringViewModel;
    private ZoomageView zoomageView;
    private VectorModel vectorModel;
    private VectorMasterDrawable vectorMasterDrawable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coloring);

        coloringViewModel = new ViewModelProvider(this).get(ColoringViewModel.class);

        vectorModel = coloringViewModel.getVectorModel();
        vectorMasterDrawable = new VectorMasterDrawable(vectorModel);

        zoomageView = findViewById(R.id.zoomage_view);
        zoomageView.setImageDrawable(vectorMasterDrawable);
        zoomageView.post(new Runnable() {
            @Override
            public void run() {
                vectorModel.drawPatternMap();
            }
        });

        setZoomageViewListener(zoomageView);
        setHintButtonListener(findViewById(R.id.hint_button));

        RecyclerView colorsRecyclerView = findViewById(R.id.coloring_recyclerView);
        colorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(this, coloringViewModel.getVectorModel(), this);
        colorsRecyclerView.setAdapter(colorPickerAdapter);
        vectorModel.setShadedPathDepletedListener(colorPickerAdapter);
    }

    private void setZoomageViewListener(ZoomageView zoomageview) {

        zoomageview.setOnTouchListener(new View.OnTouchListener() {

            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();

                        if (isAClick(startX, endX, startY, endY)) {

                            Matrix inverse = new Matrix();
                            zoomageview.getImageMatrix().invert(inverse);
                            float[] touchPoint = new float[] {endX, endY};
                            inverse.mapPoints(touchPoint);

                            int xCoord = Integer.valueOf((int)touchPoint[0]);
                            int yCoord = Integer.valueOf((int)touchPoint[1]);

                            //vectorModel.setCircle(xCoord, yCoord);

                            paintSelectedCord(xCoord, yCoord);
                        }
                       break;
                }// end switch;

                return false;
            }//end onTouch

        });//end setOnTouchListener
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {

        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);

        return !(differenceX > ViewConfiguration.get(getBaseContext()).getScaledTouchSlop()
                || differenceY > ViewConfiguration.get(getBaseContext()).getScaledTouchSlop());
    } // end isAClick

    @Override
    public void updateImage() {
        vectorMasterDrawable.invalidateSelf();
    }

    private void paintSelectedCord(int xCoord, int yCoord) {

        boolean check = vectorModel.checkIfCordInPatternMap(xCoord, yCoord);
        if(check){
            int pixelColor = vectorModel.getPixelColorFromPatternMap(xCoord, yCoord);

            if(vectorModel.paintShadedPath(pixelColor))
                vectorMasterDrawable.invalidateSelf();
        }
    }


    private void setHintButtonListener(ImageButton imageButton) {
        imageButton.setOnClickListener(new View.OnClickListener() {

            private RectF rectF;
            private float xCenter;
            private float yCenter;
            private float scaleFactor;
            private float originalScaleFactor;
            private Matrix inverse = new Matrix();

            @Override
            public void onClick(View v) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayHeight = displayMetrics.heightPixels;
                int displayWidth = displayMetrics.widthPixels;


                rectF = vectorModel.getNextShadedPathBounds();

                RectF drawRect = new RectF(0, 0, vectorMasterDrawable.getIntrinsicWidth(), vectorMasterDrawable.getIntrinsicHeight());
                //inverse.setRectToRect(rectF, drawRect, Matrix.ScaleToFit.CENTER);

                float yOffset = drawRect.centerY() / 2;
                float translateY = (yOffset - rectF.top) / 2;
                float xOffset = drawRect.centerX() / 2;
                float translateX = (xOffset - rectF.left) / 2;

                final float[] values = new float[9];
                zoomageView.getImageMatrix().getValues(values);
                float xScale = values[Matrix.MSCALE_X];
                float yScale = values[Matrix.MSCALE_Y];
                float yTranslate = values[Matrix.MTRANS_Y];
                float xTranslate = values[Matrix.MTRANS_X];


                //inverse.setTranslate(translateX, translateY );


                inverse.setScale(1 ,1);
                inverse.postTranslate(0, 0);
                //inverse.postTranslate(displayWidth - 1000, displayHeight - 1000);
                zoomageView.animateScaleAndTranslationToMatrix(inverse, 1000);

/*                inverse.postTranslate(drawRect.centerX() / 2, drawRect.centerY() / 2);
                zoomageView.animateScaleAndTranslationToMatrix(inverse, 10000);*/
                xCenter = translateX;
                yCenter = translateY;
                vectorModel.setCircle(xCenter, yCenter, true);


                drawRect.right = (drawRect.right - (drawRect.right / 2));
                vectorModel.setRectDraw(drawRect);



               /* xCenter = rectF.centerX();
                yCenter = rectF.centerY();
                //vectorModel.setCircle(xCenter, yCenter, true);


                zoomageView.getImageMatrix().invert(inverse);
                inverse.invert(inverse);
                float[] touchPoint = new float[] {xCenter, yCenter};
                inverse.mapPoints(touchPoint);
                xCenter = touchPoint[0];
                yCenter = touchPoint[1];


                //zoomageView.reset();
                scaleFactor = getZoomScaleFactor();
                originalScaleFactor = zoomageView.getDoubleTapToZoomScaleFactor();
                zoomageView.setDoubleTapToZoomScaleFactor(scaleFactor);
                fireZoomageViewDoubleTap();
                zoomageView.setDoubleTapToZoomScaleFactor(originalScaleFactor);
            }


            private void fireZoomageViewDoubleTap() {
                MotionEvent motionEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis() + 100,
                        MotionEvent.ACTION_DOWN,
                        xCenter,
                        yCenter,
                        0
                );
                zoomageView.onTouchEvent(motionEvent);

                motionEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis() + 200,
                        SystemClock.uptimeMillis() + 300,
                        MotionEvent.ACTION_UP,
                        xCenter,
                        yCenter,
                        0
                );
                zoomageView.onTouchEvent(motionEvent);
                motionEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis() + 400,
                        SystemClock.uptimeMillis() + 500,
                        MotionEvent.ACTION_DOWN,
                        xCenter,
                        yCenter,
                        0
                );
                zoomageView.onTouchEvent(motionEvent);
                motionEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis() +600,
                        SystemClock.uptimeMillis() + 700,
                        MotionEvent.ACTION_UP,
                        xCenter,
                        yCenter,
                        0
                );
                zoomageView.onTouchEvent(motionEvent);*/
            }

            private float getZoomScaleFactor() {

                float width = vectorModel.getWidth();
                float width2 = rectF.width();

                if(rectF.height() > vectorModel.getHeight())
                    return  (vectorModel.getHeight() / rectF.height());
                else
                    return  (vectorModel.getWidth() / rectF.width());
            }
        });
    }
}
