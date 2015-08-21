package com.stfalcon.dorozhnyjpatrul.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.stfalcon.dorozhnyjpatrul.R;
import com.stfalcon.dorozhnyjpatrul.models.LoginAnswer;
import com.stfalcon.dorozhnyjpatrul.models.UserData;
import com.stfalcon.dorozhnyjpatrul.network.tasks.LoginTask;
import com.stfalcon.dorozhnyjpatrul.utils.UserEmailFetcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

public class LoginActivity extends BaseSpiceActivity implements View.OnClickListener {

    private View btLogin;
    private View progressBar;
    private TextView etLogin;
    private Realm realm;
    private LoginUserRequestListener requestListener = new LoginUserRequestListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        initViews();
        initUserAccount();
    }

    private void initViews() {
        btLogin = findViewById(R.id.bt_login);
        btLogin.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBar);
        etLogin = (TextView) findViewById(R.id.et_email);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                if (isEmailValid(etLogin.getText().toString())) {
                    loginUser(etLogin.getText().toString());
                }
                break;
        }
    }


    private void initUserAccount() {
        realm = Realm.getInstance(LoginActivity.this);
        UserData userData = realm.where(UserData.class).findFirst();
        if (userData != null) {
            if (userData.isLogin()) {
                startActivity(new Intent(LoginActivity.this, MainScreen.class));
                finish();
            }
        }

        etLogin.setText(UserEmailFetcher.getEmail(this));
        btLogin.setVisibility(View.VISIBLE);
    }


    private void loginUser(String email) {
        btLogin.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        /*UserData userData = new UserData();
        userData.setEmail(etLogin.getText().toString());
        userData.setIsLogin(true);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(userData);
        realm.commitTransaction();
        startActivity(new Intent(LoginActivity.this, MainScreen.class));
        finish();*/

        getSpiceManager().execute(new LoginTask(email),"localAPI", DurationInMillis.ONE_MINUTE, requestListener);
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
            etLogin.setError(getString(R.string.message_invalid_email));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    etLogin.setError(null);
                }
            }, 1000);
        }
        return isValid;
    }


    public final class LoginUserRequestListener implements RequestListener<LoginAnswer> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            btLogin.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Exception", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(LoginAnswer user) {
            UserData userData = new UserData();
            userData.setEmail(user.email);
            userData.setId(user.id);
            userData.setIsLogin(true);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(userData);
            realm.commitTransaction();
            startActivity(new Intent(LoginActivity.this, MainScreen.class));
            finish();
        }
    }
}
