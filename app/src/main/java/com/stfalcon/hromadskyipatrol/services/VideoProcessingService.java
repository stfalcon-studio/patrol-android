package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Intent;

import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.stfalcon.hromadskyipatrol.models.VideoItem;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Anton Bevza on 12/1/15.
 */
public class VideoProcessingService extends IntentService {

    public VideoProcessingService() {
        super(VideoProcessingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getInstance(this);

        RealmResults<VideoItem> videoList;

        videoList = realm.where((VideoItem.class))
                .equalTo("state", VideoItem.STATE_SAVING)
                .findAll();

        for (VideoItem item : videoList) {
            cutVideo(item);
        }
    }

    private void cutVideo(VideoItem item) {
        //CroppedTrack croppedTrack = new CroppedTrack()
    }
}
