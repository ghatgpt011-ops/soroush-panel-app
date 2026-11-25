package com.soroush.panelapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "panel_prefs";
    private static final String KEY_URL = "server_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etUrl = findViewById(R.id.etServerUrl);
        Button btnConnect = findViewById(R.id.btnConnect);
        TextView tvStatus = findViewById(R.id.tvStatus);
        WebView webView = findViewById(R.id.webView);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String savedUrl = prefs.getString(KEY_URL, "");
        etUrl.setText(savedUrl);

        btnConnect.setOnClickListener(v -> {
            String raw = etUrl.getText().toString().trim();
            if (raw.isEmpty()) {
                tvStatus.setText("لطفاً آدرس سرور را وارد کنید.");
                return;
            }
            if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
                raw = "https://" + raw;
            }
            if (raw.endsWith("/")) {
                raw = raw.substring(0, raw.length() - 1);
            }
            prefs.edit().putString(KEY_URL, raw).apply();
            tvStatus.setText("در حال بارگذاری: " + raw + "/my-panel");
            webView.loadUrl(raw + "/my-panel");
        });

        if (!savedUrl.isEmpty()) {
            webView.loadUrl(savedUrl + "/my-panel");
        }
    }
}
