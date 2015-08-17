package com.stfalcon.dorozhnyjpatrul.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.stfalcon.dorozhnyjpatrul.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private View btLogin;
    private View progressBar;
    private TextView etLogin;

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
                loginUser();
                break;
        }
    }


    private void initUserAccount() {
        etLogin.setText("example@gmail.com");
        btLogin.setVisibility(View.VISIBLE);
    }


    private void loginUser() {
        btLogin.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoginActivity.this, MainScreen.class));
                finish();
            }
        }, 2000);
    }
}
