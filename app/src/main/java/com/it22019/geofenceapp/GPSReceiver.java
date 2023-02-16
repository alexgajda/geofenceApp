package com.it22019.geofenceapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class GPSReceiver extends BroadcastReceiver {
    private boolean isRegistered = true;
    private LocationService service;


    @Override
    public void onReceive(Context context, Intent intent) {
        //gets service gps if its on or off
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is enabled
            if (service != null) {
                //resume service
                service.resumeService();
            }
        } else {
            if (isRegistered) {
                // GPS is disabled
                // Stop the LocationService
                if (service != null) {
                    service.pauseService();
                }
                // Unregister the receiver to avoid multiple calls
                context.unregisterReceiver(this);
                isRegistered = false;
            }
        }

    }


}