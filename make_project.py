import os

base = os.path.expanduser("~/render-panel-app")
os.makedirs(f"{base}/app/src/main/java/com/soroush/panelapp", exist_ok=True)
os.makedirs(f"{base}/app/src/main/res/layout", exist_ok=True)
os.makedirs(f"{base}/app/src/main/res/values", exist_ok=True)
os.makedirs(f"{base}/.github/workflows", exist_ok=True)

with open(f"{base}/settings.gradle", "w", encoding="utf-8") as f:
    f.write("rootProject.name = 'SoroushPanelApp'\ninclude ':app'\n")

with open(f"{base}/build.gradle", "w", encoding="utf-8") as f:
    f.write("""buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.2'
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
""")

with open(f"{base}/app/build.gradle", "w", encoding="utf-8") as f:
    f.write("""plugins {
    id 'com.android.application'
}

android {
    namespace 'com.soroush.panelapp'
    compileSdk 34

    defaultConfig {
        applicationId "com.soroush.panelapp"
        minSdk 23
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
        }
        debug {
            minifyEnabled false
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
""")

with open(f"{base}/app/src/main/AndroidManifest.xml", "w", encoding="utf-8") as f:
    f.write("""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:label="Soroush Panel"
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""")

with open(f"{base}/app/src/main/res/layout/activity_main.xml", "w", encoding="utf-8") as f:
    f.write("""<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <WebView
        android:id="@+id/webview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</LinearLayout>
""")

with open(f"{base}/app/src/main/java/com/soroush/panelapp/MainActivity.java", "w", encoding="utf-8") as f:
    f.write("""package com.soroush.panelapp;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android:webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
""")

with open(f"{base}/app/src/main/assets/index.html", "w", encoding="utf-8") as f:
    f.write("""<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>اتصال به سرور Render</title>
<style>
  body { font-family: Tahoma, sans-serif; background: #121212; color: #eee; margin: 0; padding: 16px; }
  .box { max-width: 460px; margin: 0 auto 14px; background: #1c1c1c; border: 1px solid #333; border-radius: 8px; padding: 14px; }
  h3 { margin: 0 0 10px; font-size: 15px; }
  label { display: block; font-size: 12px; color: #bbb; margin-bottom: 6px; }
  input { width: 100%; box-sizing: border-box; padding: 10px; border-radius: 6px; border: 1px solid #444; background: #111; color: #fff; direction: ltr; }
  button { width: 100%; padding: 10px; margin-top: 10px; border: 0; border-radius: 6px; background: #2b6cb0; color: #fff; font-family: Tahoma, sans-serif; font-weight: bold; cursor: pointer; }
  .msg { margin-top: 10px; font-size: 12px; color: #ccc; word-break: break-word; }
</style>
</head>
<body>
<div class="box">
  <h3>مرحله ۱: اتصال به هاست Render</h3>
  <label>آدرس سرور Render (مثلاً https://botpy-oqij.onrender.com):</label>
  <input id="srv_url" type="text" placeholder="https://your-app.onrender.com">
  <button onclick="connectRender()">اتصال و باز کردن پنل</button>
  <div id="msg" class="msg">آدرس سرور را وارد کنید.</div>
</div>

<script>
const saved = localStorage.getItem('render_base_url') || '';
if (saved) document.getElementById('srv_url').value = saved;

async function connectRender() {
  let raw = document.getElementById('srv_url').value.trim();
  if (!raw) {
    document.getElementById('msg').innerText = 'لطفاً آدرس Render را وارد کنید.';
    return;
  }
  if (!raw.startsWith('http://') && !raw.startsWith('https://')) {
    raw = 'https://' + raw;
  }
  raw = raw.replace(/\\/+$/, '');
  localStorage.setItem('render_base_url', raw);
  document.getElementById('msg').innerText = 'در حال بررسی اتصال به سرور...';

  try {
    const res = await fetch(raw + '/health');
    if (res.ok) {
      window.location.href = raw + '/panel';
    } else {
      window.location.href = raw + '/panel';
    }
  } catch (e) {
    document.getElementById('msg').innerText = 'خطا در اتصال به سرور؛ لطفاً آدرس یا اینترنت را بررسی کنید.';
  }
}
</script>
</body>
</html>
""")

with open(f"{base}/.github/workflows/build-apk.yml", "w", encoding="utf-8") as f:
    f.write("""name: Build Android APK

on:
  workflow_dispatch:
  push:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3

      - name: Build Debug APK
        run: gradle assembleDebug --no-daemon

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: soroush-panel-apk
          path: app/build/outputs/apk/debug/app-debug.apk
""")

print("پروژه اندروید با موفقیت ساخته شد.")
