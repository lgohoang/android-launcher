package com.lgo.library

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Looper
import android.support.annotation.MainThread
import android.support.annotation.UiThread
import android.util.Log
import android.webkit.*
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import java.util.*
import java.util.logging.Handler
import com.google.gson.annotations.SerializedName
import com.lgo.launcher.MainActivity

data class ITicket (
        @SerializedName("id")
        val Id: String,
        @SerializedName("state")
        var State: String,
        @SerializedName("services")
        var Services: Array<String>,
        @SerializedName("service")
        var Service: String,
        @SerializedName("counter_id")
        var Counter_id: String,
        @SerializedName("cdate")
        var CDate: Date,
        @SerializedName("lang")
        var Lang: String,
        @SerializedName("ctime")
        var CTime: Number,
        @SerializedName("mtime")
        var Mtime: Number,
        @SerializedName("branch")
        var Branch: String,
        @SerializedName("store")
        var Store: String,
        @SerializedName("cnum")
        var CNum: String,
        @SerializedName("priority")
        var Priority: Number
)

data class ITemplate(
        @SerializedName("uri")
        val uri: String
)

data class ITicketCommand(
        @SerializedName("uri")
        val uri: String,
        @SerializedName("data")
        val Data: JsonObject
)

class XAndroidService {

    private val TAG = "XAndroidService"
    private var context: Context? = null
    private var ticketFormat: TicketFormat? = null

    constructor(context: Context){
        this.context = context
        this.ticketFormat = TicketFormat(context)
    }

    @JavascriptInterface
    fun BroadcastStringify(command: String, body: String?){

        Log.i(TAG, "Command: " + command)

        when(command){
            "/kiosk/ticket/command" -> {
                ticketFormat?.command(body?.trim('\\'))
            }
        }
    }
}

class TicketFormat{

    private val TAG = "PRINT"
    var webView: WebView? = null
    var context: Context? = null
    var view: WebView? = null
    var queue: Queue<String>? = null
    var mainHandler: android.os.Handler? = null

    init {

    }

    constructor(context: Context){

        val that = this

        this.mainHandler = android.os.Handler(context.mainLooper)
        this.context = context
        webView = WebView(context)


        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings?.databaseEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.builtInZoomControls = false
        webSettings?.setSupportZoom(false)
        webSettings?.allowFileAccessFromFileURLs = true
        webSettings?.allowFileAccess = true
        webSettings?.allowUniversalAccessFromFileURLs = true
        webSettings?.allowContentAccess = true
        webSettings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView?.loadUrl("file:///android_asset/ticket/ticket.html")
    }

    fun command(arg: String?){

        val mainHandler: android.os.Handler = android.os.Handler(context!!.mainLooper)

        val runnable = Runnable {
            webView?.loadUrl("javascript:Command("+ arg +");")
        }

        mainHandler.post(runnable)
    }
}

