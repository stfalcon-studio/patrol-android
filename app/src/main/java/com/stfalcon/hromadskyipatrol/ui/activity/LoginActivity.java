/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.models.LoginAnswer;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.network.tasks.LoginTask;
import com.stfalcon.hromadskyipatrol.utils.AppUtilities;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.stfalcon.hromadskyipatrol.utils.UserEmailFetcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends BaseSpiceActivity implements View.OnClickListener {

    private View loginButton;
    private View progressBar;
    private EditText emailEditText;
    private CheckBox termsPrivacyCb;
    private LinearLayout copyrightLayout;
    private TextView termsPrivacyTv;
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
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        termsPrivacyCb = (CheckBox) findViewById(R.id.termsPrivacyCb);
        termsPrivacyTv = (TextView) findViewById(R.id.termsPrivacyTv);

        copyrightLayout = (LinearLayout) findViewById(R.id.copyrightLayout);

        copyrightLayout.setOnClickListener(this);

        SpannableString ss = new SpannableString(getString(R.string.terms_and_privacy));
        ss.setSpan(new UnderlineSpan(), 17, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsPrivacyTv.setText(ss);
        termsPrivacyTv.setMovementMethod(LinkMovementMethod.getInstance());
        termsPrivacyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.privacy_policy_url))
                );
                startActivity(intent);
            }
        });
        termsPrivacyCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                loginButton.setEnabled(b);
            }
        });

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
        UserItem userData = ProjectPreferencesManager.getUser(this);
        if (userData != null) {
            if (userData.isLogin()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }

        emailEditText.setText(userData == null ? UserEmailFetcher.getEmail(this) : userData.getEmail());
        loginButton.setVisibility(View.VISIBLE);
    }


    private void loginUser(String email) {
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        getSpiceManager().execute(new LoginTask(email), "localAPI", DurationInMillis.ALWAYS_EXPIRED, requestListener);
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

            UserItem user = ProjectPreferencesManager.getUser(LoginActivity.this);
            if (user != null) {
                if (user.getEmail().contentEquals(emailEditText.getText().toString())) {
                    user.setIsLogin(true);
                    ProjectPreferencesManager.setUser(LoginActivity.this, user);

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            } else {
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                String message;
                if (NetworkUtils.getConnectivityStatus(LoginActivity.this) == NetworkUtils.NOT_CONNECTED) {
                    message = getString(R.string.error_no_connection);
                } else {
                    message = getString(R.string.error_server_connecting);
                }

                AppUtilities.showToast(LoginActivity.this, message, false);
            }
        }

        @Override
        public void onRequestSuccess(LoginAnswer user) {
            UserItem userData = ProjectPreferencesManager.getUser(LoginActivity.this);
            if (userData == null) userData = new UserItem();
            userData.setEmail(user.email);
            userData.setId(user.id);
            userData.setIsLogin(true);
            ProjectPreferencesManager.setUser(LoginActivity.this, userData);

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}
