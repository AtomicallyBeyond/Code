package com.example.kidzcolor.adapters;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidzcolor.ImageUpdater;
import com.example.kidzcolor.PositionListener;
import com.example.kidzcolor.R;
import com.example.kidzcolor.ShadedPathsDepletedListener;
import com.example.kidzcolor.models.CircleColorDrawable;
import com.example.kidzcolor.models.VectorModel;

import java.util.List;


public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> implements ShadedPathsDepletedListener {

    private Context context;
    private List<Integer> colorKeys;
    private VectorModel vectorModel;
    private ImageUpdater imageUpdater;
    private PositionListener positionListener;
    private ViewHolder viewHolder = null;
    private int selectedPosition = 0;

    public ColorPickerAdapter (Context context, VectorModel vectorModel, ImageUpdater imageUpdater, PositionListener positionListener){
        this.context = context;
        this.vectorModel = vectorModel;
        colorKeys = vectorModel.getColorKeys();
        this.imageUpdater = imageUpdater;
        this.positionListener = positionListener;

    }

    @NonNull
    @Override
    public ColorPickerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_picker_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorPickerAdapter.ViewHolder holder, int position) {
        CircleColorDrawable circleColorDrawable = ((CircleColorDrawable)holder.colorView.getDrawable());
        circleColorDrawable.setInnerCircleColor(colorKeys.get(position));
        circleColorDrawable.setProgressColor(colorKeys.get(position));
        holder.color = colorKeys.get(position);
    }

    @Override
    public int getItemCount() {
        return colorKeys.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        if(holder.getPosition() == selectedPosition) {
            CircleColorDrawable currentDrawable = ((CircleColorDrawable)holder.colorView.getDrawable());
            currentDrawable.progressInvisible(false);
            currentDrawable.backgroundProgressInvisible(false);
            holder.grow();
            viewHolder = holder;
        }

        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        if(holder.getPosition() == selectedPosition) {
            CircleColorDrawable currentDrawable = ((CircleColorDrawable)holder.colorView.getDrawable());
            currentDrawable.progressInvisible(true);
            currentDrawable.backgroundProgressInvisible(true);
            holder.shrink();
            viewHolder = null;
        }

        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void notifyShadedPathsDepleted() {
        colorKeys.remove(selectedPosition);
        notifyDataSetChanged();
        if(!colorKeys.isEmpty())
            vectorModel.shadePaths(colorKeys.get(viewHolder.getPosition()));

    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView colorView;
        private int color = Color.WHITE;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color_picker_imageView);

            CircleColorDrawable circleColorDrawable = new CircleColorDrawable(context);
            colorView.setImageDrawable(circleColorDrawable);

            setOnClickListener(itemView);

        }


        private void setOnClickListener(View itemView){

            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                    int position = ViewHolder.this.getLayoutPosition();
                    positionListener.positionChanged(position);


                    if(viewHolder != null) {
                        CircleColorDrawable previousDrawable = ((CircleColorDrawable)viewHolder.colorView.getDrawable());
                        previousDrawable.progressInvisible(true);
                        previousDrawable.backgroundProgressInvisible(true);
                        viewHolder.shrink();
                    }

                    ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(itemView, PropertyValuesHolder.ofFloat("scaleX", 1.1f), PropertyValuesHolder.ofFloat("scaleY", 1.1f));
                    objectAnimator.start();
                    objectAnimator.end();

                    CircleColorDrawable currentDrawable = ((CircleColorDrawable)colorView.getDrawable());
                    currentDrawable.setProgressColor(colorKeys.get(position));
                    currentDrawable.setBackgroundColor(Color.DKGRAY);
                    currentDrawable.progressInvisible(false);
                    currentDrawable.backgroundProgressInvisible(false);
                    grow();

                    ColorPickerAdapter.this.viewHolder = ViewHolder.this;
                    selectedPosition = position;
                    vectorModel.unShadePaths();
                    vectorModel.shadePaths(colorKeys.get(position));
                    imageUpdater.updateImage();


                }
            }); //end of setOnClickListener
        }



        private void shrink() {
            itemView.setScaleX(1.0f);
            itemView.setScaleY(1.0f);

        }


        private void grow() {
            itemView.setScaleX(1.1f);
            itemView.setScaleY(1.1f);
        }

    }
}
