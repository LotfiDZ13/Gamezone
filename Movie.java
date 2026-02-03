package com.dz.gamezone;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("name")
    public String name;

    @SerializedName("logo")
    public String logo;

    // 🟢 CRITICAL FIX: This tells Gson to map "url" from JSON to "pageUrl" in Java
    @SerializedName("url")
    public String pageUrl;

    @SerializedName("badge")
    public String badge;
}