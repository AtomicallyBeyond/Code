package com.digitalartsplayground.easycolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager2.widget.ViewPager2;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.digitalartsplayground.easycolor.adapters.ViewPagerAdapter;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.MainActivityViewModel;
import com.digitalartsplayground.easycolor.utils.PaintProvider;
import com.digitalartsplayground.easycolor.utils.SharedPrefs;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;

import java.util.concurrent.TimeUnit;

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
    public static MainActivity MemoryLeakContainerActivity;
    public static boolean ironSourceLoaded = false;
    private static BannerListener bannerListener;
    private static IronSourceBannerLayout tempBanner;
    private static FrameLayout ironSourceContainer;
    public static int counter = 0;

    public static void loadIronSource(FrameLayout bannerContainer) {


        tempBanner = IronSource.createBanner(MemoryLeakContainerActivity, ISBannerSize.BANNER);
        ironSourceContainer = bannerContainer;

        IronSource.init(MemoryLeakContainerActivity, "113d4317d", IronSource.AD_UNIT.BANNER);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        ironSourceContainer.addView(tempBanner, 0, layoutParams);
        tempBanner.setBannerListener(MainActivity.bannerListener);
        IronSource.loadBanner(tempBanner);
        ironSourceLoaded = true;
    }

    public static void destroyIronSourceAd() {
        ironSourceContainer.removeAllViews();
        ironSourceContainer = null;
        tempBanner.removeBannerListener();
        tempBanner.setBannerListener(null);
        IronSource.destroyBanner(tempBanner);
        tempBanner = null;
        ironSourceLoaded = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        subscribeObserver();
        init();

        SharedPrefs sharedPrefs = SharedPrefs.getInstance(this);
        counter = sharedPrefs.getCounter();

        if(sharedPrefs.getExpireDate() < System.currentTimeMillis()) {
            sharedPrefs.resetAdPrefs();
            counter = 0;
        }

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


    @Override
    protected void onDestroy() {
        MemoryLeakContainerActivity = null;
        super.onDestroy();
    }

    public MainActivity() {
        super();

        MemoryLeakContainerActivity = this;

        bannerListener = new BannerListener() {

            @Override
            public void onBannerAdLoaded() {
                // Called after a banner ad has been successfully loaded
            }
            @Override
            public void onBannerAdLoadFailed(IronSourceError error) {
                // Called after a banner has attempted to load an ad but failed.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(ironSourceContainer != null)
                            ironSourceContainer.removeAllViews();
                    }
                });
            }
            @Override
            public void onBannerAdClicked() {
                // Called after a banner has been clicked.
                counter++;
                SharedPrefs sharedPrefs = SharedPrefs.getInstance(MainActivity.this);
                sharedPrefs.setCounter(counter);
                sharedPrefs.setExpireDate(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2));

                if(counter > 4) {
                    if(MainActivity.ironSourceLoaded)
                        destroyIronSourceAd();
                }

            }
            @Override
            public void onBannerAdScreenPresented() {
                // Called when a banner is about to present a full screen content.
            }
            @Override
            public void onBannerAdScreenDismissed() {
                // Called after a full screen content has been dismissed
            }
            @Override
            public void onBannerAdLeftApplication() {
                // Called when a user would be taken out of the application context.
            }
        };
    }
}
