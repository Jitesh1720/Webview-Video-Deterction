package com.example.webviewjavascriptdemo;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Context context;
    WebView webview;

    String[] extensionList = {"mp4", "m4a", "m4v", "f4v", "f4a", "m4b", "m4r", "f4b", "mov",
            "3gp", "3gp2", "3g2", "3gpp", "3gpp2", "ogg", "oga", "ogv", "ogx", "wmv", "wma", "asf*",
            "webm", "flv", "avi", "vob"};

    List arrayList;

    @SuppressLint("JavascriptInterface")
    @JavascriptInterface
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        arrayList = new ArrayList();

        arrayList = Arrays.asList(extensionList);

        webview = findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.getSettings().setPluginState(WebSettings.PluginState.ON);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
        webview.setWebViewClient(new MyWebViewClient());
        webview.loadUrl("https://www.google.com/");

    }


    class MyJavaScriptInterface {

        private Context ctx;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void showHTML(String html) {
//            new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html)
//                    .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();

            //Pattern linkPattern = Pattern.compile("(<a[^>]+>.+?<\\/a>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            //Pattern linkPattern = Pattern.compile("<a[^>]+href=[\"']?([\"'>]+)[\"']?[^>]*>(.+?)<\/a>",  Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
            Pattern linkPattern = Pattern.compile("<a[^>]+href=[\"']?([\"'>]+)[\"']?[^>]*>(.+?)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher pageMatcher = linkPattern.matcher(html);
            ArrayList<String> links = new ArrayList<String>();
            while (pageMatcher.find()) {
                links.add(pageMatcher.group());
                //Log.e("TAG", "showHTML: " + pageMatcher.group());
            }
        }

    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);

            String extension = url.substring(url.lastIndexOf(".") + 1);

            if (arrayList.contains(extension)) {
                Toast.makeText(context, "Resource Loaded : " + url, Toast.LENGTH_SHORT).show();
                Log.e("TAG", "onLoadResource: " + url);
            }

            /*for (int i = 0; i < extensionList.length; i++) {
                if (extension.equalsIgnoreCase(extensionList[i])) {
                    Toast.makeText(context, "Resource Loaded : " + url, Toast.LENGTH_SHORT).show();
                }
            }*/

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //super.onPageFinished(view, url);
            webview.loadUrl("javascript:window.HtmlViewer.showHTML" +
                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("youtube") && !url.contains("-youtube")) {
                Toast.makeText(context, "This is youtube url", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                String newUrl = checkUrl(url);
                if (Patterns.WEB_URL.matcher(newUrl).matches()) {
                    //Toast.makeText(context, "This is url : " + newUrl, Toast.LENGTH_SHORT).show();
                    view.loadUrl(newUrl);
                } else {
                    //Toast.makeText(context, "This is url : " + url, Toast.LENGTH_SHORT).show();
                    view.loadUrl(String.format("http://google.com/search?tbm=vid&q=%s -youtube -site:youtube.com", new Object[]{url}));
                }
                return false;
            }
        }
    }

    public String checkUrl(String str) {
        if (str == null) {
            return str;
        }
        StringBuilder stringBuilder;
        if (Build.VERSION.SDK_INT < 28) {
            if (!str.startsWith("http")) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("http://");
                stringBuilder.append(str);
                str = stringBuilder.toString();
            }
            return str;
            //return (str.contains("tune.pk") && str.startsWith("http")) ? str.replaceFirst("http", "https") : str;
        } else if (str.startsWith("https")) {
            return str;
        } else {
            if (str.startsWith("http")) {
                return str.replaceFirst("http", "https");
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append("https://");
            stringBuilder.append(str);
            return stringBuilder.toString();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @JavascriptInterface
    public void processVideo(final String vidData, final String vidID) {
        try {
            String mBaseFolderPath = android.os.Environment
                    .getExternalStorageDirectory()
                    + File.separator
                    + "FacebookVideos" + File.separator;
            if (!new File(mBaseFolderPath).exists()) {
                new File(mBaseFolderPath).mkdir();
            }
            String mFilePath = "file://" + mBaseFolderPath + "/" + vidID + ".mp4";

            Uri downloadUri = Uri.parse(vidData);
            DownloadManager.Request req = new DownloadManager.Request(downloadUri);
            req.setDestinationUri(Uri.parse(mFilePath));
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager dm = (DownloadManager) getSystemService(getApplicationContext().DOWNLOAD_SERVICE);
            dm.enqueue(req);
            Toast.makeText(this, "Download Started", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Download Failed: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

}
