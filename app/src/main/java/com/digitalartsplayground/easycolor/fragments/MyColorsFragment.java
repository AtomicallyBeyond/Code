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
import com.digitalartsplayground.easycolor.adapters.MyColorsAdapter;
import com.digitalartsplayground.easycolor.interfaces.StartColoringActivity;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.MyColorsViewModel;
import com.digitalartsplayground.easycolor.persistance.VectorEntity;
import com.digitalartsplayground.easycolor.R;
import com.digitalartsplayground.easycolor.interfaces.ResetModelListener;

import java.util.HashMap;

public class MyColorsFragment extends Fragment implements StartColoringActivity, ResetModelListener {

    private MyColorsViewModel myColorsViewModel;
    private MyColorsAdapter myColorsAdapter;


    public MyColorsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() != null)
            myColorsViewModel = new ViewModelProvider(getActivity())
                .get(MyColorsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_my_colors, container, false);
        initRecyclerView(view);
        subscribeObserver();
        return view;
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.my_colors_recyclerview);
        recyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;

        GridLayoutManager gridLayoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getContext(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);
        myColorsAdapter = new MyColorsAdapter(this, this, orientation);
        recyclerView.setAdapter(myColorsAdapter);
    }

    private void subscribeObserver() {
        myColorsViewModel.getModelsList().observe(getViewLifecycleOwner(), new Observer<HashMap<Integer, VectorEntity>>() {
                    @Override
                    public void onChanged(HashMap<Integer, VectorEntity> entityHashMap) {
                        myColorsAdapter.setModelsList(entityHashMap.values());
                    }
                });

                myColorsViewModel.getVectorModelChanged().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        myColorsAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void startActivity(VectorEntity vectorEntity) {

        myColorsViewModel.setCurrentVectorModel(vectorEntity);
        Intent coloringIntent = new Intent(getActivity(), ColoringActivity.class);
        startActivity(coloringIntent);
    }

    @Override
    public void resetModel(VectorEntity vectorEntity) {
        myColorsViewModel.resetVectorModel(vectorEntity);
    }
}