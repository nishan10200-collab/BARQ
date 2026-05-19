package com.example.barq

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    private var currentUrl: String? = null
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webView          = findViewById(R.id.webViewB)
        val progressBar  = findViewById<ProgressBar>(R.id.progressBar)
        val errorLayout  = findViewById<LinearLayout>(R.id.errorTextView)
        val retryButton  = findViewById<Button>(R.id.retryButton)
        val errorMessage = findViewById<TextView>(R.id.errorMessage)

        currentUrl = intent.getStringExtra("url")

        // ── إعدادات الـ WebView ──
        webView.settings.apply {
            javaScriptEnabled      = true
            domStorageEnabled      = true
            databaseEnabled        = true
            cacheMode              = WebSettings.LOAD_DEFAULT
            allowFileAccess        = true
            allowContentAccess     = true
            useWideViewPort        = true
            loadWithOverviewMode   = true
            setSupportZoom(false)
            builtInZoomControls    = false
            displayZoomControls    = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = ProgressBar.GONE
                errorLayout.visibility = LinearLayout.GONE
            }

            // أخطاء الصفحة الرئيسية فقط — نتجاهل أخطاء الصور والخطوط
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    progressBar.visibility = ProgressBar.GONE
                    errorLayout.visibility = LinearLayout.VISIBLE
                    errorMessage.text = "تعذّر الاتصال بالسيرفر\nتحقق من الإنترنت وأعد المحاولة"
                }
            }

            // الروابط تُفتح داخل التطبيق فقط
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                progressBar.visibility =
                    if (newProgress == 100) ProgressBar.GONE else ProgressBar.VISIBLE
            }
        }

        retryButton.setOnClickListener {
            errorLayout.visibility = LinearLayout.GONE
            progressBar.visibility = ProgressBar.VISIBLE
            currentUrl?.let { url -> webView.loadUrl(url) }
        }

        currentUrl?.let { webView.loadUrl(it) }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
