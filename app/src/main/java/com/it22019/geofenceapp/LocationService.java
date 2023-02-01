package com.it22019.geofenceapp;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private List<LatLng> newLocations = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                newLocations.add(latLng);
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, locationListener);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service was stopped successfully
        SharedPreferences prefs = getSharedPreferences("pref", Context.MODE_PRIVATE);
        int sessions = prefs.getInt("sessions1", 0); // retrieving value from shared preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("sessions1", sessions + 1);
        editor.apply();
        newLocations.clear();
        Toast.makeText(getApplicationContext(), "Service Stopped Successfully!", Toast.LENGTH_SHORT).show();
        stopLocationUpdates();
        stopSelf();
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public List<LatLng> getNewLocations() {
        return newLocations;
    }
}