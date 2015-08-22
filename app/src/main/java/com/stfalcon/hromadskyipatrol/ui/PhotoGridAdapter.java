package com.stfalcon.hromadskyipatrol.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.models.PhotoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexandr on 17/08/15.
 */
public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.ViewHolder> {

    private List<PhotoItem> mItems = new ArrayList<PhotoItem>();
    private Context context;

    public PhotoGridAdapter(RealmResults<PhotoItem> photos, Context context) {
        super();
        this.context = context;
        mItems.addAll(photos);
        Collections.reverse(mItems);
    }

    public void addItem(PhotoItem photo) {
        mItems.add(0, photo);
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
        PhotoItem photo = mItems.get(i);

        switch (photo.getState()) {
            case PhotoItem.STATE_IN_PROCESS:
                viewHolder.imgState.setImageResource(R.drawable.icon_upload);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;
            case PhotoItem.STATE_UPLOADED:
                viewHolder.imgState.setImageResource(R.drawable.icon_done);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;
            case PhotoItem.STATE_ERROR:
                viewHolder.imgState.setImageResource(R.drawable.icon_repeat);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;
            case PhotoItem.STATE_NO_GPS:
                viewHolder.noGPS.setVisibility(View.VISIBLE);
                viewHolder.imgState.setVisibility(View.GONE);
                break;
        }
        ImageLoader.getInstance().displayImage(
                ImageDownloader.Scheme.FILE.wrap(mItems.get(i).getPhotoURL()), viewHolder.imgThumbnail);
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    public void updateItem(String photoID, int state) {
        Realm realm = Realm.getInstance(context);
        for (int i = 0; i<mItems.size(); i++){
            if (mItems.get(i).getId().equals(photoID)){
                realm.beginTransaction();
                mItems.get(i).setState(state);
                realm.commitTransaction();
                notifyItemChanged(i);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgThumbnail;
        public ImageView imgState;
        public TextView noGPS;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            imgState = (ImageView) itemView.findViewById(R.id.img_state);
            noGPS = (TextView) itemView.findViewById(R.id.gps);
        }
    }
}
