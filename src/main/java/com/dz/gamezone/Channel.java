package com.dz.gamezone;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Channel {
    @SerializedName("name")
    public String name;

    @SerializedName("logo")
    public String logo;

    @SerializedName("category")
    public String category;  // <--- This was missing!

    @SerializedName("url")
    public String url;       // <--- Useful for website links

    @SerializedName("servers")
    public List<Server> servers;
}