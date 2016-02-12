package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.utils.Constants;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by artem on 09.02.16.
 */
public class UploadVideoActivity extends BaseSpiceActivity {

    private static final int PICKED_VIDEO = 15;
    public static final int UPLOAD_VIDEO = 30;

    private EditText date;
    private ImageView imageView;
    private TextWatcher textWatcher;
    private ImageButton btnDone;
    private Uri selectedVideoUri;
    private String videoRealPath;
    private String locationNotFormated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        initView();
        initTextWatcher();
        date.addTextChangedListener(textWatcher);

        //Use MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICKED_VIDEO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKED_VIDEO) {
            if (resultCode == RESULT_OK) {
                selectedVideoUri = data.getData();
                videoRealPath = FilesUtils.getRealPathFromURI(this, selectedVideoUri, "VIDEO");
                imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(
                        videoRealPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, selectedVideoUri);

                String dateNotFormated = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
                locationNotFormated = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
                SimpleDateFormat formatReceived = new SimpleDateFormat(Constants.VIDEO_DATE_MASK);
                SimpleDateFormat formatWanted = new SimpleDateFormat(Constants.EDIT_TEXT_MASK);
                Log.i("TAG", "onActivityResult: " + locationNotFormated);
                retriever.release();
                Date received;
                if (dateNotFormated != null) {
                    try {
                        received = formatReceived.parse(dateNotFormated);
                        String output = formatWanted.format(received);
                        date.setText(output);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
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
                IntentUtilities.openVideo(UploadVideoActivity.this, videoRealPath);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String violationDate = date.getText().toString();

                if (violationDate.contains("Y")) {
                    date.setError(getString(R.string.error_incorect_date));
                } else {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoRealPath, MediaStore.Video.Thumbnails.MINI_KIND);
                    String thumbUrl = FilesUtils.storeThumb(thumb);
                    File video = new File(videoRealPath);
                    SimpleDateFormat format = new SimpleDateFormat(Constants.EDIT_TEXT_MASK);
                    File dist = FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO);

                    try {
                        Date datePast = format.parse(violationDate);
                        FilesUtils.copyFile(video, dist);
//                        FilesUtils.removeFile(video.getAbsolutePath());
//                        Intent uploadIntent = new Intent(UploadVideoActivity.this, UploadService.class);
//                        uploadIntent.putExtra(Extras.DATE, datePast);
//                        uploadIntent.putExtra(Extras.ID, userId);
//                        uploadIntent.putExtra(Extras.URL_VIDEO, videoRealPath);
//                        startService(uploadIntent);
                        if (locationNotFormated != null) {
                            String[] geodata = locationNotFormated.substring(1).split("[+]"); //TODO Caution hard divide by +
                            double lat = Double.valueOf(geodata[0]);
                            double lon = Double.valueOf(geodata[1]);
                            addVideo(thumbUrl, dist, datePast, lat, lon);
                        } else {
                            addVideo(thumbUrl, dist, datePast, 0, 0);
                        }

                        setResult(RESULT_OK);
                        finish();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void initTextWatcher() {
        textWatcher = new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int mon = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon - 1);
                        year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal.getActualMaximum(Calendar.DATE) : day;
                        clean = String.format("%02d%02d%02d", day, mon, year);
                    }

                    clean = String.format("%s-%s-%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    date.setText(current);
                    date.setSelection(sel < current.length() ? sel : current.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
