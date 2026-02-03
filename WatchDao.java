package com.dz.gamezone.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface WatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WatchItem item);

    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    List<WatchItem> getAllHistory();

    @Query("SELECT * FROM watch_history WHERE url = :url LIMIT 1")
    WatchItem getItem(String url);

    @Query("DELETE FROM watch_history")
    void clearAll();
}