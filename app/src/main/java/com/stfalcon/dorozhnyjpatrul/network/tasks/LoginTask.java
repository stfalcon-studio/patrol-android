package com.stfalcon.dorozhnyjpatrul.network.tasks;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.stfalcon.dorozhnyjpatrul.models.LoginAnswer;
import com.stfalcon.dorozhnyjpatrul.network.HPatrulAPI;

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
