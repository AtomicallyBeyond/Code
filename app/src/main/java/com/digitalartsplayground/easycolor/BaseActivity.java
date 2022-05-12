package com.digitalartsplayground.easycolor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.digitalartsplayground.easycolor.utils.SharedPrefs;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import java.util.concurrent.TimeUnit;

public class BaseActivity extends AppCompatActivity {

    public static BaseActivity MemoryLeakContainerActivity;
    public static int bannerClickCount = 0;
    private static IronSourceBannerLayout banner;
    private static BannerListener bannerListener;
    private static InterstitialListener interstitialListener;


    public BaseActivity() {
        super();
        MemoryLeakContainerActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        init();



    }

    public void init() {
        IronSource.init(this, "113d4317d", IronSource.AD_UNIT.BANNER);
        IronSource.init(this, "113d4317d", IronSource.AD_UNIT.INTERSTITIAL);
        checkAdServingTimeLimit();
        loadBannerListener();
        loadInterstitialListener();
        IronSource.setInterstitialListener(interstitialListener);

        Intent startIntent = new Intent(this, MainActivity.class);
        startActivity(startIntent);
    }



    public void checkAdServingTimeLimit(){
        SharedPrefs sharedPrefs = SharedPrefs.getInstance(this);
        bannerClickCount = sharedPrefs.getBannerClickCounter();

        if(sharedPrefs.getExpireDate() < System.currentTimeMillis()) {
            sharedPrefs.resetAdPrefs();
            bannerClickCount = 0;
        }
    }

    public static void loadInterstitial() {

        if (MemoryLeakContainerActivity != null) {
            SharedPrefs sharedPrefs = SharedPrefs.getInstance(MemoryLeakContainerActivity);
            int coinViewCount = sharedPrefs.getModelViewCount();
            if (coinViewCount > 2) {
                sharedPrefs.setModelViewCount(0);
                IronSource.loadInterstitial();
            }
        }
    }

    public static void loadIronSourceBanner(FrameLayout bannerContainer) {

        if(banner != null)
            destroyBanner(bannerContainer);

        if(bannerClickCount < 5 && MemoryLeakContainerActivity != null && bannerContainer != null) {
            banner = IronSource.createBanner(MemoryLeakContainerActivity, ISBannerSize.SMART);
            bannerContainer.addView(banner);

            if (banner != null) {
                banner.setBannerListener(bannerListener);
                IronSource.loadBanner(banner);
            }
        }

    }

    public static void destroyBanner(FrameLayout bannerContainer){

        if(bannerContainer != null) {
            bannerContainer.removeAllViews();
        }

        if(banner != null && !banner.isDestroyed()) {
            banner.removeBannerListener();
            IronSource.destroyBanner(banner);
            banner = null;
        }
    }

    private void loadBannerListener() {

        bannerListener = new BannerListener() {

            @Override
            public void onBannerAdLoaded() { }
            @Override
            public void onBannerAdLoadFailed(IronSourceError ironSourceError) {
                // Called after a banner has attempted to load an ad but failed.
                if(banner != null) {
                    banner.removeBannerListener();
                    IronSource.destroyBanner(banner);
                    banner = null;
                }
            }
            @Override
            public void onBannerAdClicked() {
                bannerClickCount++;
                if(MemoryLeakContainerActivity != null) {
                    SharedPrefs sharedPrefs = SharedPrefs.getInstance(MemoryLeakContainerActivity);
                    sharedPrefs.setBannerClickCounter(bannerClickCount);
                    sharedPrefs.setExpireDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));
                }
            }
            @Override
            public void onBannerAdScreenPresented() {}
            @Override
            public void onBannerAdScreenDismissed() {}
            @Override
            public void onBannerAdLeftApplication() {}
        };
    }


    private void loadInterstitialListener() {
        interstitialListener = new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {
                IronSource.showInterstitial();
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
            public void onInterstitialAdClicked() {}
        };
    }

}