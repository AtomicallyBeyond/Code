package com.example.kidzcolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 pager2;
    private FragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.view_pager2);

        FragmentManager fragmentManager = getSupportFragmentManager();
        adapter = new FragmentAdapter(fragmentManager, getLifecycle());
        pager2.setAdapter(adapter);
        pager2.setUserInputEnabled(false);

        tabLayout.addTab(tabLayout.newTab().setText("First"));

        listenToTabLayout(tabLayout);
        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void listenToTabLayout(TabLayout tabLayout) {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

}



/*        VectorModel vectorModel = new VectorModel(this, "ic_school.xml");
        VectorMasterDrawable vectorMasterDrawable = new VectorMasterDrawable(vectorModel);

        Drawable drawable = getResources().getDrawable(R.drawable.ic_school);


        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageDrawable(vectorMasterDrawable);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent coloringIntent = new Intent(MainActivity.this, ColoringActivity.class);
                MainActivity.this.startActivity(coloringIntent);
            }
        });*/
