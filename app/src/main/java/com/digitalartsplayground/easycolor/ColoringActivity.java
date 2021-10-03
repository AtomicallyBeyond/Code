package com.digitalartsplayground.easycolor;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import com.digitalartsplayground.easycolor.interfaces.ZoomListener;
import com.digitalartsplayground.easycolor.adapters.ColorPickerAdapter;
import com.digitalartsplayground.easycolor.interfaces.FinishedColoringListener;
import com.digitalartsplayground.easycolor.interfaces.PositionListener;
import com.digitalartsplayground.easycolor.models.ColoringVectorDrawable;
import com.digitalartsplayground.easycolor.models.ReplayDrawable;
import com.digitalartsplayground.easycolor.models.VectorModelContainer;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.ColoringViewModel;
import com.digitalartsplayground.easycolor.zoomageview.ZoomageView;
import java.util.ArrayList;
import java.util.Arrays;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


public class ColoringActivity extends AppCompatActivity implements PositionListener, FinishedColoringListener, ZoomListener {

    private ColoringViewModel coloringViewModel;
    private ZoomageView zoomageView;
    private VectorModelContainer vectorModelContainer;
    private ColoringVectorDrawable coloringVectorDrawable;
    private RecyclerView colorsRecyclerView;
    private int drawingHeight;
    private int drawingWidth;
    private ColorPickerAdapter colorPickerAdapter;
    private KonfettiView konfettiView;
    private float xScale;
    private float yScale;
    private ProgressBar hintProgressBar;
    private boolean hintAvailable = true;
    private ImageButton zoomOutButton;
    private ObjectAnimator hintAnimator;
    private FrameLayout adContainer;


    @Override
    protected void onDestroy() {

        if(BaseActivity.ironSourceLoaded) {
            BaseActivity.destroyIronSourceAd();
        }

        adContainer = null;
        super.onDestroy();
    }

    private void loadIronSource() {

        adContainer = findViewById(R.id.ironsource_container);
        BaseActivity.loadIronSource(adContainer);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coloring);
        hideSystemUI();

        coloringViewModel = new ViewModelProvider(this).get(ColoringViewModel.class);

        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            if(BaseActivity.counter < 5)
                loadIronSource();
        }


        konfettiView  = findViewById(R.id.konfetti);
        zoomOutButton = findViewById(R.id.zoom_out_button);
        hintProgressBar = findViewById(R.id.hint_progress_bar);
        animateProgress(hintProgressBar, 100);
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
        setHintButtonListener(findViewById(R.id.coloring_hint_button));
        setBackButtonListener(findViewById(R.id.coloring_back_button));
        setPlayButtonListener(findViewById(R.id.coloring_play_button));
        setResetButtonListener(findViewById(R.id.coloring_reset_button));
        initRecylerView();
        subscribeViewStateObserver();
    }


    private void initZoomageView() {
        coloringVectorDrawable = new ColoringVectorDrawable(vectorModelContainer);
        zoomageView = findViewById(R.id.zoomage_view);
        zoomageView.setImageDrawable(coloringVectorDrawable);
        int a = zoomageView.getHeight();
        int b = zoomageView.getWidth();
        zoomageView.post(new Runnable() {
            @Override
            public void run() {
                vectorModelContainer.drawPatternMap();
                drawingHeight = zoomageView.getHeight();
                drawingWidth = zoomageView.getWidth();
                zoomageView.setScaleType(ImageView.ScaleType.MATRIX);
                zoomageView.setStartValues();
                xScale = (float) drawingWidth / (float) coloringVectorDrawable.getIntrinsicWidth();
                yScale = (float) drawingHeight / (float) coloringVectorDrawable.getIntrinsicHeight();
                zoomageView.setZoomListener(ColoringActivity.this);
                setZoomageViewListener(zoomageView);
                setZoomOutButtonListener(zoomOutButton);
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
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

                            Matrix inverse = new Matrix();
                            zoomageview.getImageMatrix().invert(inverse);
                            float[] touchPoint = new float[] {endX, endY};
                            inverse.mapPoints(touchPoint);

                            int xCoord = (int) touchPoint[0];
                            int yCoord = (int) touchPoint[1];

                            paintSelectedCord(xCoord, yCoord);

                       break;
                }// end switch;

                return false;
            }//end onTouch

        });//end setOnTouchListener
    }


    private void paintSelectedCord(int xCoord, int yCoord) {

        boolean check = vectorModelContainer.checkIfCordInPatternMap(xCoord, yCoord);
        if(check){
            int pixelColor = vectorModelContainer.getPixelColorFromPatternMap(xCoord, yCoord);

            if(vectorModelContainer.paintShadedPath(pixelColor))
                coloringVectorDrawable.invalidateSelf();
        }
    }


    private void setHintButtonListener(ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {

            private RectF shadedPathBounds;
            private float xCenter;
            private float yCenter;
            private Matrix inverse = new Matrix();
            private ProgressBar progressBar;

            @Override
            public void onClick(View v) {

                if(hintAvailable) {

                    shadedPathBounds = vectorModelContainer.getNextShadedPathBounds();
                    if(shadedPathBounds == null)
                        return;

                    progressBar = ColoringActivity.this.hintProgressBar;

                    xCenter = shadedPathBounds.centerX();
                    yCenter = shadedPathBounds.centerY();

                    float scaleFactor = getZoomScaleFactor();
                    float x = (float)((xCenter * scaleFactor) - (drawingWidth * 0.5));
                    float y = (float)((yCenter * scaleFactor) - (drawingHeight * 0.5));

                    inverse.setScale(scaleFactor, scaleFactor);
                    inverse.postTranslate(-x, -y);

                    zoomageView.animateScaleAndTranslationToMatrix(inverse, 500);

                    animateProgress(progressBar, 10000);
                    hintAvailable = false;

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hintAvailable = true;
                        }
                    }, 10000);
                }

            }

            private float getZoomScaleFactor() {

                float zoomScale;
                float pathHeight = shadedPathBounds.height();
                float pathWidth = shadedPathBounds.width();

                if(pathHeight > pathWidth) {

                    float a = drawingHeight / pathHeight;

                    zoomScale = a * 0.4f;

                    if(zoomScale > 5f)
                        zoomScale = 5f;
                    else if(zoomScale < yScale)
                        zoomScale = yScale;

                    return zoomScale;

                } else {

                    float a = drawingWidth / pathWidth;

                    zoomScale = a * 0.5f;

                    if(zoomScale > 5f)
                        zoomScale = 5f;
                    else if(zoomScale < xScale)
                        zoomScale = xScale;

                    return zoomScale;

                }
            }
        });
    }

    private void animateProgress(ProgressBar progressBar, int duration) {
        hintAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        hintAnimator.setDuration(duration);
        hintAnimator.setInterpolator(new LinearInterpolator());
        hintAnimator.start();
    }

    private void resetProgressAnimation() {
        if(hintAnimator.isRunning()) {
            hintAnimator.cancel();
            animateProgress(hintProgressBar, 100);
            hintAvailable = true;
        }
    }

    private void setZoomOutButtonListener(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomageView
                        .animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 500);
                zoomOutButton.setVisibility(View.GONE);
            }
        });
    }

    private void setBackButtonListener(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setPlayButtonListener(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomageView.reset(false);
                startReplay();
            }
        });
    }

    private void setResetButtonListener(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomageView.reset(false);
                resetVectorModel();
            }
        });
    }

    private void initRecylerView() {
        colorsRecyclerView = findViewById(R.id.coloring_recyclerView);
        colorsRecyclerView.setHasFixedSize(true);

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
                new ArrayList<>(Arrays.asList(this, coloringViewModel)),
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
        coloringVectorDrawable.invalidateSelf();
        colorsRecyclerView.smoothScrollToPosition(newPosition);
    }

    @Override
    public void finished() {

        zoomageView.animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 400);

        startKonfetti();
        resetProgressAnimation();

        final Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout constraintLayout = findViewById(R.id.coloring_toolbar);
                revealView(constraintLayout);
                zoomageView.animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 10);
                startReplay();
            }
        }, 4000);

    }

    private void startKonfetti() {
        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 1000L);
    }


    private void subscribeViewStateObserver() {

        coloringViewModel.getIsCompleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ConstraintLayout constraintLayout = findViewById(R.id.coloring_toolbar);
                if(aBoolean) {
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
        view.setVisibility(View.GONE);
    }

    private void startReplay(){
        ReplayDrawable replayDrawable = new ReplayDrawable(vectorModelContainer);
        zoomageView.setImageDrawable(replayDrawable);
        replayDrawable.startReplay();
    }

    @Override
    public void isZoomed(boolean isImageZoomed) {
        if(isImageZoomed) {
            zoomOutButton.setVisibility(View.VISIBLE);
        } else {
            zoomOutButton.setVisibility(View.GONE);
        }
    }



    @SuppressWarnings("deprecation")
    private void hideSystemUI() {

        if(Build.VERSION.SDK_INT < 30) {

            @SuppressLint("WrongConstant") final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(flags);

            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int i) {
                    if((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });

        } else {

            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES; }
    }

}