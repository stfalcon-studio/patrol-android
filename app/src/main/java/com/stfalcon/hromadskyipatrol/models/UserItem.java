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

package com.stfalcon.hromadskyipatrol.models;

/**
 * Created by alexandr on 19/08/15.
 */
public class UserItem {
    public static String NO_AUTHORIZED_EMAIL = "no_authorized";
    public static int NO_AUTHORIZED_ID = -1;

    private int id;
    private String email;

    public UserItem() {
        this.id = NO_AUTHORIZED_ID;
        this.email = NO_AUTHORIZED_EMAIL;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isLogin() {
        return id != NO_AUTHORIZED_ID;
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
