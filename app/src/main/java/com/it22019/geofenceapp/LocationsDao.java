package com.it22019.geofenceapp;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

@Dao
public interface LocationsDao {

    @Query("SELECT locations_center FROM locations_table WHERE locations_session = (SELECT MAX(locations_session) FROM locations_table)")
    List<LatLng> getLocationsInLastSession();

    @Insert
    void insertLocation(Locations... locations);

}


