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
        for(int childCount = recyclerView.getChildCount(), i = 0; i < childCount; i++) {
            final ModelsAdapter.ViewHolder holder =
                    (ModelsAdapter.ViewHolder)(recyclerView.getChildViewHolder(recyclerView.getChildAt(i)));
            holder.disableListener();
        }
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
        recyclerView = view.findViewById(R.id.my_colors_recyclerview);
        recyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;

        GridLayoutManager gridLayoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getContext(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);
        myColorsAdapter = new MyColorsAdapter(this, this, this, orientation);
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

/*        mainViewModel.getLiveArtworkList().observe(getViewLifecycleOwner(), new Observer<List<VectorEntity>>() {
            @Override
            public void onChanged(List<VectorEntity> vectorEntities) {
                if(vectorEntities != null) {
                    myColorsAdapter.setModelsList(vectorEntities);
                }
            }
        });

        if(mainViewModel.getLiveArtworkList().getValue() == null) {
            mainViewModel.loadLiveArtWorkList();
        }*/
    }

    @Override
    public void startActivity(VectorEntity vectorEntity) {

        if(vectorEntity.isModelAvailable()) {

            SharedPrefs sharedPrefs = SharedPrefs.getInstance(getContext());
            sharedPrefs.setModelViewCount(sharedPrefs.getModelViewCount() + 1);

            ColoringFragment coloringFragment = ColoringFragment.newInstance(vectorEntity.getId());

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentDetached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
                    super.onFragmentDetached(fm, f);
                    if(f instanceof ColoringFragment) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mainViewModel.fetchModel(vectorEntity.getId());
                            }
                        }, 100);

                        fragmentManager.unregisterFragmentLifecycleCallbacks(this);
                    }
                }
            }, false);

            fragmentManager
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