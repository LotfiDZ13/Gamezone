package com.dz.gamezone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dz.gamezone.db.AppDatabase;
import com.dz.gamezone.db.WatchItem;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);

        TextView title = findViewById(R.id.txt_category_title);
        title.setText("Continue Watching");

        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        List<WatchItem> history = AppDatabase.getDb(this).watchDao().getAllHistory();

        List<Movie> movies = new ArrayList<>();
        for (WatchItem item : history) {
            Movie m = new Movie();
            m.name = item.name;
            m.logo = item.logo;
            m.pageUrl = item.url;

            if (item.duration > 0) {
                int percent = (int) ((item.position * 100) / item.duration);
                m.badge = percent + "%";
            } else {
                m.badge = "Play";
            }

            movies.add(m);
        }

        RecyclerView rv = findViewById(R.id.recycler_view);

        // 🟢 FIX: Handle Click to Resume
        MovieAdapter adapter = new MovieAdapter(this, movies, movie -> {
            Intent intent = new Intent(this, BrowserActivity.class);

            // Re-construct the scraper link
            String apiLink = "scraper_topcinema.php?action=list&url=" + movie.pageUrl;

            // 🟢 FORCE PLAY MODE
            // We set mode="movies" so BrowserActivity knows to PLAY this immediately
            // instead of opening it as a folder.
            intent.putExtra("site_url", apiLink);
            intent.putExtra("mode", "movies");

            // 🟢 PASS METADATA (So the player has the name/logo)
            intent.putExtra("title", movie.name);
            intent.putExtra("logo", movie.logo);

            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }
}