package com.digitalartsplayground.easycolor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
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
    public static boolean ironSourceLoaded = false;
    private static BannerListener bannerListener;
    private static InterstitialListener interstitialListener;
    private static IronSourceBannerLayout tempBanner;
    private static FrameLayout ironSourceContainer;
    public static boolean interstitialReady = false;
    public static int counter = 0;


    public BaseActivity() {
        super();
        MemoryLeakContainerActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPrefs sharedPrefs = SharedPrefs.getInstance(this);
        counter = sharedPrefs.getCounter();

        if(sharedPrefs.getExpireDate() < System.currentTimeMillis()) {
            sharedPrefs.resetAdPrefs();
            counter = 0;
        }

        IronSource.init(MemoryLeakContainerActivity, "113d4317d", IronSource.AD_UNIT.BANNER);
        IronSource.init(MemoryLeakContainerActivity, "113d4317d", IronSource.AD_UNIT.INTERSTITIAL);
        loadBannerListener();
        loadInterstitialListener();
        IronSource.setInterstitialListener(interstitialListener);

        Intent startIntent = new Intent(this, MainActivity.class);
        startActivity(startIntent);
    }

    public static void loadIronSourceInterstitial() {
        IronSource.loadInterstitial();
    }

    public static void loadIronSourceBanner(FrameLayout bannerContainer) {

        tempBanner = IronSource.createBanner(MemoryLeakContainerActivity, ISBannerSize.BANNER);
        ironSourceContainer = bannerContainer;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        if(tempBanner != null) {
            ironSourceContainer.addView(tempBanner, 0, layoutParams);
            tempBanner.setBannerListener(bannerListener);
            IronSource.loadBanner(tempBanner);
            ironSourceLoaded = true;
        }

    }

    public static void destroyIronSourceBanner() {
        ironSourceContainer.removeAllViews();
        ironSourceContainer = null;
        tempBanner.removeBannerListener();
        tempBanner.setBannerListener(null);
        IronSource.destroyBanner(tempBanner);
        tempBanner = null;
        ironSourceLoaded = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MemoryLeakContainerActivity = null;
    }


    private void loadBannerListener() {
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
                SharedPrefs sharedPrefs = SharedPrefs.getInstance(BaseActivity.this);
                sharedPrefs.setCounter(counter);
                sharedPrefs.setExpireDate(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2));

                if(counter > 4) {
                    if(BaseActivity.ironSourceLoaded)
                        destroyIronSourceBanner();
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


    private void loadInterstitialListener() {

        interstitialListener = new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {
                interstitialReady = true;
            }

            @Override
            public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {

            }

            @Override
            public void onInterstitialAdOpened() {

            }

            @Override
            public void onInterstitialAdClosed() {

            }

            @Override
            public void onInterstitialAdShowSucceeded() {
                interstitialReady = false;
                loadIronSourceInterstitial();
            }

            @Override
            public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {

            }

            @Override
            public void onInterstitialAdClicked() {

            }
        };
    }

}
