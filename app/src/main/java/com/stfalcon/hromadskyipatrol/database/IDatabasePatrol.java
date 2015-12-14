package com.stfalcon.hromadskyipatrol.database;

import com.stfalcon.hromadskyipatrol.models.VideoItem;

import java.util.ArrayList;

/**
 * Created by troy379 on 11.12.15.
 */
public interface IDatabasePatrol {

    void addVideo(VideoItem item);

    ArrayList<VideoItem> getVideos();
    ArrayList<VideoItem> getVideos(VideoItem.State state);
    VideoItem getVideo(VideoItem.State state);
    VideoItem getVideo(String id);

    void updateVideo(String id, VideoItem.State state);
    void updateVideo(String id, String url);

    void deleteVideo(String id);
}
