package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
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
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.services.UploadService;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by artem on 09.02.16.
 */
public class UploadVideoActivity extends BaseSpiceActivity {

    private static final int PICKED_VIDEO = 15;

    EditText date;
    ImageView imageView;
    TextWatcher textWatcher;
    ImageButton btnDone;
    Uri selectedImageUri;
    String mUri;

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
                selectedImageUri = data.getData();
                mUri = FilesUtils.getRealPathFromURI(this, selectedImageUri, "VIDEO");
                imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(
                        mUri, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));
            } else {
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
                IntentUtilities.openVideo(UploadVideoActivity.this, mUri);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TAG", "onClick: pressed");
                UserItem user = ProjectPreferencesManager.getUser(UploadVideoActivity.this);
                String userId = String.valueOf(user.getId());
                String violationDate = date.getText().toString();
                if (violationDate.contains("Y")) {
                    date.setError("Введіть коректну дату");
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                    try {
                        Date datePast = format.parse(violationDate);
                        Intent uploadIntent = new Intent(UploadVideoActivity.this, UploadService.class);
                        uploadIntent.putExtra(Extras.DATE, datePast);
                        uploadIntent.putExtra(Extras.ID, userId);
                        uploadIntent.putExtra(Extras.URL_VIDEO, mUri);
                        startService(uploadIntent);
                    } catch (ParseException e) {
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

}
