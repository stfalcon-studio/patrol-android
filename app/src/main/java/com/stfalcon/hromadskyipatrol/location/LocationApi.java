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
