package com.tuservidor.ytshell

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var myWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Ocultar barras del sistema para que se sienta como la app nativa
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        myWebView = WebView(this)
        setContentView(myWebView)

        // 2. Configuración del motor web y engaño de User-Agent (Modo Tablet)
        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        // Usamos un User-Agent de iPad para obtener la interfaz nativa táctil más limpia
        webSettings.userAgentString = "Mozilla/5.0 (iPad; CPU OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1"

        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Aquí inyectaremos el script de bloqueo de anuncios cuando la página cargue
                injectAdBlocker()
            }
        }

        // Cargar YouTube
        myWebView.loadUrl("https://youtube.com")
    }

    // 3. Control estricto del ciclo de vida: Si sales de la app, TODO SE DETIENE
    override fun onPause() {
        super.onPause()
        myWebView.onPause() // Pausa los videos, el audio y los scripts web al instante
    }

    override fun onResume() {
        super.onResume()
        myWebView.onResume() // Reanuda la actividad solo cuando regresas a la app
    }

    private fun injectAdBlocker() {
        // Este método leerá tu script de GitHub para borrar los anuncios en milisegundos
        myWebView.evaluateJavascript("""
            // El script de inyección irá aquí
        """.trimIndent(), null)
    }
}
