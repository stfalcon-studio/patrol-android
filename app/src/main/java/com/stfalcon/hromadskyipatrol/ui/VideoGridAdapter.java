/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.services.UploadService;
import com.stfalcon.hromadskyipatrol.services.VideoProcessingService;
import com.stfalcon.hromadskyipatrol.utils.AppUtilities;
import com.stfalcon.hromadskyipatrol.utils.Extras;
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
    private VideosListener listener;

    public VideoGridAdapter(ArrayList<VideoItem> videos, Activity context, VideosListener listener) {
        super();
        this.context = context;
        this.listener = listener;
        mItems = videos;
    }

    public void addItem(VideoItem video) {
        mItems.add(0, video);
        notifyItemInserted(0);
    }

    public void updateState(String id, VideoItem.State state) {
        int position = -1;
        for (int i = 0; i < mItems.size(); i++) {
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

    public void updateUrl(String id, String url) {
        int position = -1;
        for (int i = 0; i < mItems.size(); i++) {
            VideoItem item = mItems.get(i);
            if (item.getId().contentEquals(id)) {
                item.setVideoURL(url);
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
                break;
            case SENDING:
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setImageResource((android.R.drawable.stat_sys_upload));
                ((AnimationDrawable) viewHolder.imgState.getDrawable()).start();
                if (!NetworkUtils.isConnectionAvailable(context)) {
                    video.setState(VideoItem.State.ERROR);
                    viewHolder.imgState.setImageResource(R.drawable.icon_repeat);
                    viewHolder.noGPS.setVisibility(View.GONE);
                    viewHolder.imgState.setVisibility(View.VISIBLE);
                }
                break;
            case UPLOADED:
                viewHolder.imgState.setImageResource(R.drawable.icon_done);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                viewHolder.imgState.setImageResource(R.drawable.icon_repeat);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;

            case BROKEN_FILE:
                viewHolder.imgState.setImageResource(R.drawable.icon_broken);
                viewHolder.noGPS.setVisibility(View.GONE);
                viewHolder.imgState.setVisibility(View.VISIBLE);
                break;

            case SAVING:
                viewHolder.noGPS.setText(R.string.saving);
                viewHolder.noGPS.setVisibility(View.VISIBLE);
                viewHolder.imgState.setVisibility(View.GONE);
                break;
        }

        loadThumb(viewHolder);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface VideosListener {
        void onVideosEmpty();
    }

    private void loadThumb(ViewHolder holder) {
        String thumb = holder.video.getThumb();
        if (thumb != null && !thumb.isEmpty()) {
            File thumbFile = new File(thumb);
            Picasso.with(context).load(thumbFile).into(holder.imgThumbnail);
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
            if (video.getState() != VideoItem.State.SAVING) {
                showDialog();
            }
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
            context.startService(new Intent(context,
                    VideoProcessingService.class).putExtra(VideoProcessingService.DELETE_MOVIE, video.getId()));

            mItems.remove(video);
            notifyItemRemoved(getAdapterPosition());

            if (mItems.size() == 0 && listener != null) {
                listener.onVideosEmpty();
            }
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
