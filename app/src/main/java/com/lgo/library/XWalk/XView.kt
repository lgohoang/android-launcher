package com.lgo.library.XWalk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.nfc.Tag
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import com.lgo.launcher.MainActivity
import com.lgo.launcher.R
import com.lgo.launcher.SettingsActivity
import com.lgo.library.XWalk.Cache
import org.jsoup.helper.HttpConnection
import org.xwalk.core.*
import org.xwalk.core.XWalkWebResourceRequest
import org.xwalk.core.XWalkView
import org.xwalk.core.XWalkWebResourceResponse
import org.xwalk.core.XWalkResourceClient
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URL
import kotlin.Exception

class XView {
    private var setting: SharedPreferences? = null

    private val TAG = "XWalk XView"

    private var xWalkView: XWalkView? = null
    private var cache: Cache? = null
    private lateinit var viewGroup: ViewGroup

    init {
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true)
    }

    constructor(context: Context, viewGroup: ViewGroup){

        setting = PreferenceManager.getDefaultSharedPreferences(context)
        this.viewGroup = viewGroup

        newWebview(context)
    }

    private fun newWebview(context: Context){
        val appIndex = setting!!.getString("application_list", "0").toInt()
        val app = context.resources.getTextArray(R.array.pref_application_list_titles)

        //cache file
        if (setting!!.getBoolean("cache_file", false)){
            cache = Cache(context)
        }

        //Xwalk Init
        xWalkView = XWalkView(context)
        xWalkView?.layoutParams = viewGroup?.layoutParams

        val countToStart = 3
        var count = 0
        var startMillis: Long = 0

        xWalkView?.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_UP) {
                //get system current milliseconds
                val time = System.currentTimeMillis()


                //if it is the first time, or if it has been more than 3 seconds since the first tap ( so it is like a new try), we reset everything
                if (startMillis == 0L || time - startMillis > 1000) {
                    startMillis = time
                    count = 1
                } else { //  time-startMillis< 3000
                    count++
                }//it is not the first, and it has been  less than 3 seconds since the first

                if (count == countToStart) {
                    val intent = Intent(context, SettingsActivity::class.java)
                    startActivity(context, intent, null)
                }

                return@OnTouchListener false
            }

            false
        })

        xWalkView?.setResourceClient(object: XWalkResourceClient(xWalkView){
            override fun shouldInterceptLoadRequest(view: XWalkView?, request: XWalkWebResourceRequest?): XWalkWebResourceResponse? {
                Log.i(TAG, "Request ${request?.url.toString()}")
                //skip request
                if ((request!!.method.equals("POST") || cache == null)){
                    return super.shouldInterceptLoadRequest(view, request)
                }

                var webResourceResponse: XWalkWebResourceResponse? = null
                var responseHeader = mutableMapOf<String, String>()

                val connection = URL(request.url.toString()).openConnection()

                if (cache!!.isMediaFile(request.url.toString())){
                    //cache first mode
                    //check file exist
                    Log.i(TAG, "Cache First Mode")
                    val file = cache!!.buildMediaFilePath(request.url)
                    if (cache!!.checkFileExists(file)){
                        //return file
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(context, "Request ${request.url} from storage", Toast.LENGTH_LONG).show()
                        }
                        responseHeader.put("X-Status", "From Storage")
                        webResourceResponse = createXWalkWebResourceResponse(connection.contentType, connection.contentEncoding,
                                cache!!.readFile(file))
                        webResourceResponse?.responseHeaders = responseHeader
                        return webResourceResponse
                    }else{
                        //cache on inputStream
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(context, "Request ${request.url} start cache", Toast.LENGTH_LONG).show()
                        }
                        val input = cache!!.cacheOnInputStream(connection.getInputStream(), file, request.url.toString())
                        responseHeader.put("X-Status", "Caching & Forward")
                        webResourceResponse = createXWalkWebResourceResponse(connection.contentType, connection.contentEncoding,
                                input)
                        webResourceResponse?.responseHeaders = responseHeader
                        return webResourceResponse
                    }

                }else{
                    //fallback mode
                    Log.i(TAG, "Fallback Mode")
                    val file = cache!!.buildFileFromUrl(request.url)
                    try {
                        responseHeader.put("X-Status", "Caching & Forward")
                        webResourceResponse = createXWalkWebResourceResponse("",
                                "",
                                cache!!.cacheOnInputStream(connection.getInputStream(), file, request.url.toString()))
                        webResourceResponse?.responseHeaders = responseHeader
                        return webResourceResponse
                    }catch (ex: Exception){
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            Toast.makeText(context, "Request ${request.url.toString()} from storage", Toast.LENGTH_LONG).show()
                        }
                        responseHeader.put("X-Status", "From Storage")
                        webResourceResponse = createXWalkWebResourceResponse("",
                                "",
                                cache!!.readFile(file))
                        webResourceResponse?.responseHeaders = responseHeader
                        return webResourceResponse
                    }
                }
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
    }

    fun show(){
        viewGroup?.addView(xWalkView)
    }

    fun loadUrl(url: String){
        xWalkView?.loadUrl(url)
    }
}