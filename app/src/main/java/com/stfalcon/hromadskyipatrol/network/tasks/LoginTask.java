package com.stfalcon.hromadskyipatrol.network.tasks;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.stfalcon.hromadskyipatrol.models.LoginAnswer;
import com.stfalcon.hromadskyipatrol.network.PatrulatrulAPI;

/**
 * Created by alexandr on 18/08/15.
 */
public class LoginTask extends RetrofitSpiceRequest<LoginAnswer, PatrulatrulAPI> {

    private String email;

    public LoginTask(String email) {
        super(LoginAnswer.class, PatrulatrulAPI.class);
        this.email = email;
    }

    @Override
    public LoginAnswer loadDataFromNetwork() {
        return getService().loginUser(email);
    }
}
