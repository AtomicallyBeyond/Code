package com.digitalartsplayground.easycolor.adapters;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalartsplayground.easycolor.interfaces.DrawableAvailable;
import com.digitalartsplayground.easycolor.interfaces.FetchModelListener;
import com.digitalartsplayground.easycolor.interfaces.StartColoringActivity;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.utils.Utils;
import com.digitalartsplayground.easycolor.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ModelsAdapter extends RecyclerView.Adapter<ModelsAdapter.ViewHolder> {

    private List<Integer> modelIDList = new ArrayList<>();
    private HashMap<Integer, VectorEntity> modelHashMap = new HashMap<>();
    private List<ViewHolder> viewHolders = new ArrayList<>();

    private StartColoringActivity startColoringActivity;
    private FetchModelListener fetchModelListener;
    private VectorEntity tempEntity;


    public ModelsAdapter(StartColoringActivity startColoringActivity, FetchModelListener fetchModelListener) {
        this.startColoringActivity = startColoringActivity;
        this.fetchModelListener = fetchModelListener;
    }

    @NonNull
    @NotNull
    @Override
    public ModelsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.vector_model_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        viewHolders.add(holder);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ModelsAdapter.ViewHolder holder, int position) {

        int modelID = modelIDList.get(position);
        tempEntity = modelHashMap.get(modelID);

        if(tempEntity == null || tempEntity.getModel() == null) {

            if(!holder.isLoading){
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);
                holder.isLoading = true;
            }

            VectorEntity emptyEntity = new VectorEntity();
            emptyEntity.setId(modelID);
            modelHashMap.put(modelID, emptyEntity);
            holder.bindModel(emptyEntity);
            fetchModelListener.fetchModel(modelID);

        } else if (tempEntity.isDrawableAvailable()) {

            holder.bindModel(tempEntity);
            holder.imageView.setImageDrawable(tempEntity.getDrawable());

            if(holder.isLoading){
                holder.progressBar.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.isLoading = false;
            }
        }

    }

    @Override
    public int getItemCount() {
        return modelIDList.size();
    }


    public void setModelIDList(List<Integer> modelIDList) {
        this.modelIDList.clear();
        this.modelIDList.addAll(modelIDList);
    }

    public void setModelHashMap(HashMap<Integer, VectorEntity> modelHashMap) {
        this.modelHashMap = modelHashMap;
        notifyDataSetChanged();
    }

    public void destroyAdapter() {

        for(ViewHolder holder : viewHolders) {
            if(holder != null) {
                holder.disableListener();
                holder.destroyHolder();
                holder = null;
            }
        }
        viewHolders.clear();
        viewHolders = null;

        VectorEntity entity;

        for(int id : modelIDList) {

            entity = modelHashMap.get(id);
            if(entity != null)
                entity.setDrawableAvailable(null);
        }

        modelIDList.clear();
        modelIDList = null;
        modelHashMap = null;
        startColoringActivity = null;
        fetchModelListener = null;
        tempEntity = null;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements DrawableAvailable {

        protected VectorEntity currentEntity;
        protected ImageView imageView;
        protected ProgressBar progressBar;
        protected Boolean isLoading = true;


        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.main_imageview);
            progressBar = itemView.findViewById(R.id.progress_bar);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedModelID = modelIDList.get(getLayoutPosition());
                    VectorEntity vectorEntity = modelHashMap.get(selectedModelID);
                    if(vectorEntity != null)
                        startColoringActivity.startActivity(vectorEntity);
                }
            });
        }

        public void bindModel(VectorEntity vectorEntity) {

            if(currentEntity != null)
                currentEntity.setDrawableAvailable(null);

            currentEntity = vectorEntity;
            currentEntity.setDrawableAvailable(this);
        }

        public void disableListener() {

            if(currentEntity != null)
                currentEntity.setDrawableAvailable(null);
        }

        @Override
        public void drawableAvailable() {

            imageView.setImageDrawable(currentEntity.getDrawable());

            if(isLoading){
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                isLoading = false;
            }
        }

        public void destroyHolder() {
            currentEntity = null;
            imageView = null;
            progressBar = null;
        }
    }
}
