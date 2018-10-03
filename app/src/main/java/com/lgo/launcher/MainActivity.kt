package com.lgo.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.support.v4.view.GestureDetectorCompat
import android.view.MotionEvent
import com.lgo.library.CrossWalkView
import com.lgo.library.GestureListener
import com.lgo.library.XWebView
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private val TAG = "Main"
    private var mDetector: GestureDetectorCompat? = null
    private var setting: SharedPreferences? = null

    var autoHide: Boolean = false

    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setting = PreferenceManager.getDefaultSharedPreferences(this)

        mDetector = GestureDetectorCompat(this, GestureListener())


        if (setting!!.getBoolean("auto_fullscreen", false)){
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                Log.i(TAG, "visibility: " + visibility)
                if (visibility.equals(0)){
                    hideSystemUI()
                }
            }
            hideSystemUI()
        }
        try {
            val viewGroup: ViewGroup = findViewById(R.id.layout) as ViewGroup
//            val xWebView = XWebView(this, viewGroup)
//            xWebView.loadUrl(loadUrl())
            val crossWalkView: CrossWalkView = CrossWalkView(this, viewGroup)
            crossWalkView.loadUrl(loadUrl())

        }catch (ex: Exception){
            throw ex
        }

    }

    fun loadUrl(): String {
        var address = setting!!.getString("url", "http://miraway.vn")

        val appIndex = setting!!.getString("application_list", "0").toInt()
        val app = this.resources.getTextArray(R.array.pref_application_list_titles)

        when (app[appIndex]) {
            "None" -> {

            }

            "Digital Signage" -> {

            }

            "Kiosk" -> {
                address += "/device/#/kiosk"
            }
            "Screen" -> {
                address += "/device/#/screen"
            }
            "Feedback" -> {
                address += "/device/#/feedback"
            }
            "Counter" -> {
                address += "/app/#/counter"
            }
            else -> {
            }
        }
        return address
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        mDetector?.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

}
