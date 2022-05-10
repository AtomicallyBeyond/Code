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

    private final int orientation;

    private List<Integer> modelIDList = new ArrayList<>();
    private HashMap<Integer, VectorEntity> modelHashMap = new HashMap<>();

    private final StartColoringActivity startColoringActivity;
    private final FetchModelListener fetchModelListener;
    private VectorEntity tempEntity;


    public ModelsAdapter(StartColoringActivity startColoringActivity, FetchModelListener fetchModelListener, int orientation) {
        this.startColoringActivity = startColoringActivity;
        this.orientation = orientation;
        this.fetchModelListener = fetchModelListener;
    }

    @NonNull
    @NotNull
    @Override
    public ModelsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.vector_model_item, parent, false);
        view.setLayoutParams(getLayoutParams(view, parent));
        return new ViewHolder(view);

    }

    private GridLayoutManager.LayoutParams getLayoutParams(View view, ViewGroup parent) {

        int width;
        if(orientation == Configuration.ORIENTATION_PORTRAIT)
            width  = (parent.getWidth() / 2) - Utils.dpToPx(20);
        else
            width = (parent.getWidth() / 3) - Utils.dpToPx(20);

        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        params.height = width;
        params.width = width;

        return params;
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
        this.modelIDList = modelIDList;
    }

    public void setModelHashMap(HashMap<Integer, VectorEntity> modelHashMap) {
        this.modelHashMap = modelHashMap;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements DrawableAvailable {

        protected VectorEntity currentEntity;
        protected final ImageView imageView;
        protected final ProgressBar progressBar;
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
            currentEntity.setDrawableAvailable(null);
        }

        //need to handle the destruction of ViewHolder from fragment by setting
        //null DrawableAvailable in currentEntity

        @Override
        public void drawableAvailable() {

            imageView.setImageDrawable(currentEntity.getDrawable());

            if(isLoading){
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                isLoading = false;
            }
        }
    }
}
