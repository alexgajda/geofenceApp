package com.it22019.geofenceapp;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.android.gms.maps.model.LatLng;

import java.net.PortUnreachableException;
import java.util.List;

@Dao
public interface LocationsDao {

    @Query("SELECT locations_center FROM locations_table WHERE locations_session = (SELECT MAX(locations_session) FROM locations_table)")
    public List<LatLng> getLocationBySession();

    @Insert
    public void insertLocation(Locations ... locations);

}


