package com.dz.gamezone.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "watch_history")
public class WatchItem {
    @PrimaryKey
    @NonNull
    public String url; // Page URL is the unique ID

    public String name;
    public String logo;
    public long position; // Where they stopped (ms)
    public long duration; // Total length (ms)
    public long timestamp; // When they watched it
}