package com.kaiguanjs.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kaiguanjs.R;
import com.werb.permissionschecker.PermissionChecker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class WebViewActivity extends Activity {
    public static final String URL = "url";

    ProgressBar mPbLoading;
    WebView mWvContent;

    private String imgurl;

    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private PermissionChecker permissionChecker;

    public static void launch(Activity from,String url){
        Intent intent1 = new Intent(from, WebViewActivity.class);
        intent1.putExtra(WebViewActivity.URL, url);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        from.startActivity(intent1);
//        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yqc_webview);
        mPbLoading = findViewById(R.id.pb_loading);
        mWvContent = findViewById(R.id.wv_content);
        permissionChecker = new PermissionChecker(this);

        initData();
        initListener();
    }

    private void showToast(String str) {
        Toast.makeText(WebViewActivity.this, str, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initData() {
        mWvContent.getSettings().setJavaScriptEnabled(true);
        mWvContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWvContent.getSettings().setDefaultTextEncodingName("UTF-8");
        mWvContent.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWvContent.getSettings().setSupportMultipleWindows(true);// 多窗口
        mWvContent.getSettings().setUseWideViewPort(true);
        mWvContent.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWvContent.getSettings().setDomStorageEnabled(true);

        mWvContent.getSettings().setSupportZoom(false);
        mWvContent.getSettings().setBuiltInZoomControls(false);

        mWvContent.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mWvContent.getSettings().setLoadWithOverviewMode(true);

        WebSettings webseting = mWvContent.getSettings();
        webseting.setDomStorageEnabled(true);
        webseting.setAppCacheMaxSize(1024 * 1024 * 8);
        String appCacheDir = this.getApplicationContext().getDir("cache", MODE_PRIVATE).getPath();
        webseting.setAppCachePath(appCacheDir);
        webseting.setAllowFileAccess(true);
        webseting.setAppCacheEnabled(true);
        webseting.setCacheMode(WebSettings.LOAD_DEFAULT);

        mWvContent.getSettings().setBlockNetworkImage(true);

        mWvContent.requestFocusFromTouch();

        mWvContent.getSettings().setLoadsImagesAutomatically(true);
        mWvContent.setDownloadListener(new MyWebViewDownLoadListener());

        mWvContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mPbLoading.setProgress(newProgress);
                if (newProgress == 100) {
                    mWvContent.getSettings().setBlockNetworkImage(false);
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView newWebView = new WebView(WebViewActivity.this);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        mWvContent.loadUrl(url);
                        return true;
                    }
                });
                return true;
            }
        });

        String url = getIntent().getStringExtra(URL);
        mWvContent.loadUrl(url);

    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initListener() {
        WebSettings settings = mWvContent.getSettings();
        settings.setJavaScriptEnabled(true);
        mWvContent.setOnLongClickListener(v -> {
            WebView.HitTestResult result = ((WebView) v).getHitTestResult();
            if (null == result)
                return false;
            int type = result.getType();
            if (type == WebView.HitTestResult.UNKNOWN_TYPE)
                return false;
            switch (type) {
                case WebView.HitTestResult.PHONE_TYPE:
                    break;
                case WebView.HitTestResult.EMAIL_TYPE:
                    break;
                case WebView.HitTestResult.GEO_TYPE:
                    break;
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                    break;
                case WebView.HitTestResult.IMAGE_TYPE:
                    imgurl = result.getExtra();
                    dialogList();
                    break;
                default:
                    break;
            }
            return true;
        });

        mWvContent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mPbLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mPbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";

                builder.setTitle("SSL Certificate Error");
                builder.setMessage(message);
                builder.setPositiveButton("Continue", (dialog, which) -> handler.proceed());
                builder.setNegativeButton("Cancel", (dialog, which) -> handler.cancel());
                final AlertDialog dialog = builder.create();
                dialog.show();
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http") || url.startsWith("https")) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
                else if(url.startsWith("intent:")){
                    String[] urlSplit = url.split("/");
                    String send = "";

                    if(urlSplit[2].equals("user")){
                        send = "https://m.me/"+urlSplit[3];

                    }else if (urlSplit[2].equals("ti")){
                      String data  = urlSplit[4];
                        String[] newSplit = data.split("#");
                        send = "https://line.me/R/"+newSplit[0];
                    }// showToast(url);

                    Intent newInt = new Intent(Intent.ACTION_VIEW, Uri.parse(send));
                    startActivity(newInt);
                }
                else {
                    try {
                        Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(in);
                    } catch (Exception e) {
                        showToast("跳转失败");
                    }
                }
                return true;

            }
        });

        mWvContent.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (i == KeyEvent.KEYCODE_BACK && mWvContent.canGoBack()) {
                    mWvContent.goBack();
                    return true;
                }
            }
            return false;
        });
    }

    private class MyWebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void dialogList() {
        final String[] items = {"保存图片", "取消"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, 3);
        builder.setItems(items, (dialog, which) -> {
            dialog.dismiss();
            if ("保存图片".equals(items[which])) {
                if (permissionChecker.isLackPermissions(PERMISSIONS)) {
                    permissionChecker.requestPermissions();
                } else {
                    new SaveImage().execute();
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionChecker.PERMISSION_REQUEST_CODE) {
            if (permissionChecker.hasAllPermissionsGranted(grantResults)) {
                new SaveImage().execute();
            } else {
                permissionChecker.showDialog();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveImage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
                String sdcard = Environment.getExternalStorageDirectory().toString();
                File file = new File(sdcard + "/Download");
                if (!file.exists()) {
                    file.mkdirs();
                }
                int idx = imgurl.lastIndexOf(".");
                String ext = imgurl.substring(idx);
                file = new File(sdcard + "/Download/" + new Date().getTime() + ext);
                InputStream inputStream = null;
                java.net.URL url = new URL(imgurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(20000);
                if (conn.getResponseCode() == 200) {
                    inputStream = conn.getInputStream();
                }
                byte[] buffer = new byte[4096];
                int len = 0;
                FileOutputStream outStream = new FileOutputStream(file);
                while (true) {
                    assert inputStream != null;
                    if ((len = inputStream.read(buffer)) == -1) break;
                    outStream.write(buffer, 0, len);
                }
                outStream.close();
                result = "保存成功";
            } catch (Exception e) {
                result = "保存失败！" + e.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            showToast(result);
        }
    }

    private static long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mWvContent.canGoBack()) {
                mWvContent.goBack();
            } else {
                if ((System.currentTimeMillis() - exitTime) > 2000)
                {
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
