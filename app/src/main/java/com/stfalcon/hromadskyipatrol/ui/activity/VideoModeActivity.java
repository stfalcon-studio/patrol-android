package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.io.File;

/**
 * Created by artem on 10.02.16.
 */
public class VideoModeActivity extends BaseSpiceActivity {

    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static final String VIDEO_CAPTURE = "video";
    private File dist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videomode);

        if (getIntent().hasExtra(Extras.VIDEO)) {
            dispatchTakeVideoIntent();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(dist.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            String thumbUrl = FilesUtils.storeThumb(thumb);
            addVideo(thumbUrl, dist);
            setResult(RESULT_OK);
            finish();
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        dist = FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(dist));
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void addVideo(String bitmapUrl, File videoFile) {
        DatabasePatrol db = DatabasePatrol.get(this);

        VideoItem video = new VideoItem();
        video.setId(String.valueOf(System.currentTimeMillis()));
        video.setDate(System.currentTimeMillis());
        video.setVideoURL(videoFile.getAbsolutePath());
        video.setLatitude(0);
        video.setLongitude(0);
        video.setState(VideoItem.State.READY_TO_SEND);
        video.setOwnerEmail(ProjectPreferencesManager.getUser(this).getEmail());
        video.setThumb(bitmapUrl);

        db.addVideo(video);
    }
}
