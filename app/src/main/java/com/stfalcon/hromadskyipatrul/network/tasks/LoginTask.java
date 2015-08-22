package com.stfalcon.hromadskyipatrul.network.tasks;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.stfalcon.hromadskyipatrul.models.LoginAnswer;
import com.stfalcon.hromadskyipatrul.network.HPatrulAPI;

/**
 * Created by alexandr on 18/08/15.
 */
public class LoginTask extends RetrofitSpiceRequest<LoginAnswer, HPatrulAPI> {

    private String email;

    public LoginTask(String email) {
        super(LoginAnswer.class, HPatrulAPI.class);
        this.email = email;
    }

    @Override
    public LoginAnswer loadDataFromNetwork() {
        return getService().loginUser(email);
    }
}
