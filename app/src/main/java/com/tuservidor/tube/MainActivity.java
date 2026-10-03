package com.tuservidor.tube;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String YOUTUBE_URL = "https://www.youtube.com/";

    private FrameLayout root;
    private LinearLayout mainLayout;
    private LinearLayout topBar;
    private LinearLayout bottomBar;

    private WebView webView;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private boolean fullscreen = false;

    /*
     * Filtro básico de contenido.
     *
     * No pretende identificar todo el tráfico publicitario de YouTube.
     * Bloquea dominios/patrones conocidos de publicidad y tracking
     * cuando la petición puede identificarse de forma segura.
     */
    private final Set<String> blockedHosts = new HashSet<>(
            Arrays.asList(
                    "doubleclick.net",
                    "doubleclick.com",
                    "googlesyndication.com",
                    "googleadservices.com",
                    "adservice.google.com",
                    "ads.youtube.com",
                    "pagead2.googlesyndication.com"
            )
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configurarSistema();
        crearInterfaz();
        configurarWebView();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(YOUTUBE_URL);
        }
    }

    private void configurarSistema() {

        getWindow().setStatusBarColor(Color.rgb(15, 15, 15));
        getWindow().setNavigationBarColor(Color.BLACK);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void crearInterfaz() {

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.BLACK);

        crearBarraSuperior();

        webView = new WebView(this);

        LinearLayout.LayoutParams webParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0
                );

        webParams.weight = 1;

        mainLayout.addView(webView, webParams);

        crearBarraInferior();

        root.addView(
                mainLayout,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(root);
    }

    private void crearBarraSuperior() {

        topBar = new LinearLayout(this);

        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(12), 0, dp(12), 0);
        topBar.setBackgroundColor(Color.rgb(20, 20, 20));

        TextView logo = crearBoton("Tube");

        logo.setTextColor(Color.WHITE);
        logo.setTextSize(21);
        logo.setGravity(Gravity.CENTER_VERTICAL);

        topBar.addView(
                logo,
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                )
        );

        TextView refresh = crearBoton("↻");

        refresh.setTextSize(25);

        refresh.setOnClickListener(
                v -> webView.reload()
        );

        topBar.addView(
                refresh,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(54)
                )
        );

        mainLayout.addView(
                topBar,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(54)
                )
        );
    }

    private void crearBarraInferior() {

        bottomBar = new LinearLayout(this);

        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER);
        bottomBar.setBackgroundColor(Color.rgb(20, 20, 20));

        agregarBotonInferior(
                "⌂",
                v -> webView.loadUrl(YOUTUBE_URL)
        );

        agregarBotonInferior(
                "←",
                v -> {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                }
        );

        agregarBotonInferior(
                "→",
                v -> {
                    if (webView.canGoForward()) {
                        webView.goForward();
                    }
                }
        );

        agregarBotonInferior(
                "↻",
                v -> webView.reload()
        );

        mainLayout.addView(
                bottomBar,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                )
        );
    }

    private void agregarBotonInferior(
            String texto,
            View.OnClickListener listener
    ) {

        TextView button = crearBoton(texto);

        button.setTextSize(22);
        button.setOnClickListener(listener);

        bottomBar.addView(
                button,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                )
        );
    }

    private TextView crearBoton(String texto) {

        TextView view = new TextView(this);

        view.setText(texto);
        view.setTextColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        view.setBackgroundColor(Color.TRANSPARENT);

        return view;
    }

    private void configurarWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);

        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        settings.setUserAgentString(
                settings.getUserAgentString()
                        + " TubeAndroid/1.0"
        );

        CookieManager cookies = CookieManager.getInstance();

        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setBackgroundColor(Color.BLACK);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {

                if (request == null || request.getUrl() == null) {
                    return true;
                }

                Uri uri = request.getUrl();

                String scheme = uri.getScheme();
                String host = uri.getHost();

                if (scheme == null) {
                    return true;
                }

                /*
                 * HTTP/HTTPS permanece dentro de nuestra aplicación.
                 */
                if ("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme)) {

                    return false;
                }

                /*
                 * Intentamos manejar enlaces especiales de autenticación
                 * y navegación mediante Android sin romper la sesión web.
                 */
                if ("intent".equalsIgnoreCase(scheme)) {

                    return true;
                }

                /*
                 * No lanzamos automáticamente aplicaciones externas.
                 */
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    WebResourceRequest request
            ) {

                if (request == null || request.getUrl() == null) {
                    return super.shouldInterceptRequest(view, request);
                }

                Uri uri = request.getUrl();

                if (esContenidoBloqueado(uri)) {

                    return new WebResourceResponse(
                            "text/plain",
                            "UTF-8",
                            new ByteArrayInputStream(
                                    new byte[0]
                            )
                    );
                }

                return super.shouldInterceptRequest(view, request);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onShowCustomView(
                    View view,
                    CustomViewCallback callback
            ) {

                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;

                root.addView(
                        customView,
                        new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        )
                );

                mainLayout.setVisibility(View.GONE);

                fullscreen = true;

                entrarFullscreen();
            }

            @Override
            public void onHideCustomView() {

                salirFullscreen();
            }
        });

        webView.setDownloadListener(
                (url, userAgent, contentDisposition, mimetype, contentLength) -> {

                    /*
                     * No abrimos automáticamente aplicaciones externas.
                     * Los enlaces de navegación normales permanecen en WebView.
                     */
                }
        );
    }

    private boolean esContenidoBloqueado(Uri uri) {

        String host = uri.getHost();

        if (host == null) {
            return false;
        }

        host = host.toLowerCase(Locale.US);

        for (String blocked : blockedHosts) {

            if (host.equals(blocked)
                    || host.endsWith("." + blocked)) {

                return true;
            }
        }

        String url = uri.toString().toLowerCase(Locale.US);

        List<String> patterns = Arrays.asList(
                "/pagead/",
                "/pagead2.",
                "/adsystem/",
                "/adservice/",
                "doubleclick"
        );

        for (String pattern : patterns) {

            if (url.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    private void entrarFullscreen() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void salirFullscreen() {

        if (customView != null) {

            root.removeView(customView);

            customView = null;

            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
                customViewCallback = null;
            }
        }

        mainLayout.setVisibility(View.VISIBLE);

        fullscreen = false;

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onBackPressed() {

        if (fullscreen) {
            salirFullscreen();
            return;
        }

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (webView != null) {
            webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {

        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();

            webView = null;
        }

        super.onDestroy();
    }

    private int dp(int value) {

        float density = getResources()
                .getDisplayMetrics()
                .density;

        return (int) (value * density + 0.5f);
    }
}
