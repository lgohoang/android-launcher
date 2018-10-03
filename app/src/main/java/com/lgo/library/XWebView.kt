package com.lgo.library

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
import android.view.ViewGroup
import android.webkit.*
import com.lgo.launcher.R
import org.jsoup.Jsoup
import java.io.*
import java.lang.Exception
import java.net.URL
import java.nio.charset.Charset
import java.util.stream.Stream


class XWebView {

    private var setting: SharedPreferences? = null

    private val TAG = "XWEBVIEW"

    private var webView: WebView? = null
    private var cacheWoker: Cache? = null


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

        //cache file
        if (setting!!.getBoolean("cache_file", false)){
            cacheWoker = Cache(context)
        }


        //webview
        webView = WebView(context)
        webView?.layoutParams = viewGroup?.layoutParams
        webView?.webChromeClient = WebChromeClient()

        if (app[appIndex].equals("Kiosk")){
            Log.i(TAG, "KIOSK APP: addJavascriptInterface")
            webView?.addJavascriptInterface(XAndroidService(context), "__QMS")
        }

        webView?.webViewClient = object: WebViewClient(){

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.i("webViewClient", "onPageStarted: " + url)

                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.i("webViewClient", "onPageFinished: " + url)


                super.onPageFinished(view, url)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.e("webViewClient", "onReceivedError: " + error)

                if (setting!!.getBoolean("auto_reload", false)){
                    reload(5000)
                }

                super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                Log.e("webViewClient", "onReceivedHttpError: " + errorResponse)
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                Log.e("webViewClient", "onReceivedSslError: " + error)
                super.onReceivedSslError(view, handler, error)
            }


            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
//                Log.i("XWEB", "request: " + request?.url)

                val webResourceResponse = cacheWoker?.webResourceResponse(request)
                if (webResourceResponse != null){
                    return webResourceResponse
                }

                return super.shouldInterceptRequest(view, request)

//                    val connection = URL(request!!.url.toString()).openConnection()
//                    val inputStream = connection.getInputStream()
//
//                    var pipedInputStreamClosed = false
//
//                    val pipedInputStream = object : PipedInputStream(){
//                        override fun close() {
//                            super.close()
//                        }
//
//
//                        override fun read(): Int {
//                            return super.read()
//                        }
//
//                        override fun read(b: ByteArray?): Int {
//                            return super.read(b)
//                        }
//
//                        override fun read(b: ByteArray?, off: Int, len: Int): Int {
//                            return super.read(b, off, len)
//                        }
//
//                        override fun receive(b: Int) {
//                            super.receive(b)
//                        }
//
//                        override fun available(): Int {
//                            return connection.contentLength
//                        }
//
//                        override fun connect(src: PipedOutputStream?) {
//                            super.connect(src)
//                        }
//
//                        override fun mark(readlimit: Int) {
//                            super.mark(readlimit)
//                        }
//
//                        override fun markSupported(): Boolean {
//                            return super.markSupported()
//                        }
//                    }
//
//
//
//                    val pipedOutputStream = PipedOutputStream(pipedInputStream)
//
//
//                    val circularByteBuffer = CircularByteBuffer(-1, true)
//
//                    Thread(){
//
////                        try {
//                            inputStream.use { _ ->
//                                pipedOutputStream.use { _ ->
//                                    inputStream.copyTo(pipedOutputStream)
//                                }
//                            }
////                        }catch (ex: Exception){
////                            pipedOutputStream.close()
////                            pipedInputStream.close()
//////                            throw ex
////                        }
//
//
//
//
////                        while (true){
////                            val buffer = ByteArray(1000)
////                            val length = inputStream.read(buffer, 0, buffer.size)
////
////                            if (length < 0){
////                                break
////                            }
////
////                            pipedOutputStream.write(buffer, 0, length)
//////                            circularByteBuffer.getOutputStream().write(buffer, 0, length)
////
////                        }
////
////                        Log.i(TAG, "Available: ${pipedInputStream.available()}")
//////
////                        while (!pipedInputStreamClosed) {
////                            Log.i(TAG, "Available 2: ${pipedInputStream.available()}")
////                            Thread.sleep(100)
////                        }
//////
////                        Log.i(TAG, "Available 2: ${pipedInputStream.available()}")
//////
////                        pipedOutputStream.close()
////
//////                        circularByteBuffer.getOutputStream().flush()
//
//                    }.start()

//                    return WebResourceResponse("", "", pipedInputStream)
            }

            override fun onLoadResource(view: WebView?, url: String?) {

                Log.i(TAG, "onLoadResource: " + url)

                super.onLoadResource(view, url)
            }

        }

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
