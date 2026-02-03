package com.dz.gamezone;
import com.google.gson.annotations.SerializedName;

public class AppVersion {
    @SerializedName("version_code")
    public int versionCode;

    @SerializedName("apk_url")
    public String apkUrl;
}