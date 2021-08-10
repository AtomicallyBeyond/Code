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
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MyColorsAdapter extends RecyclerView.Adapter<MyColorsAdapter.MyColorsViewHolder> {

    private StartColoringActivity startColoringActivity;
    private List<VectorEntity> modelsList = new ArrayList<>();
    private int orientation;

    public MyColorsAdapter(StartColoringActivity startColoringActivity, int orientation) {
        this.startColoringActivity = startColoringActivity;
        this.orientation = orientation;
    }

    @NonNull
    @NotNull
    @Override
    public MyColorsAdapter.MyColorsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vector_model_item, parent, false);

        int width;
        if(orientation == Configuration.ORIENTATION_PORTRAIT)
            width  = (parent.getMeasuredWidth() / 2) - Utils.dpToPx(20);
        else
            width = (parent.getMeasuredWidth() / 3) - Utils.dpToPx(20);

        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        params.height = width;
        params.width = width;
        view.setLayoutParams(params);

        return new MyColorsAdapter.MyColorsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyColorsAdapter.MyColorsViewHolder holder, int position) {
        holder.imageView.setImageDrawable(
                new VectorMasterDrawable(new VectorModel(modelsList.get(position).getModel()))
        );
    }

    @Override
    public int getItemCount() {
        return modelsList.size();
    }

    public void setModelsList(List<VectorEntity> modelsList) {
        this.modelsList = modelsList;
        notifyDataSetChanged();
    }

    protected class MyColorsViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public MyColorsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.main_imageview);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startColoringActivity.startActivity(
                            modelsList.get(getLayoutPosition()));
                }
            });
        }
    }
}
