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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.utils.Constants;
import com.stfalcon.hromadskyipatrol.utils.ContentUtil;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by artem on 09.02.16.
 */
public class UploadVideoActivity extends BaseSpiceActivity implements ContentUtil.PickContentListener{

    public static final int UPLOAD_VIDEO = 30;
    private static final String TAG_DATE_PICKER = "date_picker";

    private EditText date;
    private ImageView imageView;
    private ImageButton btnDone;
    private String contentLocation;
    private String contentData;
    private Date violationDate;
    private ContentUtil contentUtil;
    private LatLng violationLocation;
    private File targetFile;
    private String thumbUrl;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        initView();
        contentUtil = new ContentUtil(this, this);
        contentUtil.pickContent(ContentUtil.Content.VIDEO,
                FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO));
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
        contentUtil.onActivityResult(requestCode, resultCode, data);
    }


    private void initView() {
        date = (EditText) findViewById(R.id.et_date);
        imageView = (ImageView) findViewById(R.id.img_video_preview);
        btnDone = (ImageButton) findViewById(R.id.bt_done);
        progressBar = (ProgressBar) findViewById(R.id.progress);

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

    private void handleAbsentFileType() {
        Toast.makeText(this, R.string.error_pick_video, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleWrongFileType() {
        Toast.makeText(this, R.string.error_type_video, Toast.LENGTH_SHORT).show();
        finish();
    }


    private void contentNotReady() {
        findViewById(R.id.copy_message).setVisibility(View.GONE);
        findViewById(R.id.rl_container ).setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtilities.openVideo(UploadVideoActivity.this, targetFile.getAbsolutePath());
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (violationDate == null) {
                    date.setError(getString(R.string.error_incorrect_date));
                } else {
                    finishAddVideoToDB();
                }
            }
        });
        btnDone.setVisibility(View.VISIBLE);
        findViewById(R.id.textInputLayout).setVisibility(View.VISIBLE);
    }


    private void checkContentType(String contentType) {
        if (contentType.isEmpty()) {
            handleAbsentFileType();
        }
        if (!contentType.contains("video")) {
            handleWrongFileType();
        }
    }

    private boolean checkIsHaveMetaData(Uri fileUri) {
        try {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(this, fileUri);

            contentData = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
            contentLocation = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
            metadataRetriever.release();

            parsMetaLocation();

            SimpleDateFormat metaDateFormat = new SimpleDateFormat(Constants.VIDEO_META_DATE_MASK);
            violationDate = metaDateFormat.parse(contentData);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            contentNotReady();
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

    private void addVideo(File file, String thumbUrl) {
        DatabasePatrol db = DatabasePatrol.get(this);

        VideoItem video = new VideoItem();
        video.setId(String.valueOf(System.currentTimeMillis()));
        video.setDate(violationDate.getTime());
        video.setVideoURL(file.getAbsolutePath());
        video.setLatitude(violationLocation.latitude);
        video.setLongitude(violationLocation.longitude);
        video.setState(VideoItem.State.READY_TO_SEND);
        video.setOwnerEmail(ProjectPreferencesManager.getUser(this).getEmail());
        video.setThumb(thumbUrl);
        video.setSourceType(VideoItem.SOURCE_TYPE_UPLOAD);

        db.addVideo(video);
    }

    private void finishAddVideoToDB() {
        addVideo(targetFile, thumbUrl);
        setResult(RESULT_OK);
        finish();
    }




    @Override
    public void onContentLoaded(File file, String contentType) {
        targetFile = file;
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(targetFile.getAbsolutePath(),
                MediaStore.Video.Thumbnails.MINI_KIND);
        thumbUrl = FilesUtils.storeThumb(thumb);
        checkContentType(contentType);
        if (checkIsHaveMetaData(Uri.fromFile(file))){
            finishAddVideoToDB();
        }
    }

    @Override
    public void onLoadContentProgress(int loadPercent) {
        progressBar.setProgress(loadPercent);
    }

    @Override
    public void onError(String error) {
        handleAbsentFileType();
    }

    @Override
    public void onCanceled() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
