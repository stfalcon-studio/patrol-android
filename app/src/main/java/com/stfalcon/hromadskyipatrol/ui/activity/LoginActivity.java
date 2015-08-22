package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.models.LoginAnswer;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.network.tasks.LoginTask;
import com.stfalcon.hromadskyipatrol.utils.UserEmailFetcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

public class LoginActivity extends BaseSpiceActivity implements View.OnClickListener {

    private View loginButton;
    private View progressBar;
    private TextView emailEditText;
    private Realm realm;
    private LinearLayout copyrightLayout;
    private LoginUserRequestListener requestListener = new LoginUserRequestListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        initViews();
        initUserAccount();
    }

    private void initViews() {
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBar);
        emailEditText = (TextView) findViewById(R.id.emailEditText);
        copyrightLayout = (LinearLayout) findViewById(R.id.copyrightLayout);

        copyrightLayout.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                if (isEmailValid(emailEditText.getText().toString())) {
                    loginUser(emailEditText.getText().toString());
                }
                break;
            case R.id.copyrightLayout:
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getResources().getString(R.string.website_url)));
                startActivity(intent);
                break;
        }
    }


    private void initUserAccount() {
        realm = Realm.getInstance(LoginActivity.this);
        UserItem userData = realm.where(UserItem.class).findFirst();
        if (userData != null) {
            if (userData.isLogin()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }

        emailEditText.setText(UserEmailFetcher.getEmail(this));
        loginButton.setVisibility(View.VISIBLE);
    }


    private void loginUser(String email) {
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        getSpiceManager().execute(new LoginTask(email), "localAPI", DurationInMillis.ONE_MINUTE, requestListener);
    }


    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        } else {
            emailEditText.setError(getString(R.string.message_invalid_email));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    emailEditText.setError(null);
                }
            }, 1000);
        }
        return isValid;
    }


    public final class LoginUserRequestListener implements RequestListener<LoginAnswer> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            loginButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Exception", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(LoginAnswer user) {
            UserItem userData = new UserItem();
            userData.setEmail(user.email);
            userData.setId(user.id);
            userData.setIsLogin(true);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(userData);
            realm.commitTransaction();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}
