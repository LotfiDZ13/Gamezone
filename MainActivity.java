package com.dz.gamezone;

import com.dz.gamezone.BuildConfig;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. SETUP BOTTOM NAVIGATION ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_matches) {
                selectedFragment = new MatchesFragment();
            } else if (itemId == R.id.nav_channels) {
                selectedFragment = new ChannelsFragment();
            } else if (itemId == R.id.nav_movies) {
                // 🟢 CHANGE: Load the new Professional Movies Fragment
                selectedFragment = new MoviesFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // --- 2. LOAD DEFAULT SCREEN (MATCHES) ---
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MatchesFragment())
                    .commit();
        }

        // --- 3. CHECK FOR UPDATES ---
        checkForUpdates();
    }

    // --- UPDATE CHECK LOGIC ---
    private void checkForUpdates() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://streamzone.alwaysdata.net/") // Base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        api.checkUpdate().enqueue(new Callback<AppVersion>() {
            @Override
            public void onResponse(Call<AppVersion> call, Response<AppVersion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppVersion serverVersion = response.body();

                    // Get Current App Version from Gradle
                    int currentVersion = BuildConfig.VERSION_CODE;

                    // Compare: If Server > Current, show update dialog
                    if (serverVersion.versionCode > currentVersion) {
                        showUpdateDialog(serverVersion.apkUrl);
                    }
                }
            }

            @Override
            public void onFailure(Call<AppVersion> call, Throwable t) {
                // Fail silently (don't annoy user if offline)
            }
        });
    }

    private void showUpdateDialog(String apkUrl) {
        new AlertDialog.Builder(this)
                .setTitle("New Update Available!")
                .setMessage("A new version of GameZone is ready. Please download to get the latest features.")
                .setCancelable(false) // User MUST update or click Cancel to close app (optional)
                .setPositiveButton("Update Now", (dialog, which) -> {
                    // Open Browser to Download & Install
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(apkUrl));
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .show();
    }
}