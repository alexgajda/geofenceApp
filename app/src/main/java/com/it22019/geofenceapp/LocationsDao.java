package com.it22019.geofenceapp;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

@Dao
public interface LocationsDao {

    @Query("SELECT * FROM locations_table")
    public List<Locations> getAll();

    @Query("SELECT * FROM locations_table WHERE locations_session")
    public List<Locations> getLocationBySession();

    @Query("SELECT locations_center FROM LOCATIONS_TABLE")
    public List<LatLng> getLocations();

    @Insert
    public void insertLocation(Locations ... locations);
}
