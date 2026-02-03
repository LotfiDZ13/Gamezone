package com.dz.gamezone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    private boolean isLaunched = false;
    private String sniffedAutoUrl = null;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    // 🟢 ADDED: Variables to hold movie info
    private String movieUrl;
    private String movieTitle;
    private String movieLogo;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.BLACK);

        webView = new WebView(this);
        webView.setVisibility(View.INVISIBLE);
        rootLayout.addView(webView);

        progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams pbParams = new FrameLayout.LayoutParams(150, 150, 17);
        rootLayout.addView(progressBar, pbParams);

        setContentView(rootLayout);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                extractVideoQualities();
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (sniffedAutoUrl == null && (url.contains(".mp4") || url.contains(".m3u8"))) {
                    sniffedAutoUrl = url;
                    startFallbackTimer();
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        String url = getIntent().getStringExtra("url");
        String referer = getIntent().getStringExtra("referer");

        // 🟢 FIX 2: RETRIEVE THE DATA
        movieUrl = getIntent().getStringExtra("movie_url");
        movieTitle = getIntent().getStringExtra("title");
        movieLogo = getIntent().getStringExtra("logo");

        if (url != null) {
            Map<String, String> headers = new HashMap<>();
            if (referer != null) headers.put("Referer", referer);
            webView.loadUrl(url, headers);
            Toast.makeText(this, "Analyzing video...", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFallbackTimer() {
        if (timeoutRunnable != null) return;
        timeoutRunnable = () -> {
            if (!isLaunched && sniffedAutoUrl != null) {
                List<Server> singleList = new ArrayList<>();
                singleList.add(createServer("VidTube (Auto)", sniffedAutoUrl));
                launchPlayerActivity(singleList);
            }
        };
        handler.postDelayed(timeoutRunnable, 4000);
    }

    private void extractVideoQualities() {
        if (isLaunched) return;
        String js = "javascript:(function() {" +
                "    var interval = setInterval(function() {" +
                "        try {" +
                "            var player = jwplayer();" +
                "            if (!player) player = jwplayer('vplayer');" +
                "            if (player && player.getPlaylist && player.getPlaylist().length > 0) {" +
                "                var sources = player.getPlaylist()[0].sources;" +
                "                if (sources && sources.length > 0) {" +
                "                    clearInterval(interval);" +
                "                    window.Android.onQualitiesFound(JSON.stringify(sources));" +
                "                }" +
                "            }" +
                "        } catch(e) {}" +
                "    }, 500);" +
                "    setTimeout(function() { clearInterval(interval); }, 8000);" +
                "})()";
        webView.evaluateJavascript(js, null);
    }

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }

        @JavascriptInterface
        public void onQualitiesFound(String jsonString) {
            if (isLaunched) return;
            isLaunched = true;
            if (timeoutRunnable != null) handler.removeCallbacks(timeoutRunnable);

            handler.post(() -> {
                try {
                    List<Server> serverList = new ArrayList<>();
                    JSONArray sources = new JSONArray(jsonString);

                    for (int i = 0; i < sources.length(); i++) {
                        JSONObject source = sources.getJSONObject(i);
                        String file = source.getString("file");
                        String label = source.optString("label", "HD");
                        serverList.add(createServer("VidTube - " + label, file));
                    }
                    Collections.reverse(serverList);

                    if (sniffedAutoUrl != null) {
                        boolean exists = false;
                        for(Server s : serverList) { if(s.url.equals(sniffedAutoUrl)) exists = true; }
                        if (!exists) {
                            serverList.add(createServer("VidTube (Auto)", sniffedAutoUrl));
                        }
                    }
                    launchPlayerActivity(serverList);

                } catch (Exception e) {
                    if (sniffedAutoUrl != null) {
                        List<Server> single = new ArrayList<>();
                        single.add(createServer("VidTube (Auto)", sniffedAutoUrl));
                        launchPlayerActivity(single);
                    }
                }
            });
        }
    }

    private Server createServer(String name, String url) {
        Server s = new Server();
        s.name = name;
        s.url = url;
        s.type = "player";
        s.headers = new HashMap<>();
        s.headers.put("User-Agent", webView.getSettings().getUserAgentString());
        s.headers.put("Referer", "https://vidtube.one/");
        return s;
    }

    private void launchPlayerActivity(List<Server> servers) {
        if (servers.isEmpty()) return;
        isLaunched = true;

        String json = new Gson().toJson(servers);
        Intent intent = new Intent(WebViewActivity.this, PlayerActivity.class);
        intent.putExtra("servers_json", json);

        // 🟢 FIX 3: FORWARD THE DATA TO PLAYER
        // We map 'movie_url' back to 'url' because PlayerActivity expects 'url'
        intent.putExtra("url", movieUrl);
        intent.putExtra("title", movieTitle);
        intent.putExtra("logo", movieLogo);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("Android");
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}