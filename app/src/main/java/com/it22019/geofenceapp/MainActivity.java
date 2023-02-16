package com.it22019.geofenceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    public void onBackPressed() {
        // Do nothing
        //back button of device's disabled
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //main to activityMap
        Button toMap = findViewById(R.id.chooseLocation);
        toMap.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, mapActivity.class);
            startActivity(intent);
        });

        //main to ResultsMapActivity
        Button toResults = findViewById(R.id.showAreas);
        toResults.setOnClickListener(view -> {
            Intent intent1 = new Intent(MainActivity.this, ResultsMapActivity.class);
            startActivity(intent1);
        });

        //stop tracking button
        Button stopTracking = findViewById(R.id.stopButton);
        stopTracking.setOnClickListener(view -> {
            //checks whether service there is a service or not
            Intent intent = new Intent(MainActivity.this, LocationService.class);
            if (!stopService(intent)) {
                Toast.makeText(getApplicationContext(), "No Service Running!", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(MainActivity.this, LocationService.class));
            }
        });

        //Broadcast
        GPSReceiver receiver = new GPSReceiver();
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(receiver, filter);

    }

}