package com.digitalartsplayground.easycolor.fragments;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digitalartsplayground.easycolor.ColoringActivity;
import com.digitalartsplayground.easycolor.adapters.ModelsAdapter;
import com.digitalartsplayground.easycolor.interfaces.FetchModelListener;
import com.digitalartsplayground.easycolor.interfaces.StartColoringActivity;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.LibraryViewModel;
import com.digitalartsplayground.easycolor.persistance.VectorEntity;
import com.digitalartsplayground.easycolor.R;

import java.util.List;

public class LibraryFragment extends Fragment implements StartColoringActivity, FetchModelListener {

    private LibraryViewModel libraryViewModel;
    private ModelsAdapter modelsAdapter;


    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        return view;

    }

    private void initRecyclerView(View view) {

        GridLayoutManager gridLayoutManager;
        RecyclerView recyclerView = view.findViewById(R.id.main_recylerview);
        recyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getContext(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);
        modelsAdapter = new ModelsAdapter(this, this, orientation);
        recyclerView.setAdapter(modelsAdapter);

    }

    private void subscribeObservers() {

        libraryViewModel
                .fetchLiveModels()
                .observe(getViewLifecycleOwner(), new Observer<List<VectorEntity>>() {
                    @Override
                    public void onChanged(List<VectorEntity> vectorEntities) {
                        if(vectorEntities != null)
                            modelsAdapter.setModelsList(vectorEntities);
                    }
                });

        libraryViewModel
                .getAdapterUpdater().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                modelsAdapter.notifyItemChanged(integer);
            }
        });

        libraryViewModel
                .getVectorModelChanged()
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        modelsAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void startActivity(VectorEntity selectedVectorEntity) {

        libraryViewModel.setCurrentVectorModel(selectedVectorEntity);
        Intent coloringIntent = new Intent(getActivity(), ColoringActivity.class);
        startActivity(coloringIntent);

    }


    @Override
    public void fetchModel(VectorEntity vectorEntity, int position) {
        libraryViewModel.fetchModelWithEntity(vectorEntity, position);
    }
}