package com.example.kidzcolor;

import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.example.kidzcolor.adapters.ColorPickerAdapter;
import com.example.kidzcolor.interfaces.FinishedColoringListener;
import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.models.ReplayDrawable;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.mvvm.viewmodels.ColoringViewModel;
import com.example.kidzcolor.zoomageview.ZoomageView;

import java.util.ArrayList;
import java.util.Arrays;

public class ColoringActivity extends AppCompatActivity implements PositionListener, FinishedColoringListener {

    private ColoringViewModel coloringViewModel;
    private ZoomageView zoomageView;
    private VectorModelContainer vectorModelContainer;
    private VectorMasterDrawable vectorMasterDrawable;
    private int displayHeight;
    private int displayWidth;
    private float minScale;
    private float maxScale;
    private ColorPickerAdapter colorPickerAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coloring);

        //need to use layout measurment because getMetrics is deprecated
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        coloringViewModel = new ViewModelProvider(this).get(ColoringViewModel.class);
        observeModelFromRepository();
    }

    private void observeModelFromRepository() {

        LiveData<VectorModelContainer> liveData = coloringViewModel.getVectorModelContainer();
        liveData.observe(this, new Observer<VectorModelContainer>() {
            @Override
            public void onChanged(VectorModelContainer vectorModelContainer) {
                ColoringActivity.this.vectorModelContainer = vectorModelContainer;
                init();
                if(vectorModelContainer.isCompleted()) {
                    revealView(findViewById(R.id.coloring_toolbar));
                }
            }
        });
    }

    private void init() {
        initZoomageView();
        setZoomageViewListener(zoomageView);
        setHintButtonListener(findViewById(R.id.coloring_hint_button));
        setBackButtonListener(findViewById(R.id.coloring_back_button));
        setPlayButtonListener(findViewById(R.id.coloring_play_button));
        setResetButtonListener(findViewById(R.id.coloring_reset_button));
        initRecylerView();
        subscribeViewStateObserver();
    }

    private void resetActivity() {

    }


    private void initZoomageView() {
        vectorMasterDrawable = new VectorMasterDrawable(vectorModelContainer);
        zoomageView = findViewById(R.id.zoomage_view);
        zoomageView.setImageDrawable(vectorMasterDrawable);
        zoomageView.post(new Runnable() {
            @Override
            public void run() {
                vectorModelContainer.drawPatternMap();
                zoomageView.setScaleType(ImageView.ScaleType.MATRIX);
                zoomageView.setStartValues();
                minScale = zoomageView.getDefaultScale();
                maxScale = zoomageView.getCalculatedMaxScale();
            }
        });

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

        boolean check = vectorModelContainer.checkIfCordInPatternMap(xCoord, yCoord);
        if(check){
            int pixelColor = vectorModelContainer.getPixelColorFromPatternMap(xCoord, yCoord);

            if(vectorModelContainer.paintShadedPath(pixelColor))
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

                rectF = vectorModelContainer.getNextShadedPathBounds();
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
                vectorModelContainer.setRectDraw(rectF);

            }

            private float getZoomScaleFactor() {

                float zoomScale;

                if(rectF.height() > rectF.width()){
                    zoomScale =
                            (vectorMasterDrawable.getIntrinsicHeight() / rectF.height()) * (zoomageView.getDefaultScale() / 2);
                    return getScaleInRange(zoomScale);
                } else {
                    zoomScale =
                            (vectorMasterDrawable.getIntrinsicWidth() / rectF.width()) * (zoomageView.getDefaultScale() / 3);
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

    private void setBackButtonListener(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                coloringViewModel.saveVectorModel();
                finish();
            }
        });
    }

    private void setPlayButtonListener(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReplay();
            }
        });
    }

    private void setResetButtonListener(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetVectorModel();
            }
        });
    }

    private void initRecylerView() {
        RecyclerView colorsRecyclerView = findViewById(R.id.coloring_recyclerView);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            colorsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            colorsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        }

        colorPickerAdapter = new ColorPickerAdapter(
                this,
                vectorModelContainer,
                new ArrayList<PositionListener>(Arrays.asList(this, coloringViewModel)),
                this);

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
        vectorModelContainer.setShadedPathDepletedListener(colorPickerAdapter);
    }

    public void resetVectorModel() {
        coloringViewModel.resetVectorModel();
    }



    @Override
    public void positionChanged(int newPosition) {
        vectorMasterDrawable.invalidateSelf();
        zoomageView.animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 500);
    }

    @Override
    public void finished() {
        ConstraintLayout constraintLayout = findViewById(R.id.coloring_toolbar);
        revealView(constraintLayout);
        startReplay();
    }


    private void subscribeViewStateObserver() {

        coloringViewModel.getIsCompleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ConstraintLayout constraintLayout = findViewById(R.id.coloring_toolbar);
                if(aBoolean) {
                    //startReplay();
                    revealView(constraintLayout);
                } else {
                    hideReset(constraintLayout);
                }
            }
        });
    }

    private void revealView(View view) {

        Transition transition;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            transition = new Slide(Gravity.TOP);
        } else {
            transition = new Slide(Gravity.LEFT);
        }
        transition.setDuration(500);
        transition.addTarget(view);
        TransitionManager.beginDelayedTransition(findViewById(R.id.coloring_parent), transition);
        view.setVisibility(View.VISIBLE);
    }

    private void hideReset(View view) {
        findViewById(R.id.coloring_imageview).setVisibility(View.GONE);
        view.setVisibility(View.GONE);

    }

    private void startReplay(){
        zoomageView.animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 100);
        ReplayDrawable replayDrawable = new ReplayDrawable(vectorModelContainer);
        zoomageView.setImageDrawable(replayDrawable);
        replayDrawable.startReplay();
    }
}