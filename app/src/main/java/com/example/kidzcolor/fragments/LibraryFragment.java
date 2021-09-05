package com.example.kidzcolor.fragments;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kidzcolor.ColoringActivity;
import com.example.kidzcolor.R;
import com.example.kidzcolor.adapters.ModelsAdapter;
import com.example.kidzcolor.interfaces.StartColoringActivity;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.mvvm.viewmodels.LibraryViewModel;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;

import java.util.List;

public class LibraryFragment extends Fragment implements StartColoringActivity {

    private RecyclerView recyclerView;
    private LibraryViewModel libraryViewModel;
    private ModelsAdapter modelsAdapter;
    private GridLayoutManager gridLayoutManager;
    private SharedPrefs sharedPrefs;

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = SharedPrefs.getInstance(getContext());

        if(getActivity() != null) {
            libraryViewModel = new ViewModelProvider(getActivity())
                    .get(LibraryViewModel.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        initRecyclerView(view);
        subscribeObservers();
        observeRecyclerView();
        return view;
    }

    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.main_recylerview);
        recyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getContext(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);
        modelsAdapter = new ModelsAdapter(this, orientation);
        recyclerView.setAdapter(modelsAdapter);
    }

    private void subscribeObservers() {
        libraryViewModel
                .getModelsList().observe(getViewLifecycleOwner(), new Observer<Resource<List<VectorEntity>>>() {
            @Override
            public void onChanged(Resource<List<VectorEntity>> listResource) {
                if(listResource.data != null) {
                    switch (listResource.status){
                        case SUCCESS:
                            modelsAdapter.setModelsList(listResource.data);
                            break;
                        case ERROR:
                            modelsAdapter.setModelsList(listResource.data);
                            Toast.makeText(getActivity(), listResource.message, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });

        libraryViewModel.getVectorModelChanged().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                modelsAdapter.notifyDataSetChanged();
            }
        });

    }

    private void observeRecyclerView() {

        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisible = modelsAdapter.getLastVisible();

                if(lastVisible > 1) {
                    lastVisible--;
                    libraryViewModel.fetchModel(lastVisible);
                }
            }
        };

            recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void startActivity(VectorEntity selectedVectorEntity) {

        libraryViewModel.setCurrentVectorModel(selectedVectorEntity);
        Intent coloringIntent = new Intent(getActivity(), ColoringActivity.class);
        startActivity(coloringIntent);

    }


}