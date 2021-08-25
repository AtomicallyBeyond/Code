package com.example.kidzcolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kidzcolor.adapters.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private LinearLayout libraryButton;
    private LinearLayout galleryButton;
    private ImageView libraryImage;
    private ImageView galleryImage;
    private TextView libraryTextview;
    private TextView galleryTextview;
    private boolean isLibraryCurrent = true;
    private int textColor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager2 = findViewById(R.id.view_pager_2);
        libraryButton = findViewById(R.id.library_button_custom);
        libraryImage = findViewById(R.id.library_button_imageview);
        libraryTextview = findViewById(R.id.library_button_textview);
        libraryButton.setOnClickListener(libraryOnClickListener);
        galleryButton = findViewById(R.id.gallery_button_custom);
        galleryImage = findViewById(R.id.gallery_button_imageview);
        galleryTextview = findViewById(R.id.gallery_button_textview);
        galleryButton.setOnClickListener(galleryOnClickListener);

        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle()));
        viewPager2.setCurrentItem(0);
        viewPager2.setUserInputEnabled(false);

        libraryImage.setScaleX(1.2f);
        libraryImage.setScaleY(1.2f);
        textColor = getResources().getColor(R.color.purple_700);
        libraryTextview.setTextColor(textColor);

    }

    private View.OnClickListener libraryOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isLibraryCurrent) {
                isLibraryCurrent = true;
                viewPager2.setCurrentItem(0, true);
                libraryTextview.setTextColor(textColor);
                galleryTextview.setTextColor(Color.GRAY);
                animateGrowButton(libraryImage);
                animateShrinkButton(galleryImage);
            }
        }
    };

    private View.OnClickListener galleryOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isLibraryCurrent) {
                isLibraryCurrent = false;

                viewPager2.setCurrentItem(1, true);
                galleryTextview.setTextColor(textColor);
                libraryTextview.setTextColor(Color.GRAY);
                animateGrowButton(galleryImage);
                animateShrinkButton(libraryImage);
            }
        }
    };

    private void animateGrowButton(View view) {
        view.animate().scaleX(1.2f).scaleY(1.2f);
    }

    private void animateShrinkButton(View view) {
        view.animate().scaleX(1.0f).scaleY(1.0f);
    }

}
