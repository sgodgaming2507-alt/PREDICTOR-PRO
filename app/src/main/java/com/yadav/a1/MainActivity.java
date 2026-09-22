package com.yadav.a1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // WebView setup karna
        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // JavaScript aur Android ka connection jodne ke liye
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // assets folder se html file load karna
        webView.loadUrl("file:///android_asset/injector.html");

        setContentView(webView);
    }

    // HTML ke buttons ke liye bridge class
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void closeMenu() {
            finish();
        }
    }
}
