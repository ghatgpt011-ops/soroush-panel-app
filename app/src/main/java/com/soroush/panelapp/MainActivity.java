package com.soroush.panelapp;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "fonex_auth_prefs";
    private static final String VALID_USER = "FONEX";
    private static final String VALID_PASS = "MOHAMMAD_1234";

    private WebView loginWebView;
    private WebView panelWebView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loginWebView = findViewById(R.id.loginWebView);
        panelWebView = findViewById(R.id.panelWebView);

        setupWebView(loginWebView);
        setupWebView(panelWebView);

        String savedUrl = normalizeUrl(prefs.getString("server_url", ""));
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            openPanel(savedUrl, VALID_USER);
        } else {
            openLogin(savedUrl);
        }
    }

    private void setupWebView(WebView wv) {
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        url = url.trim();
        if (url.isEmpty()) return "";
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private void openLogin(String savedUrl) {
        panelWebView.setVisibility(View.GONE);
        loginWebView.setVisibility(View.VISIBLE);
        String target = "file:///android_asset/login.html?server=" + Uri.encode(savedUrl);
        loginWebView.loadUrl(target);
    }

    private void openPanel(String url, String user) {
        loginWebView.setVisibility(View.GONE);
        panelWebView.setVisibility(View.VISIBLE);
        String target = "file:///android_asset/panel.html?server=" + Uri.encode(url) + "&user=" + Uri.encode(user);
        panelWebView.loadUrl(target);
    }

    public class AndroidBridge {
        @JavascriptInterface
        public String login(String serverUrl, String username, String password) {
            String u = username == null ? "" : username.trim();
            String p = password == null ? "" : password.trim();
            String s = normalizeUrl(serverUrl);
            if (!VALID_USER.equals(u) || !VALID_PASS.equals(p)) {
                return "ERR:نام کاربری یا رمز عبور اشتباه است.";
            }
            prefs.edit()
                .putString("server_url", s)
                .putString("username", VALID_USER)
                .putBoolean("is_logged_in", true)
                .apply();
            runOnUiThread(() -> openPanel(s, VALID_USER));
            return "OK";
        }

        @JavascriptInterface
        public void logout() {
            runOnUiThread(() -> {
                prefs.edit().putBoolean("is_logged_in", false).apply();
                String s = normalizeUrl(prefs.getString("server_url", ""));
                openLogin(s);
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (panelWebView.getVisibility() == View.VISIBLE) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }
}
