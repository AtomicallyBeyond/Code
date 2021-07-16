package com.example.kidzcolor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidzcolor.R;
import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.interfaces.VectorModelChosen;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ModelsAdapter extends RecyclerView.Adapter<ModelsAdapter.ViewHolder> {

    private List<VectorEntity> modelsList = new ArrayList<>();
    private SharedPrefs sharedPrefs;
    private VectorModelChosen vectorModelChosen;

    public ModelsAdapter(SharedPrefs sharedPrefs, VectorModelChosen vectorModelChosen) {
        this.sharedPrefs = sharedPrefs;
        this.vectorModelChosen = vectorModelChosen;
    }

    @NonNull
    @NotNull
    @Override
    public ModelsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vector_model_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ModelsAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageDrawable(
                new VectorMasterDrawable(new VectorModel(modelsList.get(position).model))
        );
    }

    @Override
    public int getItemCount() {
        return modelsList.size();
    }

    public void setNumList(List<VectorEntity> modelsList) {
        this.modelsList = modelsList;

        if(modelsList != null && modelsList.size() != 0){
            int lastValue = modelsList.get(modelsList.size() - 1).id;

            if(lastValue == 0)
                sharedPrefs.setEndReached(true);
            sharedPrefs
                    .setLastVisible(lastValue);
            sharedPrefs
                    .setLastModified(modelsList.get(0).id);
            notifyDataSetChanged();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.main_recylerview);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vectorModelChosen.chosenVectorModel(
                            modelsList.get(getLayoutPosition()));
                }
            });
        }
    }
}
