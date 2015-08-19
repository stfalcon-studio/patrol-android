package com.stfalcon.dorozhnyjpatrul.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexandr on 19/08/15.
 */
public class UserData extends RealmObject {

    private boolean isLogin;
    @PrimaryKey
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }


    public boolean isLogin() {
        return isLogin;
    }

    public String getEmail() {
        return email;
    }
}
