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

package com.stfalcon.hromadskyipatrol.location;

import android.content.Context;
import android.location.Location;

/**
 * Created by alex on 02.10.15.
 */
public interface LocationApi {

    public static int LOW_ACCURACY_LOCATION = 0;
    public static int HIGH_ACCURACY_LOCATION = 1;

    public void init(Context context);
    public boolean isWorked();
    public void createLocationRequest(int type);
    public void startLocationUpdates();
    public void stopLocationUpdates();
    public Location getPreviousLocation();

    public void setLocationListener(MyLocationListener listener);



}
