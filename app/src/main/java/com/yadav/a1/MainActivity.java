package com.yadav.a1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private WindowManager windowManager;
    private WebView webView;
    private WindowManager.LayoutParams params;
    private boolean isOverlayAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android 15 ke liye Overlay Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            showFloatingOverlay();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showFloatingOverlay() {
        if (isOverlayAdded) return;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        webView = new WebView(this);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setBackgroundColor(0x00000000); // Transparent background

        // HTML ke JavaScript methods ke liye bridge
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.loadUrl("file:///android_asset/injector.html");

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int windowSize = (int) (280 * getResources().getDisplayMetrics().density);

        params = new WindowManager.LayoutParams(
                windowSize,
                windowSize,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        // Overlay ko drag karne ke liye touch listener
        webView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(webView, params);
                        return true;
                }
                return false;
            }
        });

        try {
            windowManager.addView(webView, params);
            isOverlayAdded = true;
            // App ko background bhej do taaki game ke upar overlay tairta rahe
            moveTaskToBack(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void closeMenu() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isOverlayAdded && webView != null) {
                        windowManager.removeView(webView);
                        isOverlayAdded = false;
                    }
                    finishAffinity();
                    System.exit(0);
                }
            });
        }

        @JavascriptInterface
        public void showToast(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                showFloatingOverlay();
            } else {
                Toast.makeText(this, "Overlay permission is required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isOverlayAdded && webView != null) {
            windowManager.removeView(webView);
            isOverlayAdded = false;
        }
    }
}
