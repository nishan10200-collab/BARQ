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

        webView                  = findViewById(R.id.webViewB)
        val progressBar          = findViewById<ProgressBar>(R.id.progressBar)
        val errorLayout          = findViewById<LinearLayout>(R.id.errorTextView)
        val retryButton          = findViewById<Button>(R.id.retryButton)
        val errorMessage         = findViewById<TextView>(R.id.errorMessage)

        currentUrl = intent.getStringExtra("url")

        // ── إعدادات WebView ──────────────────────────────────────
        webView.settings.apply {
            javaScriptEnabled                     = true
            domStorageEnabled                     = true   // localStorage
            databaseEnabled                       = true
            cacheMode                             = WebSettings.LOAD_DEFAULT
            allowFileAccess                       = true
            allowContentAccess                    = true
            useWideViewPort                       = true
            loadWithOverviewMode                  = true
            setSupportZoom(false)
            builtInZoomControls                   = false
            displayZoomControls                   = false
            javaScriptCanOpenWindowsAutomatically = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }

        // ── منع Force Dark على Android 10+ ──────────────────────
        // MIUI يحاول قلب الألوان — نمنعه
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.isForceDarkAllowed = false
        }
        // خلفية WebView داكنة حتى أثناء التحميل
        webView.setBackgroundColor(0xFF0f172a.toInt())

        // ── WebViewClient ─────────────────────────────────────────
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = ProgressBar.GONE
                errorLayout.visibility = LinearLayout.GONE
            }

            // نتجاهل أخطاء الموارد الفرعية (صور، خطوط...)
            // ونعرض الخطأ فقط إذا فشلت الصفحة الرئيسية نفسها
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    progressBar.visibility = ProgressBar.GONE
                    errorLayout.visibility = LinearLayout.VISIBLE
                    val desc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        error?.description?.toString() ?: ""
                    } else ""
                    errorMessage.text = "تعذّر الاتصال\n$desc"
                }
            }

            // روابط تُفتح داخل WebView فقط
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }

        // ── شريط التحميل ─────────────────────────────────────────
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                progressBar.visibility =
                    if (newProgress == 100) ProgressBar.GONE else ProgressBar.VISIBLE
            }
        }

        // ── زر إعادة المحاولة ─────────────────────────────────────
        retryButton.setOnClickListener {
            errorLayout.visibility = LinearLayout.GONE
            progressBar.visibility = ProgressBar.VISIBLE
            currentUrl?.let { url -> webView.loadUrl(url) }
        }

        // ── تحميل الرابط ─────────────────────────────────────────
        currentUrl?.let { webView.loadUrl(it) }
    }

    // زر الرجوع يتنقل داخل WebView
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
