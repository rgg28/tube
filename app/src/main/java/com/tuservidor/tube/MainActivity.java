package com.tuservidor.tube;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla completa
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        myWebView = new WebView(this);
        setContentView(myWebView);

        WebSettings webSettings = myWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        // User-Agent
        webSettings.setUserAgentString(
                "Mozilla/5.0 (iPad; CPU OS 16_5 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                "Version/16.5 Mobile/15E148 Safari/604.1"
        );

        myWebView.setWebViewClient(new WebViewClient() {

            // ============================================================
            // ANDROID API 24+
            // ============================================================
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {

                Uri uri = request.getUrl();

                return handleUrl(uri);
            }

            // ============================================================
            // ANDROID API < 24
            // ============================================================
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {

                return handleUrl(Uri.parse(url));
            }

            // ============================================================
            // PÁGINA TERMINÓ DE CARGAR
            // ============================================================
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                injectAdBlocker();
            }
        });

        // ================================================================
        // CARGAR YOUTUBE
        // ================================================================
        myWebView.loadUrl("https://www.youtube.com/");
    }

    // ====================================================================
    // MANEJO CENTRALIZADO DE URL
    // ====================================================================
    private boolean handleUrl(Uri uri) {

        if (uri == null) {
            return true;
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();

        if (scheme == null) {
            return true;
        }

        scheme = scheme.toLowerCase();

        if (host != null) {
            host = host.toLowerCase();
        }

        // ================================================================
        // BLOQUEAR COMPLETAMENTE LA APP OFICIAL DE YOUTUBE
        // ================================================================

        // youtube://video/...
        if (scheme.equals("youtube")) {
            return true;
        }

        // vnd.youtube://...
        if (scheme.equals("vnd.youtube")) {
            return true;
        }

        // ================================================================
        // INTENT://...
        // ================================================================

        if (scheme.equals("intent")) {

            String url = uri.toString();

            // Si el Intent intenta abrir la aplicación oficial
            if (url.contains("com.google.android.youtube")) {
                return true;
            }

            // También evitamos lanzar Intents externos desconocidos
            // desde el WebView.
            return true;
        }

        // ================================================================
        // GOOGLE LOGIN
        // ================================================================

        if (host != null &&
                (host.equals("accounts.google.com")
                        || host.endsWith(".accounts.google.com"))) {

            openInBrowser(uri);
            return true;
        }

        // ================================================================
        // GOOGLE OAUTH / SIGN-IN
        // ================================================================

        if (host != null &&
                (host.equals("google.com")
                        || host.endsWith(".google.com"))) {

            String path = uri.getPath();

            if (path != null &&
                    (path.contains("signin")
                            || path.contains("ServiceLogin")
                            || path.contains("oauth")
                            || path.contains("auth"))) {

                openInBrowser(uri);
                return true;
            }
        }

        // ================================================================
        // HTTP / HTTPS
        // ================================================================

        if (scheme.equals("http") || scheme.equals("https")) {

            // YouTube siempre permanece dentro de nuestra app.
            if (host != null &&
                    (host.equals("youtube.com")
                            || host.endsWith(".youtube.com")
                            || host.equals("youtu.be")
                            || host.endsWith(".youtu.be"))) {

                return false;
            }

            // Google Accounts ya fue tratado arriba.
            if (host != null &&
                    (host.equals("accounts.google.com")
                            || host.endsWith(".accounts.google.com"))) {

                return true;
            }

            // Otros enlaces HTTPS también permanecen en el WebView.
            return false;
        }

        // ================================================================
        // CUALQUIER OTRO ESQUEMA
        // ================================================================

        // No permitimos que Android entregue el enlace
        // a otra aplicación.
        return true;
    }

    // ====================================================================
    // ABRIR GOOGLE LOGIN EN EL NAVEGADOR / CUSTOM TAB
    // ====================================================================
    private void openInBrowser(Uri uri) {

        try {

            CustomTabsIntent.Builder builder =
                    new CustomTabsIntent.Builder();

            builder.setShowTitle(true);

            CustomTabsIntent customTabsIntent =
                    builder.build();

            customTabsIntent.launchUrl(
                    MainActivity.this,
                    uri
            );

        } catch (Exception e) {

            // Si no existe navegador compatible con Custom Tabs,
            // no intentamos lanzar YouTube.
            try {

                Intent browserIntent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                uri
                        );

                // Evitamos específicamente YouTube.
                browserIntent.setPackage(null);

                startActivity(browserIntent);

            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    // ====================================================================
    // INYECTAR inject.js
    // ====================================================================
    private void injectAdBlocker() {

        try {

            InputStream inputStream =
                    getAssets().open("inject.js");

            int size = inputStream.available();

            byte[] buffer = new byte[size];

            inputStream.read(buffer);

            inputStream.close();

            String jsCode =
                    new String(
                            buffer,
                            StandardCharsets.UTF_8
                    );

            myWebView.evaluateJavascript(
                    jsCode,
                    null
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ====================================================================
    // PAUSE
    // ====================================================================
    @Override
    protected void onPause() {

        super.onPause();

        if (myWebView != null) {
            myWebView.onPause();
        }
    }

    // ====================================================================
    // RESUME
    // ====================================================================
    @Override
    protected void onResume() {

        super.onResume();

        if (myWebView != null) {
            myWebView.onResume();
        }
    }

    // ====================================================================
    // BACK
    // ====================================================================
    @Override
    public void onBackPressed() {

        if (myWebView != null && myWebView.canGoBack()) {

            myWebView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    // ====================================================================
    // DESTROY
    // ====================================================================
    @Override
    protected void onDestroy() {

        if (myWebView != null) {

            myWebView.loadUrl("about:blank");
            myWebView.stopLoading();
            myWebView.setWebViewClient(null);
            myWebView.destroy();

            myWebView = null;
        }

        super.onDestroy();
    }
}
