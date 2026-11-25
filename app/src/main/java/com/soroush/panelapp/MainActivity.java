package com.soroush.panelapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "panel_layken_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etUrl = findViewById(R.id.etServerUrl);
        EditText etUser = findViewById(R.id.etUsername);
        EditText etPass = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvStatus = findViewById(R.id.tvStatus);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        etUrl.setText(prefs.getString("server_url", ""));
        etUser.setText(prefs.getString("username", ""));

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

            tvStatus.setText("تنظیمات ذخیره شد: " + url);
        });
    }
}
