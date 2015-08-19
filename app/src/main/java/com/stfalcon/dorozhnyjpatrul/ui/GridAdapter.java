package com.stfalcon.dorozhnyjpatrul.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stfalcon.dorozhnyjpatrul.R;
import com.stfalcon.dorozhnyjpatrul.models.Photo;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by alexandr on 17/08/15.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private List<Photo> mItems = new ArrayList<Photo>();

    public GridAdapter(RealmResults<Photo> photos) {
        super();
        mItems.addAll(photos);
    }

    public void addItem(Photo photo){
        mItems.add(photo);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Photo photo = mItems.get(i);
        switch (photo.getState()) {
            case Photo.STATE_IN_PROCESS:
                viewHolder.imgState.setImageResource(android.R.drawable.ic_menu_upload);
                break;
            case Photo.STATE_UPLOADED:
                viewHolder.imgState.setImageResource(android.R.drawable.ic_menu_save);
                break;
            case Photo.STATE_ERROR:
                viewHolder.imgState.setImageResource(android.R.drawable.ic_delete);
                break;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(photo.getPhotoURL());
        viewHolder.imgThumbnail.setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                bitmap.getWidth()/4, bitmap.getHeight()/4, false));
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgThumbnail;
        public ImageView imgState;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            imgState = (ImageView) itemView.findViewById(R.id.img_state);
        }
    }
}
