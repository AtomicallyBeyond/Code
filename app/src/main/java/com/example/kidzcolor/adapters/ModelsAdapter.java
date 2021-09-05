package com.example.kidzcolor.adapters;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidzcolor.R;
import com.example.kidzcolor.interfaces.StartColoringActivity;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;
import com.example.kidzcolor.utils.Utils;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;


public class ModelsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int COMPLETED_TYPE = 1;
    private static final int LOADING_TYPE = 2;

    private int lastVisible = 0;
    private final int orientation;
    private VectorEntity tempVectorEntity;
    private List<VectorEntity> modelsList = new ArrayList<>();
    private final StartColoringActivity startColoringActivity;


    public ModelsAdapter(StartColoringActivity startColoringActivity, int orientation) {
        this.startColoringActivity = startColoringActivity;
        this.orientation = orientation;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = null;

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
        }

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
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        if(getItemViewType(position) == COMPLETED_TYPE) {
            ((ViewHolder)holder).imageView.setImageDrawable(modelsList.get(position).getDrawable());
        }

    }

    @Override
    public int getItemViewType(int position) {
        tempVectorEntity = modelsList.get(position);

        if(tempVectorEntity.isModelLoaded())
            return COMPLETED_TYPE;
        else
            return LOADING_TYPE;
    }

    @Override
    public int getItemCount() {
        return modelsList.size();
    }

    public void setModelsList(List<VectorEntity> modelsList) {
        this.modelsList = modelsList;
        notifyDataSetChanged();
    }

    public int getLastVisible() {
        return modelsList.get(modelsList.size() - 1).getId();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.main_imageview);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startColoringActivity.startActivity(modelsList.get(getLayoutPosition()));
                }
            });
        }
    }
}
