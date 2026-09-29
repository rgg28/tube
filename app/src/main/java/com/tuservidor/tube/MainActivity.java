package com.tuservidor.tube;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "TubeLogin";

    // Deep Link propio de nuestra aplicación
    private static final String REDIRECT_URI =
            "com.tuservidor.tube://oauth2redirect";

    private WebView myWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ================================================================
        // PANTALLA COMPLETA
        // ================================================================

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // ================================================================
        // WEBVIEW
        // ================================================================

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

        // ================================================================
        // WEBVIEW CLIENT
        // ================================================================

        myWebView.setWebViewClient(new WebViewClient() {

            // ------------------------------------------------------------
            // Android API 24+
            // ------------------------------------------------------------

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                Uri uri = request.getUrl();

                return handleUrl(uri);
            }

            // ------------------------------------------------------------
            // Android API < 24
            // ------------------------------------------------------------

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url) {

                return handleUrl(Uri.parse(url));
            }

            // ------------------------------------------------------------
            // Página cargada
            // ------------------------------------------------------------

            @Override
            public void onPageFinished(
                    WebView view,
                    String url) {

                super.onPageFinished(view, url);

                Log.d(TAG, "Página cargada: " + url);

                injectAdBlocker();
            }
        });

        // ================================================================
        // PROCESAR DEEP LINK SI LA APP FUE ABIERTA MEDIANTE OAUTH
        // ================================================================

        handleIntent(getIntent());

        // ================================================================
        // CARGAR YOUTUBE
        // ================================================================

        myWebView.loadUrl("https://www.youtube.com/");
    }

    // ====================================================================
    // MANEJAR INTENT RECIBIDO
    // ====================================================================

    private void handleIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        Uri data = intent.getData();

        if (data == null) {
            return;
        }

        Log.d(TAG, "Intent recibido: " + data);

        String scheme = data.getScheme();
        String host = data.getHost();

        if (scheme == null || host == null) {
            return;
        }

        if (scheme.equalsIgnoreCase("com.tuservidor.tube")
                && host.equalsIgnoreCase("oauth2redirect")) {

            Log.d(TAG, "DEEP LINK OAuth recibido");

            handleOAuthCallback(data);
        }
    }

    // ====================================================================
    // CALLBACK DEL LOGIN
    // ====================================================================

    private void handleOAuthCallback(Uri uri) {

        Log.d(TAG, "OAuth callback: " + uri);

        String error = uri.getQueryParameter("error");

        if (error != null) {

            Log.e(
                    TAG,
                    "Google OAuth devolvió error: " + error
            );

            return;
        }

        String code =
                uri.getQueryParameter("code");

        String state =
                uri.getQueryParameter("state");

        if (code != null) {

            Log.d(
                    TAG,
                    "Authorization code recibido"
            );

            if (state != null) {

                Log.d(
                        TAG,
                        "State recibido"
                );
            }

            /*
             * Aquí tenemos el authorization code.
             *
             * IMPORTANTE:
             *
             * Este código no se convierte directamente
             * en una sesión de YouTube.
             *
             * Debe utilizarse con el proveedor OAuth
             * correspondiente y con un client_id autorizado.
             */

            exchangeCodeForToken(code, state);
        }
    }

    // ====================================================================
    // INTERCAMBIO DEL AUTHORIZATION CODE
    // ====================================================================

    private void exchangeCodeForToken(
            String code,
            String state) {

        /*
         * Preparado para implementar el intercambio OAuth.
         *
         * No ponemos client_secret dentro del APK.
         */

        Log.d(
                TAG,
                "Authorization code listo para intercambio"
        );
    }

    // ====================================================================
    // MANEJO CENTRALIZADO DE URL
    // ====================================================================

    private boolean handleUrl(Uri uri) {

        if (uri == null) {
            return true;
        }

        // ---------------------------------------------------------------
        // LOG DE TODAS LAS URL
        // ---------------------------------------------------------------

        Log.d(
                TAG,
                "NAVEGACIÓN: " + uri.toString()
        );

        String scheme = uri.getScheme();
        String host = uri.getHost();

        if (scheme == null) {
            return true;
        }

        scheme = scheme.toLowerCase();

        if (host != null) {
            host = host.toLowerCase();
        }

        // ===============================================================
        // NUESTRO DEEP LINK
        // ===============================================================

        if (scheme.equals("com.tuservidor.tube")) {

            if ("oauth2redirect".equals(host)) {

                handleOAuthCallback(uri);
            }

            return true;
        }

        // ===============================================================
        // BLOQUEAR YOUTUBE APP
        // ===============================================================

        if (scheme.equals("youtube")) {

            Log.d(
                    TAG,
                    "Bloqueado esquema youtube://"
            );

            return true;
        }

        if (scheme.equals("vnd.youtube")) {

            Log.d(
                    TAG,
                    "Bloqueado esquema vnd.youtube://"
            );

            return true;
        }

        // ===============================================================
        // INTENT://
        // ===============================================================

        if (scheme.equals("intent")) {

            String intentUrl = uri.toString();

            Log.d(
                    TAG,
                    "Intent externo detectado: " + intentUrl
            );

            // Bloquear específicamente YouTube oficial
            if (intentUrl.contains(
                    "com.google.android.youtube")) {

                Log.d(
                        TAG,
                        "Bloqueado Intent hacia YouTube"
                );

                return true;
            }

            // Por seguridad tampoco entregamos
            // otros intent:// al sistema.
            return true;
        }

        // ===============================================================
        // GOOGLE ACCOUNTS
        // ===============================================================

        if (host != null &&
                (host.equals("accounts.google.com")
                        || host.endsWith(".accounts.google.com"))) {

            Log.d(
                    TAG,
                    "Google Accounts detectado"
            );

            openGoogleLogin(uri);

            return true;
        }

        // ===============================================================
        // GOOGLE LOGIN / OAUTH
        // ===============================================================

        if (host != null &&
                (host.equals("google.com")
                        || host.endsWith(".google.com"))) {

            String path = uri.getPath();

            if (path != null &&
                    (path.contains("signin")
                            || path.contains("ServiceLogin")
                            || path.contains("oauth")
                            || path.contains("auth"))) {

                Log.d(
                        TAG,
                        "Google OAuth detectado"
                );

                openGoogleLogin(uri);

                return true;
            }
        }

        // ===============================================================
        // HTTP / HTTPS
        // ===============================================================

        if (scheme.equals("http")
                || scheme.equals("https")) {

            // YouTube permanece en nuestro WebView
            if (host != null &&
                    (host.equals("youtube.com")
                            || host.equals("www.youtube.com")
                            || host.endsWith(".youtube.com")
                            || host.equals("youtu.be")
                            || host.endsWith(".youtu.be"))) {

                return false;
            }

            // Google Accounts ya fue procesado
            if (host != null &&
                    (host.equals("accounts.google.com")
                            || host.endsWith(".accounts.google.com"))) {

                return true;
            }

            // Otros enlaces HTTPS
            // permanecen en el WebView
            return false;
        }

        // ===============================================================
        // OTROS ESQUEMAS
        // ===============================================================

        Log.d(
                TAG,
                "Esquema externo bloqueado: " + scheme
        );

        return true;
    }

    // ====================================================================
    // GOOGLE LOGIN
    // ====================================================================

    private void openGoogleLogin(Uri uri) {

        try {

            Log.d(
                    TAG,
                    "Abriendo Google Login mediante Custom Tab"
            );

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

            Log.e(
                    TAG,
                    "Error al abrir Custom Tab",
                    e
            );

            try {

                Intent browserIntent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                uri
                        );

                startActivity(browserIntent);

            } catch (Exception ignored) {

                Log.e(
                        TAG,
                        "No se pudo abrir Google Login",
                        ignored
                );
            }
        }
    }

    // ====================================================================
    // RECIBIR DEEP LINK CUANDO LA ACTIVIDAD YA ESTÁ ABIERTA
    // ====================================================================

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        setIntent(intent);

        Log.d(
                TAG,
                "onNewIntent recibido"
        );

        handleIntent(intent);
    }

    // ====================================================================
    // INJECT.JS
    // ====================================================================

    private void injectAdBlocker() {

        try {

            InputStream inputStream =
                    getAssets().open("inject.js");

            int size =
                    inputStream.available();

            byte[] buffer =
                    new byte[size];

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

            Log.e(
                    TAG,
                    "Error cargando inject.js",
                    e
            );
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

        if (myWebView != null
                && myWebView.canGoBack()) {

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
