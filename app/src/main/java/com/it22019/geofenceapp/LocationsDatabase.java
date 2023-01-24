package com.it22019.geofenceapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Locations.class}, version = 2, exportSchema = false)
@TypeConverters({LatLngConverter.class})
public abstract class LocationsDatabase extends RoomDatabase {
    public abstract LocationsDao locationsDao();

}
