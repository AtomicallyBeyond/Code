package com.digitalartsplayground.easycolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager2.widget.ViewPager2;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.digitalartsplayground.easycolor.adapters.ViewPagerAdapter;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.MainActivityViewModel;
import com.digitalartsplayground.easycolor.utils.PaintProvider;
import com.google.firebase.analytics.FirebaseAnalytics;


public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel mainViewModel;
    private ViewPager2 viewPager2;
    private ImageView libraryImageView;
    private ImageView artworkImageView;
    private TextView libraryTextview;
    private TextView artworkTextview;
    private int highlightedTextColor;
    private int textColor;
    private FirebaseAnalytics firebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();
        mainViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        subscribeObserver();
        init();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void subscribeObserver() {
        mainViewModel.removeLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                findViewById(R.id.main_progressbar).setVisibility(View.GONE);
                libraryTextview.setOnClickListener(libraryOnClickListener);
                artworkTextview.setOnClickListener(artworkOnClickListener);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    private void init() {

        PaintProvider.createPaint();
        PaintProvider.createHDPaint();

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        libraryImageView = findViewById(R.id.library_imageview);
        libraryTextview = findViewById(R.id.library_button_textview);
        artworkImageView = findViewById(R.id.artwork_imageview);
        artworkTextview = findViewById(R.id.artwork_button_textview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textColor = getResources().getColor(R.color.text_gray, getTheme());
            highlightedTextColor = getResources().getColor(R.color.highlighted_text, getTheme());
        }else {
            textColor = getResources().getColor(R.color.text_gray);
            highlightedTextColor = getResources().getColor(R.color.highlighted_text);
        }

        viewPager2 = findViewById(R.id.view_pager_2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle()));
        viewPager2.setUserInputEnabled(false);

        if(mainViewModel.isLibraryCurrent()) {
            viewPager2.setCurrentItem(0);
            libraryTextview.setTextColor(highlightedTextColor);
        } else {
            viewPager2.setCurrentItem(1);
            artworkTextview.setTextColor(highlightedTextColor);
        }
    }

    private final View.OnClickListener libraryOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!mainViewModel.isLibraryCurrent()) {
                mainViewModel.setLibraryCurrent(true);
                viewPager2.setCurrentItem(0, true);
                artworkTextview.setTextColor(textColor);
                libraryTextview.setTextColor(highlightedTextColor);
                animateFromLeft();
            }
        }
    };

    private final View.OnClickListener artworkOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mainViewModel.isLibraryCurrent()) {
                mainViewModel.setLibraryCurrent(false);
                viewPager2.setCurrentItem(1, true);
                libraryTextview.setTextColor(textColor);
                artworkTextview.setTextColor(highlightedTextColor);
                animateFromRight();
            }
        }
    };

    private void animateFromLeft() {
        Transition transition = new Fade();
        transition.setDuration(50);
        transition.addTarget(artworkImageView);
        TransitionManager.beginDelayedTransition(findViewById(R.id.center_imageview_layout), transition);
        artworkImageView.setVisibility(View.GONE);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Transition transition = new Fade();
                transition.setDuration(100);
                transition.addTarget(libraryImageView);
                TransitionManager.beginDelayedTransition(findViewById(R.id.center_imageview_layout), transition);
                libraryImageView.setVisibility(View.VISIBLE);
            }
        }, 50);

    }

    private void animateFromRight() {
        Transition transition = new Fade();
        transition.setDuration(50);
        transition.addTarget(libraryImageView);
        TransitionManager.beginDelayedTransition(findViewById(R.id.center_imageview_layout), transition);
        libraryImageView.setVisibility(View.GONE);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Transition transition = new Fade();
                transition.setDuration(100);
                transition.addTarget(artworkImageView);
                TransitionManager.beginDelayedTransition(findViewById(R.id.center_imageview_layout), transition);
                artworkImageView.setVisibility(View.VISIBLE);
            }
        }, 50);
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
