package com.lgo.library

import android.content.Context
import android.content.SharedPreferences
import android.nfc.Tag
import android.preference.PreferenceManager
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import com.lgo.launcher.R
import org.jsoup.helper.HttpConnection
import org.xwalk.core.*
import org.xwalk.core.XWalkWebResourceRequest
import org.xwalk.core.XWalkView
import org.xwalk.core.XWalkWebResourceResponse
import org.xwalk.core.XWalkResourceClient
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URL
import kotlin.Exception


class CrossWalkView {
    private var setting: SharedPreferences? = null

    private val TAG = "XWalkView"

    private var xWalkView: XWalkView? = null
    private var cacheWoker: Cache? = null

    constructor(context: Context, viewGroup: ViewGroup?){
        newWebview(context, viewGroup)
    }

    init {
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true)
    }

    protected fun newWebview(context: Context, viewGroup: ViewGroup?){

        //Setting
        setting = PreferenceManager.getDefaultSharedPreferences(context)
        val appIndex = setting!!.getString("application_list", "0").toInt()
        val app = context.resources.getTextArray(R.array.pref_application_list_titles)

        //cache file
        if (setting!!.getBoolean("cache_file", false)){
            cacheWoker = Cache(context)
        }

        //Xwalk Init
        xWalkView = XWalkView(context)
        xWalkView?.layoutParams = viewGroup?.layoutParams



        xWalkView?.setResourceClient(object: XWalkResourceClient(xWalkView){

            override fun shouldInterceptLoadRequest(view: XWalkView?, request: XWalkWebResourceRequest?): XWalkWebResourceResponse? {

                if (request!!.method.equals("POST")){
                    return super.shouldInterceptLoadRequest(view, request)
                }

                if (!cacheWoker!!.isMediaFile(request!!.url.toString())){
                    return super.shouldInterceptLoadRequest(view, request)
                }

                var xWalkWebResourceResponse: XWalkWebResourceResponse? = null
                val connection = URL(request!!.url.toString()).openConnection()

                val pipedInputStream = PipedInputStream()
                val pipedOutputStream = PipedOutputStream(pipedInputStream)


                val input = connection.getInputStream()

                Log.i(TAG, "Cache File Start: " + request.url.toString())

                Thread(){

                    input.use { _ ->
                        pipedOutputStream.use { _ ->
                            try {
                                input.copyTo(pipedOutputStream)
                            }catch (ex: Exception){

                            }
                        }
                    }

                }.start()

                Log.i(TAG, "return input stream")

                xWalkWebResourceResponse = createXWalkWebResourceResponse("",
                                        "", pipedInputStream)


                if (xWalkWebResourceResponse != null){
                    return xWalkWebResourceResponse
                }

                return super.shouldInterceptLoadRequest(view, request)
            }
        })

        val xWalkSetting = xWalkView?.settings
        xWalkSetting?.cacheMode = WebSettings.LOAD_NO_CACHE
        xWalkSetting?.databaseEnabled = true
        xWalkSetting?.domStorageEnabled = true
        xWalkSetting?.javaScriptEnabled = true
        xWalkSetting?.builtInZoomControls = false
        xWalkSetting?.setSupportZoom(false)
        xWalkSetting?.mediaPlaybackRequiresUserGesture = false

        viewGroup?.addView(xWalkView)
    }

    fun loadUrl(url: String){
        xWalkView?.loadUrl(url)
    }

}