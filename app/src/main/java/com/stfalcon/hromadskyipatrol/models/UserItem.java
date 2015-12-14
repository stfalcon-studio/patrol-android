package com.stfalcon.hromadskyipatrol.models;

/**
 * Created by alexandr on 19/08/15.
 */
public class UserItem {

    private boolean isLogin;
    private int id;
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


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }
}
