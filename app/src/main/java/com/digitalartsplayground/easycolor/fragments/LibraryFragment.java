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
import androidx.room.Update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.digitalartsplayground.easycolor.adapters.ModelsAdapter;
import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.interfaces.FetchModelListener;
import com.digitalartsplayground.easycolor.interfaces.StartColoringActivity;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.R;
import com.digitalartsplayground.easycolor.mvvm.viewmodels.MainActivityViewModel;
import com.digitalartsplayground.easycolor.utils.SharedPrefs;

import org.jetbrains.annotations.NotNull;


public class LibraryFragment extends Fragment implements StartColoringActivity, FetchModelListener {

    private MainActivityViewModel mainViewModel;
    private ModelsAdapter modelsAdapter;
    private RecyclerView recyclerView;

    public LibraryFragment() {
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

        if(getActivity() != null) {
            mainViewModel = new ViewModelProvider(requireActivity())
                    .get(MainActivityViewModel.class);
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
        recyclerView = view.findViewById(R.id.main_recyclerview);
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

        LiveData<FirestoreMap> liveMap = mainViewModel.getLiveFirestoreMap();

        liveMap.observe(getViewLifecycleOwner(), new Observer<FirestoreMap>() {
            @Override
            public void onChanged(FirestoreMap firestoreMap) {
                if(firestoreMap != null && firestoreMap.index != null) {
                    modelsAdapter.setModelIDList(firestoreMap.index);
                    modelsAdapter.setModelHashMap(mainViewModel.getModelHashMap());
                }
            }
        });
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
    public void fetchModel(int modelID) {
        mainViewModel.fetchModel(modelID);
    }

}