package com.stfalcon.hromadskyipatrol.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexandr on 17/08/15.
 */
public class VideoGridAdapter extends RecyclerView.Adapter<VideoGridAdapter.ViewHolder> {

    private List<VideoItem> mItems = new ArrayList<VideoItem>();
    private Context context;

    public VideoGridAdapter(RealmResults<VideoItem> photos, Context context) {
        super();
        this.context = context;
        mItems.addAll(photos);
        Collections.reverse(mItems);
    }

    public void addItem(VideoItem photo) {
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
        VideoItem video = mItems.get(i);
        viewHolder.video = video;

        //check location in item
        /*if (video.getState() == VideoItem.STATE_SAVING
                && video.getLatitude() == 0){
            video.setState(VideoItem.STATE_NO_GPS);
        }*/

        switch (video.getState()) {
            case VideoItem.STATE_IN_PROCESS:
                viewHolder.imgState.setImageResource(R.drawable.icon_upload);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;
            case VideoItem.STATE_UPLOADED:
                viewHolder.imgState.setImageResource(R.drawable.icon_done);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;
            case VideoItem.STATE_ERROR:
                viewHolder.imgState.setImageResource(R.drawable.icon_repeat);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;
            case VideoItem.STATE_NO_GPS:
                viewHolder.noGPS.setText(R.string.no_gps);
                viewHolder.noGPS.setVisibility(View.VISIBLE);
                viewHolder.imgState.setVisibility(View.GONE);
                break;

            case VideoItem.STATE_SAVING:
                viewHolder.noGPS.setText(R.string.saving);
                viewHolder.noGPS.setVisibility(View.VISIBLE);
                viewHolder.imgState.setVisibility(View.GONE);
                break;
        }

        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mItems.get(i).getVideoURL(),
                MediaStore.Images.Thumbnails.MINI_KIND);
        viewHolder.imgThumbnail.setImageBitmap(thumb);

//        ImageLoader.getInstance().displayImage(
//                ImageDownloader.Scheme.FILE.wrap(mItems.get(i).getVideoURL()), viewHolder.imgThumbnail);
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

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private VideoItem video;

        public ImageView imgThumbnail;
        public ImageView imgState;
        public TextView noGPS;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            imgState = (ImageView) itemView.findViewById(R.id.img_state);
            noGPS = (TextView) itemView.findViewById(R.id.gps);
        }

        @Override
        public void onClick(View view) {
            showDialog();
        }

        private void upload() {
            Intent intent = new Intent(context, UploadService.class);
            intent.putExtra(IntentUtilities.VIDEO_ID, video.getId());
            context.startService(intent);
        }

        private void delete() {
            mItems.remove(video);
            notifyItemRemoved(getAdapterPosition());
            Realm realm = Realm.getInstance(context);
            realm.beginTransaction();
            video.removeFromRealm();
            realm.commitTransaction();
        }

        private void showDialog() {
            final boolean isLoaded = video.getState() == VideoItem.STATE_UPLOADED;
            ArrayList<String> options = StringUtilities.getOptions(context, isLoaded);

            new AlertDialog.Builder(context)
                    .setItems(
                            options.toArray(new String[options.size()]),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            IntentUtilities.openVideo(context, mItems.get(getAdapterPosition()).getVideoURL());
                                            break;
                                        case 1:
                                            if (isLoaded) delete();
                                            else upload();
                                            break;
                                        case 2:
                                            delete();
                                            break;
                                    }
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }
}
