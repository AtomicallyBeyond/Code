package com.example.kidzcolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kidzcolor.adapters.FragmentAdapter;
import com.example.kidzcolor.utils.Utils;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 pager2;
    private FragmentAdapter adapter;

    private LibraryFragment libraryFragment;
    private MyColorsFragment galleryFragment;
    private LinearLayout libraryButton;
    private LinearLayout galleryButton;
    private ImageView libraryImage;
    private ImageView galleryImage;
    private TextView libraryTextview;
    private TextView galleryTextview;
    private boolean isLibraryCurrent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        libraryButton = findViewById(R.id.library_button_custom);
        libraryImage = findViewById(R.id.library_button_imageview);
        libraryTextview = findViewById(R.id.library_button_textview);
        libraryButton.setOnClickListener(libraryOnClickListener);
        galleryButton = findViewById(R.id.gallery_button_custom);
        galleryImage = findViewById(R.id.gallery_button_imageview);
        galleryTextview = findViewById(R.id.gallery_button_textview);
        galleryButton.setOnClickListener(galleryOnClickListener);

        libraryFragment = new LibraryFragment();
        galleryFragment = new MyColorsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, libraryFragment)
                .commit();

        galleryImage.setScaleX(0.7f);
        galleryImage.setScaleY(0.7f);
        libraryTextview.setTextColor(Color.WHITE);


/*        tabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.view_pager2);

        FragmentManager fragmentManager = getSupportFragmentManager();
        adapter = new FragmentAdapter(fragmentManager, getLifecycle());
        pager2.setAdapter(adapter);
        //pager2.setUserInputEnabled(false);

        tabLayout.addTab(tabLayout.newTab().setText("Library") );
        tabLayout.addTab(tabLayout.newTab().setText("My Colors"));

        listenToTabLayout(tabLayout);*/
    }

    private View.OnClickListener libraryOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!isLibraryCurrent) {
                isLibraryCurrent = true;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, libraryFragment)
                        .commit();

                libraryTextview.setTextColor(Color.WHITE);
                galleryTextview.setTextColor(Color.BLACK);
                animateGrowButton(libraryImage);
                animateShrinkButton(galleryImage);

            }
        }
    };

    private View.OnClickListener galleryOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isLibraryCurrent) {
                isLibraryCurrent = false;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, galleryFragment)
                        .commit();

                galleryTextview.setTextColor(Color.WHITE);
                libraryTextview.setTextColor(Color.BLACK);
                animateGrowButton(galleryImage);
                animateShrinkButton(libraryImage);
            }
        }
    };

    private void animateGrowButton(View view) {
        view.animate().scaleX(1.0f).scaleY(1.0f);
    }

    private void animateShrinkButton(View view) {
        view.animate().scaleX(0.8f).scaleY(0.8f);
    }

/*    ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat("scaleX", 1.1f),
            PropertyValuesHolder.ofFloat("scaleY", 1.1f)
    );

        objectAnimator.start();
        objectAnimator.end();*/

/*    private void listenToTabLayout(TabLayout tabLayout) {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                pager2.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

    }*/

}
