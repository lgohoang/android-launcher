package com.lgo.library.XWeb

import android.app.ApplicationErrorReport
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.nfc.Tag
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.lgo.launcher.R
import com.lgo.library.XAndroidService
import org.jsoup.Jsoup
import java.io.*
import java.lang.Exception
import java.net.URL
import java.nio.charset.Charset
import java.util.stream.Stream


class XView {

    private var setting: SharedPreferences? = null
    private val TAG = "XWEB"
    private var webView: WebView? = null

    init {
        WebView.setWebContentsDebuggingEnabled(true)
    }

    constructor(context: Context, viewGroup: ViewGroup?){
        newWebview(context, viewGroup)
    }

    protected fun newWebview(context: Context, viewGroup: ViewGroup?){
        setting = PreferenceManager.getDefaultSharedPreferences(context)
        val appIndex = setting!!.getString("application_list", "0").toInt()
        val app = context.resources.getTextArray(R.array.pref_application_list_titles)


        //web view
        webView = WebView(context)
        webView?.layoutParams = viewGroup?.layoutParams
        webView?.webChromeClient = object : WebChromeClient(){
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
            }

            override fun getDefaultVideoPoster(): Bitmap {
                return super.getDefaultVideoPoster()
            }
        }

        if (app[appIndex].equals("Kiosk")){
            Log.i(TAG, "KIOSK APP: addJavascriptInterface")
            webView?.addJavascriptInterface(XAndroidService(context), "__QMS")
        }

        webView?.webViewClient = WebViewClient()

        val webSettings = webView?.settings

        webSettings?.javaScriptEnabled = true
        webSettings?.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings?.databaseEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.builtInZoomControls = false
        webSettings?.setSupportZoom(false)
        webSettings?.mediaPlaybackRequiresUserGesture = false
        webSettings?.allowFileAccessFromFileURLs = true
        webSettings?.allowContentAccess = true

        viewGroup?.addView(webView)
    }

    fun destroy(){
        webView?.destroy()
    }

    fun reload(delay: Long){
        Handler().postDelayed({
            webView?.reload()
            Log.i("webView", "reload: " + delay + "ms")
        }, delay)
    }


    fun loadUrl(url: String?){
        webView?.loadUrl(url)
    }
}