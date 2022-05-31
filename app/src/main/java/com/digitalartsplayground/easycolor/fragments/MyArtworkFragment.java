package com.digitalartsplayground.easycolor.fragments;


import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digitalartsplayground.easycolor.adapters.ModelsAdapter;
import com.digitalartsplayground.easycolor.adapters.MyColorsAdapter;
import com.digitalartsplayground.easycolor.interfaces.FetchModelListener;
import com.digitalartsplayground.easycolor.interfaces.StartColoringActivity;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.MainActivityViewModel;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.R;
import com.digitalartsplayground.easycolor.interfaces.ResetModelListener;
import com.digitalartsplayground.easycolor.utils.SharedPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;


public class MyArtworkFragment extends Fragment implements StartColoringActivity, FetchModelListener, ResetModelListener {

    private MainActivityViewModel mainViewModel;
    private MyColorsAdapter myColorsAdapter;
    private RecyclerView recyclerView;


    public MyArtworkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myColorsAdapter.destroyHolders();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() != null)
            mainViewModel = new ViewModelProvider(requireActivity())
                .get(MainActivityViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_my_artwork, container, false);
        initRecyclerView(view);
        subscribeObserver();
        return view;
    }

    private void initRecyclerView(View view) {

        GridLayoutManager gridLayoutManager;
        recyclerView = view.findViewById(R.id.my_colors_recyclerview);
        recyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getContext(), 4);

        recyclerView.setLayoutManager(gridLayoutManager);
        myColorsAdapter = new MyColorsAdapter(this, this, this);
        recyclerView.setAdapter(myColorsAdapter);
    }

    private void subscribeObserver() {

        LiveData<List<Integer>> liveIDList = mainViewModel.getLiveArtworkIDs();

        liveIDList.observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> integers) {
                if(integers != null) {
                    myColorsAdapter.setArtworkIDList(integers);
                    myColorsAdapter.setArtworkHashMap(mainViewModel.getArtworkHashMap());
                }
            }
        });

        if(mainViewModel.getLiveArtworkIDs().getValue() == null) {
            mainViewModel.loadArtworkIDs();
        }
    }

    @Override
    public void startActivity(VectorEntity vectorEntity) {

        if(vectorEntity.isModelAvailable()) {

            SharedPrefs sharedPrefs = SharedPrefs.getInstance(getContext());
            sharedPrefs.setModelViewCount(sharedPrefs.getModelViewCount() + 1);

            ColoringFragment coloringFragment = ColoringFragment.newInstance(vectorEntity.getId());
            mainViewModel.watchCurrentArtworkModel(vectorEntity.getId());

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, coloringFragment)
                    .commit();
        }
    }

    @Override
    public void resetModel(VectorEntity vectorEntity) {
        mainViewModel.resetVectorModel(vectorEntity.getId());
    }

    @Override
    public void fetchModel(int modelID) {
        mainViewModel.fetchArtwork(modelID);
    }
}