package com.soroush.panelapp;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "fonex_auth_prefs";
    private static final String VALID_USER = "FONEX";
    private static final String VALID_PASS = "MOHAMMAD_1234";

    private LinearLayout loginLayout;
    private WebView panelWebView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loginLayout = findViewById(R.id.loginLayout);
        panelWebView = findViewById(R.id.panelWebView);

        EditText etUrl = findViewById(R.id.etServerUrl);
        EditText etUser = findViewById(R.id.etUsername);
        EditText etPass = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvStatus = findViewById(R.id.tvStatus);

        String savedUrl = prefs.getString("server_url", "");
        String savedUser = prefs.getString("username", VALID_USER);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        etUrl.setText(savedUrl);
        etUser.setText(savedUser);

        WebSettings ws = panelWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        panelWebView.setWebViewClient(new WebViewClient());
        panelWebView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        btnLogin.setOnClickListener(v -> {
            String url = normalizeUrl(etUrl.getText().toString().trim());
            String user = etUser.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (!VALID_USER.equals(user) || !VALID_PASS.equals(pass)) {
                tvStatus.setText("نام کاربری یا رمز عبور اشتباه است.");
                return;
            }

            prefs.edit()
                .putString("server_url", url)
                .putString("username", user)
                .putBoolean("is_logged_in", true)
                .apply();

            openPanel(url, user);
        });

        if (isLoggedIn) {
            openPanel(normalizeUrl(savedUrl), VALID_USER);
        }
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private void openPanel(String url, String user) {
        loginLayout.setVisibility(View.GONE);
        panelWebView.setVisibility(View.VISIBLE);
        String target = "file:///android_asset/panel.html?server=" + Uri.encode(url) + "&user=" + Uri.encode(user);
        panelWebView.loadUrl(target);
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void logout() {
            runOnUiThread(() -> {
                prefs.edit().putBoolean("is_logged_in", false).apply();
                panelWebView.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
            });
        }

        @JavascriptInterface
        public void saveServerUrl(String newUrl) {
            runOnUiThread(() -> {
                prefs.edit().putString("server_url", normalizeUrl(newUrl)).apply();
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
