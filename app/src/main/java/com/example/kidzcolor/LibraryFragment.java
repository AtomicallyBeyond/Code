package com.example.kidzcolor;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kidzcolor.adapters.ModelsAdapter;
import com.example.kidzcolor.interfaces.StartColoringActivity;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.mvvm.viewmodels.LibraryViewModel;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;

import java.util.List;

public class LibraryFragment extends Fragment implements StartColoringActivity {

    private RecyclerView recyclerView;
    private LibraryViewModel libraryViewModel;
    private ModelsAdapter modelsAdapter;
    private GridLayoutManager gridLayoutManager;
    private boolean shouldFetch = true;
    private SharedPrefs sharedPrefs;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public LibraryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment LibraryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibraryFragment newInstance(String param1, String param2) {
        LibraryFragment fragment = new LibraryFragment();
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

        sharedPrefs = SharedPrefs.getInstance(getContext());
        libraryViewModel = new ViewModelProvider(getActivity())
                .get(LibraryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        initRecyclerView(view);
        subscribeObservers();
        observeRecyclerView();
        libraryViewModel.fetchUpdates();

        return view;
    }

    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.main_recylerview);
        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        modelsAdapter = new ModelsAdapter(sharedPrefs, this);
        recyclerView.setAdapter(modelsAdapter);
    }

    private void subscribeObservers() {
        libraryViewModel
                .getModelsList().observe(getViewLifecycleOwner(), new Observer<Resource<List<VectorEntity>>>() {
            @Override
            public void onChanged(Resource<List<VectorEntity>> listResource) {
                shouldFetch = true;

                if(listResource.data != null) {
                    switch (listResource.status){
                        case SUCCESS:
                            modelsAdapter.setModelsList(listResource.data);
                            break;
                        case ERROR:
                            modelsAdapter.setModelsList(listResource.data);
                            Toast.makeText(getActivity(), listResource.message, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        });

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
                        libraryViewModel.fetchMore();
                    }
                }
            }
        };

        if(!sharedPrefs.getEndReached())
            recyclerView.addOnScrollListener(onScrollListener);
    }


    @Override
    public void startActivity(VectorEntity vectorEntity) {

        libraryViewModel.setCurrentVectorModel(vectorEntity);
        Intent coloringIntent = new Intent(getActivity(), ColoringActivity.class);
        startActivity(coloringIntent);

        libraryViewModel.getCurrentVectorModel().observe(getViewLifecycleOwner(), new Observer<VectorModelContainer>() {
            @Override
            public void onChanged(VectorModelContainer vectorModelContainer) {
                modelsAdapter.notifyDataSetChanged();
            }
        });

    }


}