package com.digitalartsplayground.easycolor.adapters;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.digitalartsplayground.easycolor.interfaces.DrawableAvailable;
import com.digitalartsplayground.easycolor.interfaces.StartColoringActivity;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.utils.Utils;
import com.digitalartsplayground.easycolor.R;
import com.digitalartsplayground.easycolor.interfaces.ResetModelListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyColorsAdapter extends RecyclerView.Adapter<MyColorsAdapter.MyColorsViewHolder> {

    private final StartColoringActivity startColoringActivity;
    private final ResetModelListener resetModelListener;
    private final List<VectorEntity> modelsList = new ArrayList<>();
    private final int orientation;
    private VectorEntity tempEntity;

    public MyColorsAdapter(StartColoringActivity startColoringActivity, ResetModelListener resetModelListener, int orientation) {
        this.startColoringActivity = startColoringActivity;
        this.resetModelListener = resetModelListener;
        this.orientation = orientation;
    }

    @NonNull
    @NotNull
    @Override
    public MyColorsAdapter.MyColorsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vector_model_inprogress_item, parent, false);

        int width;
        if(orientation == Configuration.ORIENTATION_PORTRAIT)
            width  = (parent.getMeasuredWidth() / 2) - Utils.dpToPx(20);
        else
            width = (parent.getMeasuredWidth() / 3) - Utils.dpToPx(20);

        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        params.height = width;
        params.width = width;
        view.setLayoutParams(params);

        view.findViewById(R.id.remove_button).setVisibility(View.VISIBLE);

        return new MyColorsAdapter.MyColorsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyColorsAdapter.MyColorsViewHolder holder, int position) {

        tempEntity = modelsList.get(position);

        if(!tempEntity.isDrawableAvailable()) {

            if(!holder.isLoading){
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.GONE);
                holder.isLoading = true;
            }

            holder.bindModel(tempEntity);
            tempEntity.loadDrawable();

        } else {

            holder.imageView.setImageDrawable(tempEntity.getDrawable());

            if(holder.isLoading){
                holder.progressBar.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.isLoading = false;
            }
        }
    }

    @Override
    public int getItemCount() {
        return modelsList.size();
    }

    public void setModelsList(Collection<VectorEntity> modelsCollection) {
        modelsList.clear();
        modelsList.addAll(modelsCollection);
        notifyDataSetChanged();
    }

    protected class MyColorsViewHolder extends RecyclerView.ViewHolder implements DrawableAvailable {

        protected VectorEntity currentEntity;
        protected final ImageView imageView;
        protected final ImageButton deleteButton;
        protected final ProgressBar progressBar;
        protected Boolean isLoading = true;

        public MyColorsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.main_imageview);
            deleteButton = itemView.findViewById(R.id.remove_button);
            progressBar = itemView.findViewById(R.id.artwork_progress_bar);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VectorEntity vectorEntity = modelsList.get(getLayoutPosition());
                    if(vectorEntity != null)
                        startColoringActivity.startActivity(vectorEntity);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetModelListener.resetModel(modelsList.get(getLayoutPosition()));
                    modelsList.remove(getLayoutPosition());
                    notifyDataSetChanged();
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

        @Override
        public void drawableAvailable() {

            imageView.setImageDrawable(currentEntity.getDrawable());

            if(isLoading){
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                isLoading = false;
            }
        }
    }
}
