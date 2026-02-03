package com.dz.gamezone;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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

public class MoviesFragment extends Fragment {

    private RecyclerView rv;
    private ProgressBar progressBar;
    private ApiService api;
    private Button btnRecent, btnMovies, btnSeries;

    private final int COLOR_ACTIVE = Color.parseColor("#E50914");
    private final int COLOR_INACTIVE = Color.parseColor("#333333");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        rv = view.findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        progressBar = view.findViewById(R.id.progressBar);

        btnRecent = view.findViewById(R.id.chip_recent);
        btnMovies = view.findViewById(R.id.chip_movies);
        btnSeries = view.findViewById(R.id.chip_series);
        ImageButton btnHistory = view.findViewById(R.id.btn_history);
        ImageButton btnSearch = view.findViewById(R.id.btn_search);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://streamzone.alwaysdata.net/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);

        btnRecent.setOnClickListener(v -> loadCategory("recent"));
        btnMovies.setOnClickListener(v -> loadCategory("movies"));
        btnSeries.setOnClickListener(v -> loadCategory("series"));

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HistoryActivity.class));
        });

        btnSearch.setOnClickListener(v -> showSearchDialog());

        loadCategory("recent");

        return view;
    }

    // 🟢 FIXED SEARCH DIALOG
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Search Movies & Series");

        // Container
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Input
        final EditText input = new EditText(getContext());
        input.setHint("Type name (e.g. Batman)...");
        layout.addView(input);

        // Radio Group
        final RadioGroup typeGroup = new RadioGroup(getContext());
        typeGroup.setOrientation(RadioGroup.HORIZONTAL);
        typeGroup.setPadding(0, 30, 0, 0);

        // 🟢 FIX: Added IDs so only one stays checked!
        RadioButton rbAll = new RadioButton(getContext());
        rbAll.setText("All");
        rbAll.setId(1);
        rbAll.setChecked(true);

        RadioButton rbMovies = new RadioButton(getContext());
        rbMovies.setText("Movies");
        rbMovies.setId(2);

        RadioButton rbSeries = new RadioButton(getContext());
        rbSeries.setText("Series");
        rbSeries.setId(3);

        typeGroup.addView(rbAll);
        typeGroup.addView(rbMovies);
        typeGroup.addView(rbSeries);
        layout.addView(typeGroup);

        builder.setView(layout);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                String selectedType = "all";
                // Check selection using the object directly
                if (rbMovies.isChecked()) selectedType = "movies";
                if (rbSeries.isChecked()) selectedType = "series";

                performSearch(query, selectedType);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void performSearch(String query, String type) {
        updateButtonStyles("search");
        // Pass query AND type to the server
        String apiLink = "scraper_topcinema.php?action=search&q=" + query + "&type=" + type;
        fetchMovies(apiLink, "search");
    }

    private void loadCategory(String category) {
        updateButtonStyles(category);

        String url = "";
        switch (category) {
            case "recent":
                url = "https://topcinema.rip/recent/";
                break;
            case "movies":
                url = "https://topcinema.rip/movies/";
                break;
            case "series":
                url = "https://topcinema.rip/category/مسلسلات-اجنبي/";
                break;
        }

        String apiLink = "scraper_topcinema.php?action=list&url=" + url;
        fetchMovies(apiLink, category);
    }

    private void fetchMovies(String fullApiUrl, String currentCategory) {
        progressBar.setVisibility(View.VISIBLE);
        rv.setAdapter(null);

        api.getMoviesFromUrl(fullApiUrl).enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body();
                    if (movies.isEmpty()) {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                    MovieAdapter adapter = new MovieAdapter(getContext(), movies, movie -> handleMovieClick(movie, currentCategory));
                    rv.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No content found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                if (getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleMovieClick(Movie movie, String categoryMode) {
        Intent intent = new Intent(getContext(), BrowserActivity.class);
        String url = movie.pageUrl;

        String nextMode = "movies";

        // Logic to determine if it's a Series or Movie
        if (categoryMode.equals("series")) {
            nextMode = "series";
        }
        else if (url.contains("/series/") || url.contains("مسلسل")) {
            nextMode = "series";
        }
        // Redirect Episode clicks to Series list
        else if (url.contains("/episode/") || movie.name.toLowerCase().contains("episode") || movie.name.contains("الحلقة")) {
            nextMode = "series";
        }

        if (nextMode.equals("movies")) {
            fetchAndPlay(movie);
        } else {
            if (!url.endsWith("/")) url += "/";
            String seasonsUrl = url + "list/";
            String seasonsApi = "scraper_topcinema.php?action=seasons&url=" + seasonsUrl;

            intent.putExtra("site_url", seasonsApi);
            intent.putExtra("mode", "season_list");
            startActivity(intent);
        }
    }

    private void fetchAndPlay(Movie movie) {
        Toast.makeText(getContext(), "Loading servers...", Toast.LENGTH_SHORT).show();

        String pageUrl = movie.pageUrl;
        try { pageUrl = URLDecoder.decode(pageUrl, "UTF-8"); } catch (Exception e) {}

        api.getTopCinemaSources(pageUrl).enqueue(new Callback<List<Server>>() {
            @Override
            public void onResponse(Call<List<Server>> call, Response<List<Server>> response) {
                if (getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Server> servers = response.body();

                    if ("webview".equalsIgnoreCase(servers.get(0).type)) {
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra("url", servers.get(0).url);

                        intent.putExtra("movie_url", movie.pageUrl);
                        intent.putExtra("title", movie.name);
                        intent.putExtra("logo", movie.logo);

                        startActivity(intent);
                    } else {
                        String serversJson = new Gson().toJson(servers);
                        Intent intent = new Intent(getContext(), PlayerActivity.class);
                        intent.putExtra("servers_json", serversJson);
                        intent.putExtra("url", movie.pageUrl);
                        intent.putExtra("title", movie.name);
                        intent.putExtra("logo", movie.logo);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getContext(), "No servers found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Server>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonStyles(String activeCategory) {
        btnRecent.setBackgroundTintList(ColorStateList.valueOf(activeCategory.equals("recent") ? COLOR_ACTIVE : COLOR_INACTIVE));
        btnMovies.setBackgroundTintList(ColorStateList.valueOf(activeCategory.equals("movies") ? COLOR_ACTIVE : COLOR_INACTIVE));
        btnSeries.setBackgroundTintList(ColorStateList.valueOf(activeCategory.equals("series") ? COLOR_ACTIVE : COLOR_INACTIVE));
    }
}