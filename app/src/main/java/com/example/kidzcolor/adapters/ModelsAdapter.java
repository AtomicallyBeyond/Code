package com.example.kidzcolor.adapters;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidzcolor.R;
import com.example.kidzcolor.interfaces.FetchModelListener;
import com.example.kidzcolor.interfaces.StartColoringActivity;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.Utils;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;


public class ModelsAdapter extends RecyclerView.Adapter<ModelsAdapter.ViewHolder> {

    private final int orientation;
    private List<VectorEntity> modelsList = new ArrayList<>();
    private final StartColoringActivity startColoringActivity;
    private final FetchModelListener fetchModelListener;


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

/*        View view = null;

        if(viewType == COMPLETED_TYPE) {
            view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.vector_model_item, parent, false);
            view.setLayoutParams(getLayoutParams(view, parent));
            return new ViewHolder(view);
        }

        else {

            view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.vector_model_loading, parent, false);
            view.setLayoutParams(getLayoutParams(view, parent));
            return new LoadingViewHolder(view);
        }*/

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

        if(modelsList.get(position).isModelLoaded()) {

            if(holder.isLoading){
                holder.progressBar.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.isLoading = false;
            }

            holder.imageView.setImageDrawable(modelsList.get(position).getDrawable());

        } else {

            if(!holder.isLoading){
                holder.imageView.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.isLoading = true;
            }

            fetchModelListener.fetchModel(modelsList.get(position), position);
        }

    }

    @Override
    public int getItemCount() {
        return modelsList.size();
    }

    public void setModelsList(List<VectorEntity> modelsList) {
        this.modelsList = modelsList;
        notifyDataSetChanged();
    }

    public void setModel(VectorEntity vectorEntity, int position){
        modelsList.set(position, vectorEntity);
        notifyItemChanged(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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
                    startColoringActivity.startActivity(modelsList.get(getLayoutPosition()));
                }
            });
        }
    }
}
