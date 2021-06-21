package com.example.kidzcolor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
import com.jsibbold.zoomage.ZoomageView;

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
                //((ImageView)findViewById(R.id.patternImage)).setImageBitmap(vectorModel.getPatternMap());
            }
        });

        setZoomageViewListener(zoomageView);

        RecyclerView colorsRecyclerView = findViewById(R.id.coloring_recyclerView);
        colorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        colorsRecyclerView.setAdapter( new ColorPickerAdapter(this, coloringViewModel.getVectorModel(), this));

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
}
