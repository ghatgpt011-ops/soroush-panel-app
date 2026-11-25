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
    private static final String PREFS = "fonex_prefs";
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

        etUrl.setText(prefs.getString("server_url", ""));
        etUser.setText(prefs.getString("username", ""));

        WebSettings ws = panelWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        panelWebView.setWebViewClient(new WebViewClient());
        panelWebView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        btnLogin.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            String user = etUser.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                tvStatus.setText("آدرس سرور، نام کاربری و رمز عبور را وارد کنید.");
                return;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            prefs.edit()
                .putString("server_url", url)
                .putString("username", user)
                .apply();

            openPanel(url, user);
        });
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
                panelWebView.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (panelWebView.getVisibility() == View.VISIBLE) {
            panelWebView.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
