package com.digitalartsplayground.easycolor.adapters;

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
import com.digitalartsplayground.easycolor.interfaces.ColorDepletedListener;
import com.digitalartsplayground.easycolor.interfaces.FinishedColoringListener;
import com.digitalartsplayground.easycolor.interfaces.PositionListener;
import com.digitalartsplayground.easycolor.models.CircleColorDrawable;
import com.digitalartsplayground.easycolor.models.VectorModelContainer;
import com.digitalartsplayground.easycolor.R;
import java.util.ArrayList;
import java.util.List;


public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> implements ColorDepletedListener {

    //Is there a way to avoid using context here?
    private  Context context;
    private  List<Integer> colorKeys;
    private  VectorModelContainer vectorModelContainer;
    private PositionListener positionListener;
    private FinishedColoringListener finishedColoringListener;
    private ViewHolder viewHolder = null;
    private int selectedPosition = 0;


    public ColorPickerAdapter (Context context, VectorModelContainer vectorModelContainer, PositionListener positionListener, FinishedColoringListener finishedColoringListener){
        this.context = context;
        this.vectorModelContainer = vectorModelContainer;
        colorKeys = vectorModelContainer.getColorKeys();
        this.positionListener = positionListener;
        this.finishedColoringListener = finishedColoringListener;

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
    }

    @Override
    public int getItemCount() {
        return colorKeys.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        if(holder.getLayoutPosition() == selectedPosition) {
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
        if(holder.getLayoutPosition() == selectedPosition) {
            CircleColorDrawable currentDrawable = ((CircleColorDrawable)holder.colorView.getDrawable());
            currentDrawable.progressInvisible(true);
            currentDrawable.backgroundProgressInvisible(true);
            holder.shrink();
            viewHolder = null;
        }

        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void notifyColorDepleted() {

        if(!colorKeys.isEmpty()) {
            colorKeys.remove(selectedPosition);

            if(viewHolder != null)
                onViewDetachedFromWindow(viewHolder);


            if(selectedPosition > 0 || colorKeys.isEmpty())
                selectedPosition--;

            notifyDataSetChanged();

            if(selectedPosition >= 0 && !colorKeys.isEmpty()) {
                vectorModelContainer.shadePaths(colorKeys.get(selectedPosition));
                positionListener.positionChanged(selectedPosition);
            }

            if(colorKeys.isEmpty())
                finishedColoringListener.finished();

        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView colorView;

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
                    vectorModelContainer.unShadePaths();
                    vectorModelContainer.shadePaths(colorKeys.get(position));
                    selectedPosition = position;

                    positionListener.positionChanged(position);
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
