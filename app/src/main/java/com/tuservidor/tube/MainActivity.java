package com.tuservidor.tube;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

public class MainActivity extends Activity {

    private GeckoRuntime runtime;
    private GeckoSession session;
    private GeckoView geckoView;

    private static final String YOUTUBE_URL =
            "https://www.youtube.com/";

    private static final String EXTENSION_LOCATION =
            "resource://android/assets/extensions/tube-adblock/";

    private static final String EXTENSION_ID =
            "tube-adblock@tuservidor.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        FrameLayout root = new FrameLayout(this);

        geckoView = new GeckoView(this);

        root.addView(
                geckoView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(root);

        runtime = GeckoRuntime.create(this);

        session = new GeckoSession();
        session.open(runtime);

        geckoView.setSession(session);

        instalarExtension();

        procesarIntent(getIntent());

        session.loadUri(YOUTUBE_URL);
    }

    private void instalarExtension() {

        runtime.getWebExtensionController()
                .ensureBuiltIn(
                        EXTENSION_LOCATION,
                        EXTENSION_ID
                )
                .accept(
                        extension -> {},
                        error -> {}
                );
    }

    private void procesarIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        Uri data = intent.getData();

        if (data == null) {
            return;
        }

        if ("com.tuservidor.tube".equalsIgnoreCase(data.getScheme())
                && "oauth2redirect".equalsIgnoreCase(data.getHost())) {

            String code = data.getQueryParameter("code");
            String error = data.getQueryParameter("error");

            if (error != null) {
                return;
            }

            if (code != null) {
                session.loadUri(YOUTUBE_URL);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        procesarIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session != null) {
            session.setActive(true);
        }
    }

    @Override
    protected void onPause() {

        if (session != null) {
            session.setActive(false);
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (session != null) {
            session.goBack();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        if (session != null) {
            session.close();
            session = null;
        }

        runtime = null;

        super.onDestroy();
    }
}
