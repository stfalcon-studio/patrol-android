package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.location.LocationActivity;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.io.File;

/**
 * Created by artem on 10.02.16.
 */
public class VideoModeActivity extends LocationActivity {

    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static final String VIDEO_CAPTURE = "video";
    private static final long WAIT_LOCATION_TIME = 15000;
    private File dist;
    private Location currentLocation;
    private boolean waitLocation = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videomode);

        currentLocation = getLastUserLocation();
        if (getIntent().hasExtra(Extras.VIDEO)) {
            dispatchTakeVideoIntent();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        currentLocation = location;
        if (waitLocation) saveVideo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (currentLocation != null) {
                    saveVideo();
                } else {
                    waitLoc();
                }
            } else {
                FilesUtils.removeFile(dist.getAbsolutePath());
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private void waitLoc() {
        waitLocation = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentLocation == null) {
                    currentLocation = new Location("gps");
                    currentLocation.setLatitude(0);
                    currentLocation.setLongitude(0);
                    saveVideo();
                }
            }
        }, WAIT_LOCATION_TIME);
    }

    private void saveVideo() {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(dist.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
        String thumbUrl = FilesUtils.storeThumb(thumb);
        addVideo(thumbUrl, dist);
        setResult(RESULT_OK);
        finish();
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, );  // TODO: 19.02.16 max video size
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
        video.setLatitude(currentLocation.getLatitude());
        video.setLongitude(currentLocation.getLongitude());
        video.setState(VideoItem.State.READY_TO_SEND);
        video.setOwnerEmail(ProjectPreferencesManager.getUser(this).getEmail());
        video.setThumb(bitmapUrl);
        video.setSourceType(VideoItem.SOURCE_TYPE_CAMERA);

        db.addVideo(video);
    }
}
