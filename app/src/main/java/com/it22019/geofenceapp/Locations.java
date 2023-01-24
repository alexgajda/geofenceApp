package com.it22019.geofenceapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "locations_table")
public class Locations {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "locations_center")
    public LatLng center;

    @ColumnInfo(name = "locations_session")
    public int session;
}
