package com.dz.gamezone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar; // 🟢 Import
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.dz.gamezone.db.AppDatabase;
import com.dz.gamezone.db.WatchItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private LibVLC libVlc;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;
    private ProgressBar loadingSpinner;
    private View controlsRoot;

    // UI Elements
    private ImageButton btnPlayPause, btnResize, btnClose;
    private TextView btnQuality, txtTitle, txtCurrentTime, txtTotalTime; // 🟢 New Time Texts
    private SeekBar playerSeekBar; // 🟢 New SeekBar

    private List<Server> servers;
    private String pageUrl, title, logo;

    private int currentAspectRatioMode = 0;
    private Handler hideControlsHandler = new Handler(Looper.getMainLooper());
    private Handler updateTimeHandler = new Handler(Looper.getMainLooper()); // 🟢 Time Update Handler
    private boolean isControlsVisible = true;
    private boolean isSeeking = false; // 🟢 Prevent updates while dragging
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        hideSystemUi();
        prefs = getSharedPreferences("player_prefs", MODE_PRIVATE);

        // Bind Views
        videoLayout = findViewById(R.id.vlc_layout);
        loadingSpinner = findViewById(R.id.loading_spinner);
        controlsRoot = findViewById(R.id.controls_root);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnResize = findViewById(R.id.btn_resize_bottom);
        btnQuality = findViewById(R.id.btn_quality);
        btnClose = findViewById(R.id.btn_close_player);
        txtTitle = findViewById(R.id.player_title);

        // 🟢 Bind New Seek Views
        playerSeekBar = findViewById(R.id.player_seekbar);
        txtCurrentTime = findViewById(R.id.txt_current_time);
        txtTotalTime = findViewById(R.id.txt_total_time);

        String serversJson = getIntent().getStringExtra("servers_json");
        pageUrl = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        logo = getIntent().getStringExtra("logo");

        if (title != null) txtTitle.setText(title);

        if (serversJson == null) {
            finish();
            return;
        }

        try {
            servers = new Gson().fromJson(serversJson, new TypeToken<List<Server>>(){}.getType());
        } catch (Exception e) { e.printStackTrace(); }

        // VLC Init
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-drop-late-frames");
        options.add("--no-skip-frames");
        options.add("--rtsp-tcp");
        options.add("-vvv");

        libVlc = new LibVLC(this, options);
        mediaPlayer = new MediaPlayer(libVlc);
        mediaPlayer.attachViews(videoLayout, null, false, false);

        restoreOrDetectAspectRatio();
        setupListeners();

        if (servers != null && !servers.isEmpty()) {
            playServer(servers.get(0));
        }

        startAutoHideTimer();
    }

    private void restoreOrDetectAspectRatio() {
        int savedMode = prefs.getInt("aspect_ratio_mode", -1);
        if (savedMode != -1) {
            currentAspectRatioMode = savedMode;
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            float ratio = (float) metrics.widthPixels / metrics.heightPixels;
            if (ratio > 1.8) currentAspectRatioMode = 1;
            else currentAspectRatioMode = 0;
        }
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.play();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            }
            resetAutoHideTimer();
        });

        btnResize.setOnClickListener(v -> {
            cycleAspectRatio();
            resetAutoHideTimer();
        });

        btnQuality.setOnClickListener(v -> {
            showServerDialog();
            resetAutoHideTimer();
        });

        btnClose.setOnClickListener(v -> finish());

        videoLayout.setOnClickListener(v -> toggleControls());
        controlsRoot.setOnClickListener(v -> toggleControls());

        mediaPlayer.setEventListener(event -> {
            switch (event.type) {
                case MediaPlayer.Event.Buffering:
                    if (event.getBuffering() == 100.0f) loadingSpinner.setVisibility(View.GONE);
                    else loadingSpinner.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.Event.Playing:
                    loadingSpinner.setVisibility(View.GONE);
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                    applyAspectRatio(currentAspectRatioMode);
                    saveHistory();
                    startUpdateTimeTask(); // 🟢 Start Seekbar Updater
                    break;
                case MediaPlayer.Event.EncounteredError:
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(this, "Stream Error", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // 🟢 SEEK BAR LISTENER
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    txtCurrentTime.setText(stringForTime((long) progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
                resetAutoHideTimer();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                mediaPlayer.setTime(seekBar.getProgress()); // Seek to new time
                resetAutoHideTimer();
            }
        });
    }

    // 🟢 UPDATE TIME TASK (Runs every 1 second)
    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && !isSeeking) {
                long current = mediaPlayer.getTime();
                long total = mediaPlayer.getLength();

                if (total > 0) {
                    playerSeekBar.setMax((int) total);
                    playerSeekBar.setProgress((int) current);
                    txtCurrentTime.setText(stringForTime(current));
                    txtTotalTime.setText(stringForTime(total));
                } else {
                    // LIVE STREAM
                    txtCurrentTime.setText("LIVE");
                    txtTotalTime.setText("");
                    playerSeekBar.setEnabled(false);
                }
            }
            updateTimeHandler.postDelayed(this, 1000);
        }
    };

    private void startUpdateTimeTask() {
        updateTimeHandler.removeCallbacks(updateTimeRunnable);
        updateTimeHandler.post(updateTimeRunnable);
    }

    private String stringForTime(long timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) return "00:00";
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        if (hours > 0) return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        else return formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private void cycleAspectRatio() {
        currentAspectRatioMode = (currentAspectRatioMode + 1) % 4;
        prefs.edit().putInt("aspect_ratio_mode", currentAspectRatioMode).apply();
        applyAspectRatio(currentAspectRatioMode);
    }

    private void applyAspectRatio(int mode) {
        if (mediaPlayer == null) return;
        switch (mode) {
            case 0: // Fit
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(0);
                Toast.makeText(this, "Fit Screen", Toast.LENGTH_SHORT).show();
                break;
            case 1: // Fill
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                String screenRatio = metrics.widthPixels + ":" + metrics.heightPixels;
                mediaPlayer.setAspectRatio(screenRatio);
                mediaPlayer.setScale(0);
                Toast.makeText(this, "Fill Screen", Toast.LENGTH_SHORT).show();
                break;
            case 2: // 16:9
                mediaPlayer.setAspectRatio("16:9");
                mediaPlayer.setScale(0);
                Toast.makeText(this, "16:9", Toast.LENGTH_SHORT).show();
                break;
            case 3: // 4:3
                mediaPlayer.setAspectRatio("4:3");
                mediaPlayer.setScale(0);
                Toast.makeText(this, "4:3", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void playServer(Server server) {
        if ("webview".equalsIgnoreCase(server.type)) {
            startActivity(new Intent(this, WebViewActivity.class).putExtra("url", server.url));
            return;
        }
        loadingSpinner.setVisibility(View.VISIBLE);
        try {
            Media media = new Media(libVlc, Uri.parse(server.url));
            if (server.headers != null) {
                for (String key : server.headers.keySet()) {
                    if (key.equalsIgnoreCase("User-Agent")) media.addOption(":http-user-agent=" + server.headers.get(key));
                    if (key.equalsIgnoreCase("Referer")) media.addOption(":http-referrer=" + server.headers.get(key));
                }
            }
            media.setHWDecoderEnabled(true, false);
            media.addOption(":network-caching=2000");
            mediaPlayer.setMedia(media);
            media.release();
            mediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showServerDialog() {
        if (servers == null || servers.size() < 2) {
            Toast.makeText(this, "Only one source available", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] serverNames = new String[servers.size()];
        for (int i = 0; i < servers.size(); i++) serverNames[i] = servers.get(i).name;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Quality / Server");
        builder.setItems(serverNames, (dialog, which) -> playServer(servers.get(which)));
        builder.show();
    }

    private void toggleControls() {
        if (isControlsVisible) hideControls();
        else showControls();
    }

    private void showControls() {
        controlsRoot.setVisibility(View.VISIBLE);
        controlsRoot.animate().alpha(1f).setDuration(200).start();
        isControlsVisible = true;
        startAutoHideTimer();
        hideSystemUi();
    }

    private void hideControls() {
        controlsRoot.animate().alpha(0f).setDuration(200).withEndAction(() -> controlsRoot.setVisibility(View.GONE)).start();
        isControlsVisible = false;
        hideSystemUi();
    }

    private void startAutoHideTimer() {
        hideControlsHandler.removeCallbacksAndMessages(null);
        hideControlsHandler.postDelayed(this::hideControls, 4000);
    }

    private void resetAutoHideTimer() {
        if (isControlsVisible) startAutoHideTimer();
    }

    private void saveHistory() {
        if (pageUrl == null) return;
        new Thread(() -> {
            try {
                WatchItem item = new WatchItem();
                item.url = pageUrl;
                item.name = title != null ? title : "Unknown";
                item.logo = logo;
                item.position = mediaPlayer.getTime();
                item.duration = mediaPlayer.getLength();
                item.timestamp = System.currentTimeMillis();
                AppDatabase.getDb(this).watchDao().insert(item);
            } catch (Exception e) {}
        }).start();
    }

    private void hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateTimeHandler.removeCallbacks(updateTimeRunnable); // Stop seeking
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateTimeHandler.removeCallbacks(updateTimeRunnable); // Stop seeking
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            libVlc.release();
        }
    }
}