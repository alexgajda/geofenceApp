package com.it22019.geofenceapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.maps.android.SphericalUtil;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class mapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = mapActivity.class.getSimpleName();
    private GoogleMap map;
    private static int sessions;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    @Override
    public void onBackPressed() {
        // Do nothing
        //back button of device's disabled
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }



        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        // [START maps_current_place_map_fragment]
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }


    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;


        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


        //list with circle objects
        List<Circle> circles = new ArrayList<>();

        map.setOnMapLongClickListener(latLng -> {
            boolean circleRemoved = false;
            // Iterate through the list of circles
            for (Circle circle : circles) {
                // Calculate the distance between the long press location and the center of the circle
                float distance = (float) SphericalUtil.computeDistanceBetween(latLng, circle.getCenter());
                // If the distance is less than the radius of the circle, remove the circle from the map
                if (distance < circle.getRadius()) {
                    circle.remove();
                    circles.remove(circle);
                    circleRemoved = true;
                    break;
                }
            }

            // Only add a new circle if none of the existing circles were removed
            if (!circleRemoved) {
                circles.add(map.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(100)
                        .strokeColor(Color.RED)
                        .visible(true)));
            }
        });

        //cancel button
        Button cancel = findViewById(R.id.cancelButton);
        cancel.setOnClickListener(view -> {
            //iterator to remove circles
            Iterator<Circle> iterator = circles.iterator();
            //while there is another circle remove it
            while (iterator.hasNext()) {
                Circle circle = iterator.next();
                //removing
                circle.remove();
                iterator.remove();
            }

            //gets back to main
            Intent intent = new Intent(mapActivity.this, MainActivity.class);
            startActivity(intent);
        });

        //database builder
        LocationsDatabase db = Room.databaseBuilder(getApplicationContext(),LocationsDatabase.class,"locations_table").build();
        LocationsDao locationsDao = db.locationsDao();

        //start button that stores the centers and starts service
        Button start = findViewById(R.id.startButton);
        start.setOnClickListener(view -> {

            //stores current session
            SharedPreferences prefs = getSharedPreferences("pref", Context.MODE_PRIVATE);
            int sessions = prefs.getInt("sessions1", 0); // retrieving value from shared preferences

            //if there is at least 1 circle that has been added by user
            if (circles.size() >= 1){
                for (Circle circle : circles) {
                    //stores centers
                    Locations locations1 = new Locations();
                    locations1.session = sessions;
                    locations1.center = circle.getCenter();
                    new Thread(() -> locationsDao.insertLocation(locations1)).start();
                }

                //starts service with intent
                Intent intent1 = new Intent(mapActivity.this, LocationService.class);
                startService(intent1);

                //goes back to main
                Intent intent = new Intent(mapActivity.this, MainActivity.class);
                Toast.makeText(getApplicationContext(), "Locations Saved Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
            else{
                //make a toast if user pressed button without choosing any location
                Toast.makeText(getApplicationContext(), "Please select at least 1 location!", Toast.LENGTH_SHORT).show();
            }
        });



    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

