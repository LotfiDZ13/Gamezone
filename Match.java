package com.dz.gamezone;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Match {
    @SerializedName("id") public String id;
    @SerializedName("league") public String league;
    @SerializedName("home_team") public String homeTeam;
    @SerializedName("away_team") public String awayTeam;
    @SerializedName("home_logo") public String homeLogo;
    @SerializedName("away_logo") public String awayLogo;
    @SerializedName("match_time") public String time;
    @SerializedName("status") public String status;

    // Ensure Server class also exists, or remove this list if you aren't using it yet
    @SerializedName("servers") public List<Server> servers;
}