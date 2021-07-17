package com.example.kidzcolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.PathParser;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kidzcolor.adapters.ModelsAdapter;
import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.interfaces.VectorModelChosen;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.mvvm.viewmodels.MainActivityViewModel;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;

import java.util.List;

public class MainActivity extends AppCompatActivity implements VectorModelChosen {

    private RecyclerView recyclerView;
    private MainActivityViewModel mainActivityViewModel;
    private ModelsAdapter modelsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private boolean shouldFetch = true;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs = SharedPrefs.getInstance(MainActivity.this);
        mainActivityViewModel = new ViewModelProvider(this)
                .get(MainActivityViewModel.class);

        initRecyclerView();
        subscribeObservers();
        observeRecyclerView();
        mainActivityViewModel.fetchUpdates();

    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.main_recylerview);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        modelsAdapter = new ModelsAdapter(sharedPrefs, this);
        recyclerView.setAdapter(modelsAdapter);
    }

    private void observeRecyclerView() {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!sharedPrefs.getEndReached()) {
                    int firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();
                    int visibleItemCount = gridLayoutManager.getChildCount();
                    int totalItemCount = gridLayoutManager.getItemCount();

                    if((shouldFetch && (firstVisibleItem + visibleItemCount) > (totalItemCount - 4))) {
                        shouldFetch = false;
                        mainActivityViewModel.fetchMore();
                    }
                }
            }
        };

        if(!sharedPrefs.getEndReached())
            recyclerView.addOnScrollListener(onScrollListener);
    }

    private void subscribeObservers() {
        mainActivityViewModel
                .getModelsList().observe(this, new Observer<Resource<List<VectorEntity>>>() {
            @Override
            public void onChanged(Resource<List<VectorEntity>> listResource) {
                shouldFetch = true;

                if(listResource.data != null) {
                    switch (listResource.status){
                        case SUCCESS:
                            modelsAdapter.setNumList(listResource.data);
                            break;
                        case ERROR:
                            modelsAdapter.setNumList(listResource.data);
                            Toast.makeText(MainActivity.this, listResource.message, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        });

    }

    @Override
    public void chosenVectorModel(VectorEntity vectorEntity) {
        mainActivityViewModel.setCurrentVectorModel(vectorEntity);
        Intent coloringIntent = new Intent(this, ColoringActivity.class);
        startActivity(coloringIntent);
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
