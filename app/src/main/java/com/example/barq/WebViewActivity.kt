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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView        = findViewById<WebView>(R.id.webViewB)
        val progressBar    = findViewById<ProgressBar>(R.id.progressBar)
        val errorLayout    = findViewById<LinearLayout>(R.id.errorTextView)
        val retryButton    = findViewById<Button>(R.id.retryButton)       // زر إعادة المحاولة
        val errorMessage   = findViewById<TextView>(R.id.errorMessage)    // نص الخطأ

        currentUrl = intent.getStringExtra("url")

        // ── إعدادات WebView ──────────────────────────────────────
        webView.settings.apply {
            javaScriptEnabled      = true
            domStorageEnabled      = true          // localStorage يعمل
            databaseEnabled        = true
            cacheMode              = WebSettings.LOAD_DEFAULT
            allowFileAccess        = true
            allowContentAccess     = true
            setSupportZoom(false)
            builtInZoomControls    = false
            displayZoomControls    = false
            useWideViewPort        = true
            loadWithOverviewMode   = true
            javaScriptCanOpenWindowsAutomatically = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }

        // ── WebViewClient ─────────────────────────────────────────
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = ProgressBar.GONE
                errorLayout.visibility = LinearLayout.GONE
                webView.visibility     = WebView.VISIBLE
            }

            // API 23+ — الطريقة الحديثة
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // نتجاهل أخطاء الموارد الفرعية (صور، خطوط...) ونعرض الخطأ فقط للصفحة الرئيسية
                if (request?.isForMainFrame == true) {
                    progressBar.visibility = ProgressBar.GONE
                    webView.visibility     = WebView.GONE
                    errorLayout.visibility = LinearLayout.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        errorMessage.text = "تعذّر الاتصال\n${error?.description ?: ""}"
                    } else {
                        errorMessage.text = "تعذّر الاتصال بالسيرفر"
                    }
                }
            }

            // منع فتح روابط خارجية في متصفح خارجي
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }

        // ── WebChromeClient (شريط التحميل) ───────────────────────
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress == 100) ProgressBar.GONE else ProgressBar.VISIBLE
            }
        }

        // ── زر إعادة المحاولة ─────────────────────────────────────
        retryButton.setOnClickListener {
            errorLayout.visibility = LinearLayout.GONE
            webView.visibility     = WebView.VISIBLE
            progressBar.visibility = ProgressBar.VISIBLE
            currentUrl?.let { webView.loadUrl(it) }
        }

        // ── تحميل الرابط ─────────────────────────────────────────
        currentUrl?.let { webView.loadUrl(it) }
    }

    // زر الرجوع يتنقل داخل WebView بدل إغلاق النشاط
    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.webViewB)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
