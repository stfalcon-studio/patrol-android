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
import java.io.IOException;

/**
 * Created by artem on 10.02.16.
 */
public class VideoModeActivity extends BaseSpiceActivity {

    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static final String VIDEO_CAPTURE = "video";

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
            Uri videoUri = data.getData();
            String realPath = FilesUtils.getRealPathFromURI(this, videoUri, "VIDEO");
            File src = new File(realPath);
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(realPath, MediaStore.Video.Thumbnails.MINI_KIND);
            try {
                String thumbUrl = FilesUtils.storeThumb(thumb);
                File dist = FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO);
                FilesUtils.copyFile(src, dist);
                FilesUtils.removeFile(src.getAbsolutePath());
                addVideo(thumbUrl, dist);
                setResult(RESULT_OK);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        setResult(RESULT_CANCELED);
        finish();
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
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
