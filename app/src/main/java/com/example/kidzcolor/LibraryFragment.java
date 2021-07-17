package com.example.kidzcolor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kidzcolor.adapters.ModelsAdapter;
import com.example.kidzcolor.interfaces.VectorModelChosen;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.mvvm.viewmodels.LibraryViewModel;
import com.example.kidzcolor.mvvm.viewmodels.MainActivityViewModel;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;

import java.util.List;

public class LibraryFragment extends Fragment implements VectorModelChosen {

    private RecyclerView recyclerView;
    private LibraryViewModel libraryViewModel;
    private ModelsAdapter modelsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private boolean shouldFetch = true;
    private SharedPrefs sharedPrefs;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context context;
    private static final String ARG_PARAM2 = "param2";



    public LibraryFragment() {
        // Required empty public constructor
    }

    public LibraryFragment(Context context){
        this.context = context;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param2 Parameter 2.
     * @return A new instance of fragment LibraryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibraryFragment newInstance(Context context, String param2) {
        LibraryFragment fragment = new LibraryFragment(context);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = SharedPrefs.getInstance(getContext());
        libraryViewModel = new ViewModelProvider(getActivity())
                .get(LibraryViewModel.class);

        initRecyclerView();
        subscribeObservers();
        observeRecyclerView();
        libraryViewModel.fetchUpdates();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    private void initRecyclerView() {
        recyclerView = getActivity().findViewById(R.id.main_recylerview);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        modelsAdapter = new ModelsAdapter(sharedPrefs, this);
        recyclerView.setAdapter(modelsAdapter);
    }

    private void subscribeObservers() {
        libraryViewModel
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
    public void chosenVectorModel(VectorEntity vectorEntity) {
        libraryViewModel.setCurrentVectorModel(vectorEntity);
        Intent coloringIntent = new Intent(getActivity(), ColoringActivity.class);
        startActivity(coloringIntent);
    }
}