package com.example.kidzcolor;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidzcolor.adapters.ColorPickerAdapter;
import com.example.kidzcolor.intefaces.FinishedColoringListener;
import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.viewmodels.ColoringViewModel;
import com.example.kidzcolor.zoomageview.ZoomageView;

import java.util.ArrayList;
import java.util.Arrays;

public class ColoringActivity extends AppCompatActivity implements PositionListener, FinishedColoringListener {

    private ColoringViewModel coloringViewModel;
    private ZoomageView zoomageView;
    private VectorModelContainer vectorModel;
    private VectorMasterDrawable vectorMasterDrawable;
    private int displayHeight;
    private int displayWidth;
    private float minScale;
    private float maxScale;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coloring);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        coloringViewModel = new ViewModelProvider(this).get(ColoringViewModel.class);

        coloringViewModel.getVectorModel().observe(this, new Observer<VectorModel>() {
            @Override
            public void onChanged(VectorModel localModel) {

                vectorModel = coloringViewModel.getVectorModel().getValue();
                init();
            }
        });
    }

    private void init() {
        vectorMasterDrawable = new VectorMasterDrawable(vectorModel);

        zoomageView = findViewById(R.id.zoomage_view);
        zoomageView.setImageDrawable(vectorMasterDrawable);
        zoomageView.post(new Runnable() {
            @Override
            public void run() {
                vectorModel.drawPatternMap();
                zoomageView.setScaleType(ImageView.ScaleType.MATRIX);
                zoomageView.setStartValues();
                minScale = zoomageView.getDefaultScale();
                maxScale = zoomageView.getCalculatedMaxScale();
            }
        });


        setZoomageViewListener(zoomageView);
        setHintButtonListener(findViewById(R.id.hint_button));

        RecyclerView colorsRecyclerView = findViewById(R.id.coloring_recyclerView);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            colorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            colorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        }

        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(this, coloringViewModel.getVectorModel().getValue(), new ArrayList<PositionListener>(Arrays.asList(this, coloringViewModel)), this);
        colorsRecyclerView.setAdapter(colorPickerAdapter);
        colorsRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int position = coloringViewModel.getPosition();
                colorsRecyclerView.scrollToPosition(position);
                View view  = colorsRecyclerView.getLayoutManager().findViewByPosition(position);
                if(view != null) {
                    view.callOnClick();
                }
            }
        });
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
            private Matrix inverse = new Matrix();

            @Override
            public void onClick(View v) {

                rectF = vectorModel.getNextShadedPathBounds();
                if(rectF == null)
                    return;

                xCenter = rectF.centerX();
                yCenter = rectF.centerY();

                ZoomageView zoom = zoomageView;
                float scaleFactor = getZoomScaleFactor();
                float x = (float)((xCenter * scaleFactor) - (displayWidth * 0.5));
                float y = (float)((yCenter * scaleFactor) - (displayHeight * 0.5));

                inverse.setScale(scaleFactor, scaleFactor);
                inverse.postTranslate(-x, -y);

                zoomageView.animateScaleAndTranslationToMatrix(inverse, 500);;
                vectorModel.setRectDraw(rectF);

            }

            private float getZoomScaleFactor() {

                float zoomScale;

                if(rectF.height() > rectF.width()){
                    zoomScale = (vectorMasterDrawable.getIntrinsicHeight() / rectF.height()) * (zoomageView.getDefaultScale() / 2);
                    return getScaleInRange(zoomScale);
                } else {
                    zoomScale = (vectorMasterDrawable.getIntrinsicWidth() / rectF.width()) * (zoomageView.getDefaultScale() / 2);
                    return getScaleInRange(zoomScale);
                }

            }

            private float getScaleInRange(float zoomScale){
                if(zoomScale <= minScale)
                    return minScale;
                else if(zoomScale >= maxScale)
                    return maxScale;

                return  zoomScale;
            }
        });
    }

    @Override
    public void positionChanged(int newPosition) {
        vectorMasterDrawable.invalidateSelf();
        zoomageView.animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 500);
    }

    @Override
    public void finished() {
        Intent coloringIntent = new Intent(this, CompletedActivity.class);
        startActivity(coloringIntent);
    }
}



/*                zoomageView.animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 1000);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        zoomageView.animateScaleAndTranslationToMatrix(inverse, 2000);
                    }
                }, 1200);*/

