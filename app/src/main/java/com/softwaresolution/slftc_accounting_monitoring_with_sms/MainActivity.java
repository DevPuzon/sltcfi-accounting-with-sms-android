package com.softwaresolution.slftc_accounting_monitoring_with_sms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity  {
    private final int STORAGE_PERMISSION_CODE = 1;
    private WebView mWebView;
    private static final String TAG = "MainActivity";

    private SharedPreferences sharedPref ;
    private SharedPreferences.Editor editor;

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to download files")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        requestStoragePermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.activity_main_webview);
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new HelloWebViewClient(this));
        mWebView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            Uri source = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(source);
            String cookies = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookies);
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading File...");
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
        });

        sharedPref = getSharedPreferences("sharedDoc", MODE_PRIVATE);
        editor = sharedPref.edit();
        String url =sharedPref.getString("url","");
        if(!TextUtils.isEmpty(url)){
            mWebView.loadUrl(url);
        }else{
            mWebView.loadUrl("https://sltfci.xyz/mobile/login");
        }
    }
    private static class HelloWebViewClient extends WebViewClient
    {
        MainActivity context;
        public HelloWebViewClient(MainActivity ctx){
            context = ctx;
        }
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url)
        {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            context.sharedPref = context.getSharedPreferences("sharedDoc", MODE_PRIVATE);
            context.editor = context.sharedPref.edit();
            context.editor.putString("url",url);
            context.editor.apply();
            Log.d(TAG,"doUpdateVisitedHistory: "+url);
        }
    }

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) {
            Log.d(TAG,"WebAppInterface: ");
            mContext = c;
        }
        @JavascriptInterface
        public String getFcmToken() {
            sharedPref = mContext.getSharedPreferences("sharedDoc", MODE_PRIVATE);
            editor = sharedPref.edit();
            String fcm_token= sharedPref.getString("fcm_token","");
            Log.d(TAG,"fcm_token: "+fcm_token);
            return  fcm_token;
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}