package com.dz.gamezone;

import com.google.gson.annotations.SerializedName;
import java.util.Map; // Import Map

public class Server {
    @SerializedName("name")
    public String name;

    @SerializedName("url")
    public String url;

    @SerializedName("type")
    public String type;

    // 🟢 NEW: Add Headers Map
    @SerializedName("headers")
    public Map<String, String> headers;
}