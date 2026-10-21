package com.harsh.olympiadlab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    // Primary entry point: The genuine local production web application
    private static final String LOCAL_PRODUCTION_URL = "file:///android_asset/public/index.html";

    // Verified cloud backend and deployment endpoint
    private static final String CLOUD_DEPLOYMENT_URL = "https://ais-dev-wjxmplvizjfqw7pkhyr2ib-595060723507.asia-northeast1.run.app";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pre-initialize Android notification channel
        ReminderScheduler.createNotificationChannel(this);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);

        // Hardware back-button handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

        // Native Android Bridge for Web UI
        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidApp");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Keep local assets, cloud backend, and Google APIs / Sheets within WebView
                if (url.startsWith("file://") ||
                    url.contains("asia-northeast1.run.app") ||
                    url.contains("script.google.com") ||
                    url.contains("googleapis.com") ||
                    url.contains("docs.google.com") ||
                    url.contains("sheets.google.com")) {
                    return false;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                handleIntentForDeepLink(getIntent());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    // Fallback to local production assets if network or remote server is unreachable
                    Toast.makeText(MainActivity.this, "Offline fallback active: loaded local Olympiad Lab", Toast.LENGTH_SHORT).show();
                    view.loadUrl(LOCAL_PRODUCTION_URL);
                }
            }
        });

        // Always load genuine local production application first for instant offline startup
        webView.loadUrl(LOCAL_PRODUCTION_URL);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentForDeepLink(intent);
    }

    private void handleIntentForDeepLink(Intent intent) {
        if (intent != null && intent.hasExtra("targetTab") && webView != null) {
            String targetTab = intent.getStringExtra("targetTab");
            if (targetTab != null && !targetTab.trim().isEmpty()) {
                final String safeTab = targetTab.replace("'", "\\'");
                final String hashSlug = safeTab.replace('_', '-');
                runOnUiThread(() -> {
                    String js = "if (window.handleNativeTabNavigation) { " +
                                "  window.handleNativeTabNavigation('" + safeTab + "'); " +
                                "} else { " +
                                "  window.location.hash = '#/" + hashSlug + "'; " +
                                "}";
                    webView.evaluateJavascript(js, null);
                });
            }
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    public boolean areNotificationsEnabled() {
        return androidx.core.app.NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    public void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                        this,
                        new String[]{"android.permission.POST_NOTIFICATIONS"},
                        101
                );
            }
        }
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public String getCloudUrl() {
            return CLOUD_DEPLOYMENT_URL;
        }

        @JavascriptInterface
        public boolean isOnline() {
            return isNetworkAvailable();
        }

        @JavascriptInterface
        public String getAppVersion() {
            return "1.0.0";
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void switchToLocal() {
            runOnUiThread(() -> webView.loadUrl(LOCAL_PRODUCTION_URL));
        }

        @JavascriptInterface
        public void switchToCloud() {
            runOnUiThread(() -> {
                if (isNetworkAvailable()) {
                    webView.loadUrl(CLOUD_DEPLOYMENT_URL);
                } else {
                    Toast.makeText(mContext, "No internet connection. Staying in offline mode.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public boolean areNotificationsEnabled() {
            return MainActivity.this.areNotificationsEnabled();
        }

        @JavascriptInterface
        public void requestNotificationPermission() {
            runOnUiThread(MainActivity.this::requestNotificationPermission);
        }

        @JavascriptInterface
        public boolean scheduleReminder(String id, String title, String body, long triggerTimestampMs, String targetTab, String category) {
            return ReminderScheduler.scheduleExactReminder(mContext, id, title, body, triggerTimestampMs, targetTab, category);
        }

        @JavascriptInterface
        public boolean scheduleWeeklyReminder(String id, String title, String body, int dayOfWeek, int hour, int minute, String targetTab, String category) {
            return ReminderScheduler.scheduleWeeklyRecurringReminder(mContext, id, title, body, dayOfWeek, hour, minute, targetTab, category);
        }

        @JavascriptInterface
        public boolean cancelReminder(String id) {
            return ReminderScheduler.cancelReminder(mContext, id);
        }

        @JavascriptInterface
        public boolean cancelAllReminders() {
            return ReminderScheduler.cancelAllReminders(mContext);
        }

        @JavascriptInterface
        public String getScheduledRemindersJson() {
            return ReminderScheduler.getAllRemindersJson(mContext);
        }

        @JavascriptInterface
        public void showInstantNotification(String title, String body, String targetTab) {
            try {
                ReminderScheduler.createNotificationChannel(mContext);
                Intent launchIntent = new Intent(mContext, MainActivity.class);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                launchIntent.putExtra("targetTab", targetTab != null ? targetTab : "my_day");
                int reqCode = (int) System.currentTimeMillis();
                android.app.PendingIntent pi = android.app.PendingIntent.getActivity(
                        mContext,
                        reqCode,
                        launchIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                );
                androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(mContext, ReminderScheduler.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pi);
                androidx.core.app.NotificationManagerCompat.from(mContext).notify(reqCode, builder.build());
            } catch (Exception ignored) {}
        }
    }
}
