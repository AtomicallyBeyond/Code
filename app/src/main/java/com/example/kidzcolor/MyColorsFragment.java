package com.example.kidzcolor;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kidzcolor.adapters.MyColorsAdapter;
import com.example.kidzcolor.interfaces.StartColoringActivity;
import com.example.kidzcolor.mvvm.viewmodels.MyColorsViewModel;
import com.example.kidzcolor.persistance.VectorEntity;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyColorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyColorsFragment extends Fragment implements StartColoringActivity {

    private MyColorsViewModel myColorsViewModel;
    private RecyclerView recyclerView;
    private MyColorsAdapter myColorsAdapter;
    private GridLayoutManager gridLayoutManager;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyColorsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyColorsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyColorsFragment newInstance(String param1, String param2) {
        MyColorsFragment fragment = new MyColorsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

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
        recyclerView = view.findViewById(R.id.my_colors_recyclerview);

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getContext(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);
        myColorsAdapter = new MyColorsAdapter(this, orientation);
        recyclerView.setAdapter(myColorsAdapter);
    }

    private void subscribeObserver() {
        myColorsViewModel.getModelsList().observe(getViewLifecycleOwner(), new Observer<List<VectorEntity>>() {
            @Override
            public void onChanged(List<VectorEntity> vectorEntities) {
                myColorsAdapter.setModelsList(vectorEntities);
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
    public void onResume() {
        super.onResume();
        myColorsViewModel.observeUpdates();
    }
}