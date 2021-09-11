package com.example.kidzcolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.kidzcolor.adapters.ViewPagerAdapter;
import com.example.kidzcolor.mvvm.viewmodels.MainActivityViewModel;
import com.example.kidzcolor.utils.PaintProvider;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel mainViewModel;
    private ViewPager2 viewPager2;
    private ImageView libraryImageView;
    private ImageView artworkImageView;
    private TextView libraryTextview;
    private TextView artworkTextview;
    private int highlightedTextColor;
    private int textColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        subscribeObserver();
        init();
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

    private void init() {

        PaintProvider.createPaint();
        PaintProvider.createHDPaint();

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
}
