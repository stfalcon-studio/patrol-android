package com.stfalcon.hromadskyipatrol.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.utils.Constants;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by artem on 09.02.16.
 */
public class UploadVideoActivity extends BaseSpiceActivity {

    private static final int PICKED_VIDEO = 15;
    public static final int UPLOAD_VIDEO = 30;
    private static final String TAG_DATE_PICKER = "date_picker";

    private EditText date;
    private ImageView imageView;
    private ImageButton btnDone;
    private Uri contentVideoUri;
    private String contentVideoRealPath;
    private String contentLocation;
    private String contentData;
    private Date violationDate;
    private LatLng violationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        initView();

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICKED_VIDEO);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKED_VIDEO) {
            if (resultCode == RESULT_OK) {
                handleContentData(data);
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }


    private void initView() {
        date = (EditText) findViewById(R.id.et_date);
        btnDone = (ImageButton) findViewById(R.id.bt_done);
        imageView = (ImageView) findViewById(R.id.img_video_preview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtilities.openVideo(UploadVideoActivity.this, contentVideoRealPath);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (violationDate == null) {
                    date.setError(getString(R.string.error_incorrect_date));
                } else {
                    processContent();
                }
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(UploadVideoActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        violationDate = calendar.getTime();
                        SimpleDateFormat editTextDateFormat = new SimpleDateFormat(Constants.EDIT_TEXT_MASK);
                        date.setText(editTextDateFormat.format(violationDate));
                    }
                });
            }
        });
    }

    public static void showDatePicker(Activity activity,
                                      DatePickerDialog.OnDateSetListener setListener) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                setListener,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setThemeDark(true);
        datePickerDialog.show(activity.getFragmentManager(), TAG_DATE_PICKER);
    }



    private void handleContentData(Intent data) {
        contentVideoUri = data.getData();
        contentVideoRealPath = FilesUtils.getRealPathFromURI(this, contentVideoUri);
        imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(
                contentVideoRealPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));

        fetchContentMetaData();
        if (checkContentMetaData()) {
           // processContent();
        }
    }

    private void fetchContentMetaData() {
        try {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(this, contentVideoUri);

            contentData = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
            contentLocation = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
            metadataRetriever.release();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    private boolean checkContentMetaData() {
        SimpleDateFormat metaDateFormat = new SimpleDateFormat(Constants.VIDEO_META_DATE_MASK);
        parsMetaLocation();
        try {
            violationDate = metaDateFormat.parse(contentData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void parsMetaLocation() {
        if (contentLocation != null) {
            String[] geoData = contentLocation.substring(1).split("[+]"); //TODO Caution hard divide by +
            double lat = Double.valueOf(geoData[0]);
            double lon = Double.valueOf(geoData[1]);
            violationLocation = new LatLng(lat, lon);
        } else {
            violationLocation = new LatLng(0, 0);
        }
    }


    private void processContent() {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(contentVideoRealPath, MediaStore.Video.Thumbnails.MINI_KIND);
        String thumbUrl = FilesUtils.storeThumb(thumb);
        File video = new File(contentVideoRealPath);
        File dist = FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO);
        try {
            FilesUtils.copyFile(video, dist);
            addVideo(thumbUrl, dist, violationDate,
                    violationLocation.latitude,
                    violationLocation.longitude);
            setResult(RESULT_OK);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.exeption_copy_file, Toast.LENGTH_LONG).show();
        }
    }


    private void addVideo(String bitmapUrl, File videoFile, Date date, double lat, double lon) {
        DatabasePatrol db = DatabasePatrol.get(this);

        VideoItem video = new VideoItem();
        video.setId(String.valueOf(System.currentTimeMillis()));
        video.setDate(date.getTime());
        video.setVideoURL(videoFile.getAbsolutePath());
        video.setLatitude(lat);
        video.setLongitude(lon);
        video.setState(VideoItem.State.READY_TO_SEND);
        video.setOwnerEmail(ProjectPreferencesManager.getUser(this).getEmail());
        video.setThumb(bitmapUrl);

        db.addVideo(video);
    }
}
