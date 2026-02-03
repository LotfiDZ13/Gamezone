package com.dz.gamezone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import java.net.URLDecoder;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BrowserActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ApiService api;
    private ProgressDialog loadingDialog;
    private String currentMode = "movies";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);

        rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        TextView title = findViewById(R.id.txt_category_title);
        title.setText("Movies & Series");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://streamzone.alwaysdata.net/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);

        String siteUrl = getIntent().getStringExtra("site_url");

        if (getIntent().hasExtra("mode")) {
            currentMode = getIntent().getStringExtra("mode");
        }

        if (siteUrl != null) {
            // 🟢 FIX: Check if we want to PLAY immediately (From History)
            if ("movies".equals(currentMode)) {
                // Reconstruct the movie object from Intent extras (We need to update HistoryActivity to send these!)
                Movie movie = new Movie();
                // Extract the real URL from the scraper link if needed, or just use the raw intent data
                // The intent from HistoryActivity sends "scraper...url=REAL_URL"
                // We need the REAL URL for fetchVideoSources.

                String realUrl = siteUrl.replace("scraper_topcinema.php?action=list&url=", "");
                movie.pageUrl = realUrl;

                // We can try to get name/logo if passed, otherwise use defaults
                movie.name = getIntent().hasExtra("title") ? getIntent().getStringExtra("title") : "Resume Watching";
                movie.logo = getIntent().hasExtra("logo") ? getIntent().getStringExtra("logo") : "";

                fetchVideoSources(movie);
            } else {
                // Normal browse mode
                loadMovies(siteUrl);
            }
        }
    }

    // ... rest of the file (loadMovies, handleContentClick, fetchVideoSources) remains the same ...
    // Make sure fetchVideoSources is exactly as we fixed in the previous step!

    private void loadMovies(String url) {
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        Call<List<Movie>> call;

        if (url.contains("scraper_topcinema.php")) {
            call = api.getMoviesFromUrl(url);
        }
        else if (url.contains("scraper_fasel.php")) {
            call = api.getFaselMovies();
        }
        else {
            call = api.getScrapedMovies(url);
        }

        call.enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body();
                    MovieAdapter adapter = new MovieAdapter(BrowserActivity.this, movies, movie -> handleContentClick(movie));
                    rv.setAdapter(adapter);
                } else {
                    Toast.makeText(BrowserActivity.this, "No content found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(BrowserActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleContentClick(Movie movie) {
        String url = movie.pageUrl;

        // 1. Series -> Open Season List
        if (currentMode.equals("series")) {
            if (!url.endsWith("/")) url += "/";
            String seasonsUrl = url + "list/";
            String nextApiUrl = "scraper_topcinema.php?action=seasons&url=" + seasonsUrl;

            Intent intent = new Intent(BrowserActivity.this, BrowserActivity.class);
            intent.putExtra("site_url", nextApiUrl);
            intent.putExtra("mode", "season_list");
            startActivity(intent);
        }
        // 2. Season -> Open Episode List
        else if (currentMode.equals("season_list")) {
            if (!url.endsWith("/")) url += "/";
            String episodesUrl = url + "list/";
            String nextApiUrl = "scraper_topcinema.php?action=episodes&url=" + episodesUrl;

            Intent intent = new Intent(BrowserActivity.this, BrowserActivity.class);
            intent.putExtra("site_url", nextApiUrl);
            intent.putExtra("mode", "episode_list");
            startActivity(intent);
        }
        // 3. Movies / Recent / Episodes -> PLAY
        else {
            fetchVideoSources(movie);
        }
    }

    private void fetchVideoSources(Movie movie) {
        ProgressDialog huntingDialog = new ProgressDialog(this);
        huntingDialog.setMessage("Finding video servers...");
        huntingDialog.setCancelable(false);
        huntingDialog.show();

        String pageUrl = movie.pageUrl;
        try {
            pageUrl = URLDecoder.decode(pageUrl, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Call<List<Server>> call;
        if (pageUrl.contains("topcinema.rip")) {
            call = api.getTopCinemaSources(pageUrl);
        } else if (pageUrl.contains("fasel")) {
            call = api.getFaselSources(pageUrl);
        } else {
            call = api.getScrapedSources(pageUrl);
        }

        call.enqueue(new Callback<List<Server>>() {
            @Override
            public void onResponse(Call<List<Server>> call, Response<List<Server>> response) {
                huntingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Server> servers = response.body();
                    Server bestServer = servers.get(0);

                    if ("webview".equalsIgnoreCase(bestServer.type)) {
                        Intent intent = new Intent(BrowserActivity.this, WebViewActivity.class);
                        intent.putExtra("url", bestServer.url);
                        intent.putExtra("movie_url", movie.pageUrl);
                        intent.putExtra("title", movie.name);
                        intent.putExtra("logo", movie.logo);
                        startActivity(intent);
                    } else {
                        String serversJson = new Gson().toJson(servers);
                        Intent intent = new Intent(BrowserActivity.this, PlayerActivity.class);
                        intent.putExtra("servers_json", serversJson);
                        intent.putExtra("url", movie.pageUrl);
                        intent.putExtra("title", movie.name);
                        intent.putExtra("logo", movie.logo);
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(BrowserActivity.this, "No servers found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Server>> call, Throwable t) {
                huntingDialog.dismiss();
                Toast.makeText(BrowserActivity.this, "Link Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}