package com.dz.gamezone;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query; // <--- THIS WAS MISSING!

public interface ApiService {

    // Get Matches (Type = matches is default)
    @GET("api.php?type=matches")
    Call<List<Match>> getMatches();

    // Get Categories (Folders like "BeIN", "Sky")
    @GET("api.php?type=categories")
    Call<List<Category>> getCategories();

    // Get Channels inside a specific Category
    @GET("api.php?type=channels")
    Call<List<Channel>> getChannelsByCat(@Query("cat") String categoryName);
    // Check for Updates
    @GET("version.json")
    Call<AppVersion> checkUpdate();
    // Add these to ApiService.java
    @GET("scraper.php?action=list")
    Call<List<Movie>> getScrapedMovies(@Query("url") String siteUrl);

    @GET("scraper.php?action=source")
    Call<List<Server>> getScrapedSources(@Query("link") String movieUrl);
    // Add this for TopCinema
    // ... your existing calls ...

    // 1. Fetch TopCinema Movie List
    @GET("scraper_topcinema.php?action=list")
    Call<List<Movie>> getTopCinemaMovies();

    // 2. Fetch TopCinema Video Links
    @GET("scraper_topcinema.php?action=source")
    Call<List<Server>> getTopCinemaSources(@Query("link") String link);
    // FASEL HD CALLS
    @GET("scraper_fasel.php?action=list")
    Call<List<Movie>> getFaselMovies();

    @GET("scraper_fasel.php?action=source")
    Call<List<Server>> getFaselSources(@Query("link") String link);
    // ... existing calls ...

    // 3. Fetch TopCinema Episodes / Seasons
    @GET("scraper_topcinema.php?action=episodes")
    Call<List<Movie>> getTopCinemaEpisodes(@Query("url") String url);
    // 🟢 GENERIC CALL: Allows us to pass the full "scraper_topcinema.php?action=..." string
    @GET
    Call<List<Movie>> getMoviesFromUrl(@retrofit2.http.Url String fullUrl);
}