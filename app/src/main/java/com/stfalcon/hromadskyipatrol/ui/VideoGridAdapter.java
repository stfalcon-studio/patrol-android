package com.stfalcon.hromadskyipatrol.ui;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.AppUtilities;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.StringUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandr on 17/08/15.
 */
public class VideoGridAdapter extends RecyclerView.Adapter<VideoGridAdapter.ViewHolder> {

    private List<VideoItem> mItems = new ArrayList<>();
    private Activity context;

    public VideoGridAdapter(ArrayList<VideoItem> videos, Activity context) {
        super();
        this.context = context;
        mItems = videos;
    }

    public void addItem(VideoItem video) {
        mItems.add(0, video);
        notifyItemInserted(0);
    }

    public void updateState(String id, VideoItem.State state) {
        int position = -1;
        for (int i = 0 ; i < mItems.size(); i++) {
            VideoItem item = mItems.get(i);
            if (item.getId().contentEquals(id)) {
                item.setState(state);
                position = i;
                break;
            }
        }
        if (position >= 0) {
            notifyItemChanged(position);
        }
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

        switch (video.getState()) {
            case READY_TO_SEND:
                viewHolder.imgState.setImageResource(R.drawable.icon_upload);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.GONE);
                break;
            case SENDING:
                viewHolder.imgState.setImageResource(R.drawable.ic_video_white_24dp);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.upload();
                if (!NetworkUtils.isConnectionAvailable(context)) {
                    video.setState(VideoItem.State.ERROR);
                    viewHolder.imgState.setImageResource(R.drawable.icon_repeat);
                    viewHolder.noGPS.setVisibility(View.GONE);
                    viewHolder.imgState.setVisibility(View.VISIBLE);
                    viewHolder.progressBar.setVisibility(View.GONE);
                }
                break;
            case UPLOADED:
                viewHolder.imgState.setImageResource(R.drawable.icon_done);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.GONE);
                break;
            case ERROR:
                viewHolder.imgState.setImageResource(R.drawable.icon_repeat);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.GONE);
                break;

            case SAVING:
                viewHolder.noGPS.setText(R.string.saving);
                viewHolder.noGPS.setVisibility(View.VISIBLE);
                viewHolder.imgState.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
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

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private VideoItem video;

        public ImageView imgThumbnail;
        public ImageView imgState;
        public TextView noGPS;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            imgState = (ImageView) itemView.findViewById(R.id.img_state);
            noGPS = (TextView) itemView.findViewById(R.id.gps);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }

        @Override
        public void onClick(View view) {
            showDialog();
        }

        private void upload() {
            if (NetworkUtils.isConnectionAvailable(context)) {
                Intent intent = new Intent(context, UploadService.class);
                intent.putExtra(IntentUtilities.VIDEO_ID, video.getId());
                context.startService(intent);
            } else {
                AppUtilities.showToast(context, R.string.error_no_connection, false);
            }
        }

        private void delete() {
            try {
                new File(video.getVideoURL()).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            DatabasePatrol.get(context).deleteVideo(video.getId());

            mItems.remove(video);
            notifyItemRemoved(getAdapterPosition());
        }

        private void showDialog() {
            final boolean isCanDelete = NetworkUtils.isCanDelete(video.getState());
            final boolean isCanLoad = NetworkUtils.isCanLoadItem(video.getState());
            ArrayList<String> options = StringUtilities.getOptions(context, isCanLoad, isCanDelete);

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
                                            if (isCanLoad) upload();
                                            else delete();
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
