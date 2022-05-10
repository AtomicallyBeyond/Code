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
import com.digitalartsplayground.easycolor.interfaces.FetchModelListener;
import com.digitalartsplayground.easycolor.interfaces.StartColoringActivity;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.utils.Utils;
import com.digitalartsplayground.easycolor.R;
import com.digitalartsplayground.easycolor.interfaces.ResetModelListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MyColorsAdapter extends RecyclerView.Adapter<MyColorsAdapter.MyColorsViewHolder> {
    private final int orientation;
    private VectorEntity tempEntity;
    private final StartColoringActivity startColoringActivity;
    private final FetchModelListener fetchModelListener;
    private final ResetModelListener resetModelListener;
    private List<Integer> artworkIDList = new ArrayList<>();
    private HashMap<Integer, VectorEntity> artworkHashMap = new HashMap<>();


    public MyColorsAdapter(StartColoringActivity startColoringActivity, FetchModelListener fetchModelListener, ResetModelListener resetModelListener, int orientation) {
        this.startColoringActivity = startColoringActivity;
        this.fetchModelListener = fetchModelListener;
        this.resetModelListener = resetModelListener;
        this.orientation = orientation;
    }

    @NonNull
    @NotNull
    @Override
    public MyColorsAdapter.MyColorsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vector_model_inprogress_item, parent, false);
        view.setLayoutParams(getLayoutParams(view, parent));
        view.findViewById(R.id.remove_button).setVisibility(View.VISIBLE);
        return new MyColorsAdapter.MyColorsViewHolder(view);

    }

    private GridLayoutManager.LayoutParams getLayoutParams(View view, ViewGroup parent) {

        int width;
        if(orientation == Configuration.ORIENTATION_PORTRAIT)
            width  = (parent.getMeasuredWidth() / 2) - Utils.dpToPx(20);
        else
            width = (parent.getMeasuredWidth() / 3) - Utils.dpToPx(20);

        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        params.height = width;
        params.width = width;
        return params;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyColorsAdapter.MyColorsViewHolder holder, int position) {

        int artworkID = artworkIDList.get(position);
        tempEntity = artworkHashMap.get(artworkID);

        if(tempEntity == null || tempEntity.getModel() == null) {

            if(!holder.isLoading){
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);
                holder.isLoading = true;
            }

            VectorEntity emptyEntity = new VectorEntity();
            emptyEntity.setId(artworkID);
            artworkHashMap.put(artworkID, emptyEntity);
            holder.bindModel(emptyEntity);
            fetchModelListener.fetchModel(artworkID);

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
        return artworkIDList.size();
    }

    public void setArtworkIDList(List<Integer> artworkIDList) {
        this.artworkIDList = artworkIDList;
    }

    public void setArtworkHashMap(HashMap<Integer, VectorEntity> artworkHashMap) {
        this.artworkHashMap = artworkHashMap;
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
                    int selectedModelID = artworkIDList.get(getLayoutPosition());
                    VectorEntity vectorEntity = artworkHashMap.get(selectedModelID);
                    if(vectorEntity != null)
                        startColoringActivity.startActivity(vectorEntity);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VectorEntity vectorEntity =
                            artworkHashMap.get(artworkIDList.get(getLayoutPosition()));
                    if(vectorEntity != null) {
                        resetModelListener.resetModel(vectorEntity);
                    }
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
