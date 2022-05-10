package com.digitalartsplayground.easycolor.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import com.digitalartsplayground.easycolor.R;
import com.digitalartsplayground.easycolor.adapters.ColorPickerAdapter;
import com.digitalartsplayground.easycolor.interfaces.FinishedColoringListener;
import com.digitalartsplayground.easycolor.interfaces.PositionListener;
import com.digitalartsplayground.easycolor.interfaces.ZoomListener;
import com.digitalartsplayground.easycolor.models.ColoringVectorDrawable;
import com.digitalartsplayground.easycolor.models.ReplayDrawable;
import com.digitalartsplayground.easycolor.models.VectorModelContainer;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.ColoringFragmentViewModel;
import com.digitalartsplayground.easycolor.utils.SharedPrefs;
import com.digitalartsplayground.easycolor.zoomageview.ZoomageView;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


public class ColoringFragment extends Fragment implements PositionListener, FinishedColoringListener, ZoomListener {

    private static final String MODEL_ID = "modelID";

    private int modelID;
    private int drawingHeight;
    private int drawingWidth;
    private float xScale;
    private float yScale;
    private boolean hintAvailable = true;
    private SharedPrefs sharedPrefs;
    private ColoringFragmentViewModel coloringViewModel;
    private ZoomageView zoomageView;
    private VectorModelContainer vectorModelContainer;
    private ColoringVectorDrawable coloringVectorDrawable;
    private RecyclerView colorsRecyclerView;
    private KonfettiView konfettiView;
    private ProgressBar hintProgressBar;
    private ImageButton zoomOutButton;
    private ObjectAnimator hintAnimator;
    private ReplayDrawable replayDrawable;
    private View mainView;
    public static int bannerClickCount = 0;
    private IronSourceBannerLayout banner;
    private FrameLayout bannerContainer;

    public static ColoringFragment newInstance(int modelID) {
        ColoringFragment coloringFragment = new ColoringFragment();
        Bundle args = new Bundle();
        args.putInt(MODEL_ID, modelID);
        coloringFragment.setArguments(args);
        return coloringFragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coloringViewModel = new ViewModelProvider(this).get(ColoringFragmentViewModel.class);
        sharedPrefs = SharedPrefs.getInstance(getActivity());

        if(getArguments() != null)
            modelID = getArguments().getInt(MODEL_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_coloring, container, false);
        bannerContainer = mainView.findViewById(R.id.banner_container);
        konfettiView  = mainView.findViewById(R.id.konfetti);
        zoomOutButton = mainView.findViewById(R.id.zoom_out_button);
        hintProgressBar = mainView.findViewById(R.id.hint_progress_bar);
        animateProgress(hintProgressBar, 100);
        observeModelFromRepository();

        return mainView;
    }


    public void onResume() {
        super.onResume();

        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            checkAdServingTimeLimit();
            loadIronSourceBanner();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        destroyBanner();
    }

    private void observeModelFromRepository() {

        LiveData<VectorModelContainer> liveData = coloringViewModel.getVectorModelContainer();

        liveData.observe(getViewLifecycleOwner(), new Observer<VectorModelContainer>() {
            @Override
            public void onChanged(VectorModelContainer vectorModelContainer) {
                ColoringFragment.this.vectorModelContainer = vectorModelContainer;
                init();
                if(vectorModelContainer.isCompleted()) {
                    revealView(mainView.findViewById(R.id.coloring_toolbar));
                }
            }
        });

        if(coloringViewModel.getVectorModelContainer().getValue() == null)
            coloringViewModel.fetchModel(modelID);
    }


    private void init() {
        //IronSource.setInterstitialListener(interstitialListener);
        //IronSource.loadInterstitial();
        initZoomageView();
        setHintButtonListener(mainView.findViewById(R.id.coloring_hint_button));
        setBackButtonListener(mainView.findViewById(R.id.coloring_back_button));
        setPlayButtonListener(mainView.findViewById(R.id.coloring_play_button));
        setResetButtonListener(mainView.findViewById(R.id.coloring_reset_button));
        initRecylerView();
        subscribeViewStateObserver();
    }


    private void initZoomageView() {
        coloringVectorDrawable = new ColoringVectorDrawable(vectorModelContainer);
        zoomageView = mainView.findViewById(R.id.zoomage_view);
        zoomageView.setImageDrawable(coloringVectorDrawable);
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
                zoomageView.setZoomListener(ColoringFragment.this);
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

                    progressBar = ColoringFragment.this.hintProgressBar;

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

                if(replayDrawable != null)
                    replayDrawable.stopReplay();

                getActivity().getSupportFragmentManager().beginTransaction().remove(ColoringFragment.this).commit();
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
        colorsRecyclerView = mainView.findViewById(R.id.coloring_recyclerView);
        colorsRecyclerView.setHasFixedSize(true);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            colorsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        } else {
            colorsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        }

        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(
                getContext(),
                vectorModelContainer,
                this,
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
        coloringViewModel.setPosition(newPosition);
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
                if(mainView.isShown()) {
                    ConstraintLayout constraintLayout = mainView.findViewById(R.id.coloring_toolbar);
                    revealView(constraintLayout);
                    zoomageView.animateScaleAndTranslationToMatrix(zoomageView.getStartMatrix(), 10);
                    startReplay();
                }
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
                ConstraintLayout constraintLayout = mainView.findViewById(R.id.coloring_toolbar);
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
        int orientation = getActivity().getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            transition = new Slide(Gravity.TOP);
        } else {
            transition = new Slide(Gravity.LEFT);
        }
        transition.setDuration(500);
        transition.addTarget(view);
        TransitionManager.beginDelayedTransition(mainView.findViewById(R.id.coloring_parent), transition);
        view.setVisibility(View.VISIBLE);
    }

    private void hideReset(View view) {
        view.setVisibility(View.GONE);
    }

    private void startReplay(){
        replayDrawable = new ReplayDrawable(vectorModelContainer);
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


    public void checkAdServingTimeLimit(){

        bannerClickCount = sharedPrefs.getBannerClickCounter();

        if(sharedPrefs.getExpireDate() < System.currentTimeMillis()) {
            sharedPrefs.resetAdPrefs();
            bannerClickCount = 0;
        }
    }

    private void loadIronSourceBanner() {

        if(banner == null || banner.isDestroyed()) {
            if(bannerClickCount < 5) {
                banner = IronSource.createBanner(getActivity(), ISBannerSize.SMART);
                bannerContainer.addView(banner);

                if(banner != null) {
                    banner.setBannerListener(bannerListener);
                    IronSource.loadBanner(banner);
                }
            }
        }
    }

    public void destroyBanner(){

        if(bannerContainer != null) {
            bannerContainer.removeAllViews();
        }

        if(banner != null && !banner.isDestroyed()) {
            banner.removeBannerListener();
            bannerListener = null;
            IronSource.destroyBanner(banner);
            banner = null;
        }

    }


    BannerListener bannerListener = new BannerListener() {

        @Override
        public void onBannerAdLoaded() { }
        @Override
        public void onBannerAdLoadFailed(IronSourceError ironSourceError) {
            // Called after a banner has attempted to load an ad but failed.
            bannerContainer.removeAllViews();
        }
        @Override
        public void onBannerAdClicked() {
            bannerClickCount++;
            sharedPrefs.setBannerClickCounter(bannerClickCount);
            sharedPrefs.setExpireDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));

            if(bannerClickCount > 4) {
                IronSource.destroyBanner(banner);
            }
        }
        @Override
        public void onBannerAdScreenPresented() {}
        @Override
        public void onBannerAdScreenDismissed() {}
        @Override
        public void onBannerAdLeftApplication() { }
    };


/*    InterstitialListener interstitialListener = new InterstitialListener() {
        @Override
        public void onInterstitialAdReady() {

            int coinViewCount = sharedPrefs.getModelViewCount();

            if(coinViewCount > 2) {
                IronSource.showInterstitial();
                sharedPrefs.setModelViewCount(0);
            }
        }

        @Override
        public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {}
        @Override
        public void onInterstitialAdOpened() {}
        @Override
        public void onInterstitialAdClosed() {}
        @Override
        public void onInterstitialAdShowSucceeded() {}
        @Override
        public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {}
        @Override
        public void onInterstitialAdClicked() { }
    };*/

}
