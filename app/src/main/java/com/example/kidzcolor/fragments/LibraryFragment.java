package com.example.kidzcolor.fragments;


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
import com.example.kidzcolor.ColoringActivity;
import com.example.kidzcolor.mvvm.ModelResource;
import com.example.kidzcolor.R;
import com.example.kidzcolor.adapters.ModelsAdapter;
import com.example.kidzcolor.interfaces.FetchModelListener;
import com.example.kidzcolor.interfaces.StartColoringActivity;
import com.example.kidzcolor.mvvm.viewmodels.LibraryViewModel;
import com.example.kidzcolor.persistance.VectorEntity;
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
                .getAdapterUpdater().observe(getViewLifecycleOwner(), new Observer<ModelResource>() {
            @Override
            public void onChanged(ModelResource modelResource) {
                if(modelResource != null)
                    modelsAdapter.setModel(modelResource.vectorEntity, modelResource.position);
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