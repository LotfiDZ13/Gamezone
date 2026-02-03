package com.dz.gamezone.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 🟢 Change version to 2 (or 3 if you already changed it)
@Database(entities = {WatchItem.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WatchDao watchDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDb(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "gamezone_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // 🟢 This prevents crashes if DB changes
                    .build();
        }
        return INSTANCE;
    }
}